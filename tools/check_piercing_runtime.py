"""Check piercing access against an actual Goety release, without deobfuscating it.

Usage: python tools/check_piercing_runtime.py <addon.jar> <goety.jar>
This verifies release member names/descriptors, not complete Mixin transformation or visuals.
"""
import argparse
from pathlib import Path
import zipfile


def class_members(data):
    position = 8

    def read(size):
        nonlocal position
        value = int.from_bytes(data[position:position + size], 'big')
        position += size
        return value

    if data[:4] != b'\xca\xfe\xba\xbe':
        raise ValueError('Not a Java class')
    pool = [None] * read(2)
    index = 1
    while index < len(pool):
        tag = read(1)
        if tag == 1:
            size = read(2)
            pool[index] = data[position:position + size].decode('utf-8', errors='replace')
            position += size
        elif tag in (7, 8, 16, 19, 20):
            read(2)
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            read(4)
        elif tag in (5, 6):
            read(8)
            index += 1
        elif tag == 15:
            read(3)
        else:
            raise ValueError(f'Unknown constant pool tag: {tag}')
        index += 1
    read(6)  # access flags, this class, superclass
    for _ in range(read(2)):
        read(2)

    def members():
        nonlocal position
        result = set()
        for _ in range(read(2)):
            read(2)
            result.add((pool[read(2)], pool[read(2)]))
            for _ in range(read(2)):
                read(2)
                position_length = read(4)
                position += position_length
        return result

    return members(), members(), set(value for value in pool if isinstance(value, str))


def check(addon_path, native_path):
    mixin_path = 'dev/goetynouveau/mixin/NativePiercingHistoryMixin.class'
    fields_expected = {
        ('piercingIgnoreEntityIds', 'Lit/unimi/dsi/fastutil/ints/IntOpenHashSet;'),
        ('piercedAndKilledEntities', 'Ljava/util/List;'),
    }
    with zipfile.ZipFile(addon_path) as addon, zipfile.ZipFile(native_path) as native:
        _, methods, constants = class_members(addon.read(mixin_path))
        if 'Lorg/spongepowered/asm/mixin/gen/Invoker;' in constants:
            raise ValueError('Piercing history still uses a name-sensitive private invoker')
        if ('goetyNouveau$resetPiercing', '()V') not in methods:
            raise ValueError('Piercing reset implementation is missing')
        for getter, descriptor in (
            ('goetyNouveau$piercingIds', '()Lit/unimi/dsi/fastutil/ints/IntOpenHashSet;'),
            ('goetyNouveau$piercedAndKilled', '()Ljava/util/List;'),
        ):
            if (getter, descriptor) not in methods:
                raise ValueError(f'Accessor signature changed: {getter}')
        for obsolete in ('IceSpearPiercingAccess', 'QuillPiercingAccess'):
            if f'dev/goetynouveau/mixin/{obsolete}.class' in addon.namelist():
                raise ValueError(f'Obsolete invoker is still packaged: {obsolete}')
        for field, descriptor in fields_expected:
            if field not in constants:
                raise ValueError(f'Missing native field accessor: {field}')
        for name in ('PoisonQuill', 'IceSpear'):
            path = f'com/Polarice3/Goety/common/entities/projectiles/{name}.class'
            fields, _, _ = class_members(native.read(path))
            if missing := fields_expected - fields:
                raise ValueError(f'{name} release fields changed: {sorted(missing)}')
        if any(name.startswith('com/Polarice3/Goety/') for name in addon.namelist()):
            raise ValueError('Addon unexpectedly bundles Goety classes')
    print(f'PASS: {Path(addon_path).name} against {Path(native_path).name}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('addon', type=Path)
    parser.add_argument('goety', type=Path)
    arguments = parser.parse_args()
    check(arguments.addon, arguments.goety)
