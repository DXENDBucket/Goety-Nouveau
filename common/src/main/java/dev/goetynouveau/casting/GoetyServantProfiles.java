package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.BearServant;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.common.magic.spells.necromancy.*;
import com.Polarice3.Goety.common.magic.spells.wild.HuntingSpell;
import com.Polarice3.Goety.common.magic.spells.wild.SlimySpell;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.BlockFinder;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeSummonHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Species rules only. Owner quota, payment and lifecycle are shared by all servants. */
final class GoetyServantProfiles {
    private GoetyServantProfiles() {}
    static int count(EffectFocus focus, SummonSpell spell, LivingEntity caster, ItemStack staff) {
        return switch (focus) {
            case SLIMY -> spell.rightStaff(staff) ? 2 + caster.getRandom().nextInt(2) : ((SlimySpell)spell).specialStaffs(staff) ? 2 : 1;
            case CARRION -> spell.rightStaff(staff) ? 3 + caster.getRandom().nextInt(4) : 3;
            case WHISPER -> spell.rightStaff(staff) ? 2 : 1;
            case FROSTBORN -> spell.rightStaff(staff) && caster.getRandom().nextBoolean() ? 2 : 1;
            case SPRITE -> spell.rightStaff(staff) ? 2 + caster.getRandom().nextInt(4) : 1;
            case VANGUARD, BLACKGUARD -> NativeSummonHooks.guardCount(focus, spell, caster, staff);
            case LEAPING -> spell.rightStaff(staff) && caster.getRandom().nextBoolean() ? 2 : 1;
            case VEX -> spell.rightStaff(staff) ? 3 + caster.getRandom().nextInt(3) : 3;
            case HUNTING -> spell.rightStaff(staff) ? 3 : ((HuntingSpell)spell).specialStaffs(staff) ? 2 : 1;
            case MAULING -> spell.rightStaff(staff) ? 2 : 1;
            case REAPER, WRAITH, PHANTOM -> NativeSummonHooks.necromancyCount(focus, spell, caster, staff);
            default -> staff.is(ModItems.NAMELESS_STAFF.get()) ? 7
                    : spell.rightStaff(staff) ? 2 + caster.getRandom().nextInt(4) : special(spell, staff) ? 2 : 1;
        };
    }
    private static boolean special(SummonSpell spell, ItemStack staff) {
        return spell instanceof ZombieSpell z ? z.specialStaffs(staff)
                : spell instanceof SkeletonSpell s && s.specialStaffs(staff);
    }
    static Owned seed(EffectFocus focus, ServerLevel world) {
        return switch (focus) {
            case VEX -> ModEntityType.VEX_SERVANT.get().create(world);
            case LEAPING -> ModEntityType.LEAPLEAF.get().create(world);
            case HUNTING -> ModEntityType.BLACK_WOLF.get().create(world);
            case MAULING -> ModEntityType.BEAR_SERVANT.get().create(world);
            case REAPER -> ModEntityType.REAPER_SERVANT.get().create(world);
            case WRAITH -> ModEntityType.WRAITH_SERVANT.get().create(world);
            case PHANTOM -> ModEntityType.PHANTOM_SERVANT.get().create(world);
            case SLIMY -> ModEntityType.SLIME_SERVANT.get().create(world);
            case CARRION -> ModEntityType.CARRION_FLY.get().create(world);
            case WHISPER -> ModEntityType.WHISPERER.get().create(world);
            case FROSTBORN -> ModEntityType.ICE_GOLEM.get().create(world);
            case SPRITE -> ModEntityType.SPRITE.get().create(world);
            case VANGUARD -> ModEntityType.VANGUARD_SERVANT.get().create(world);
            case BLACKGUARD -> ModEntityType.BLACKGUARD_SERVANT.get().create(world);
            case ZOMBIE -> ModEntityType.ZOMBIE_SERVANT.get().create(world);
            case SKELETON -> ModEntityType.SKELETON_SERVANT.get().create(world);
            default -> throw new IllegalArgumentException("Not a servant focus: " + focus);
        };
    }
    static Owned variant(EffectFocus focus, Owned seed, ServerLevel world, BlockPos pos,
                         SummonSpell spell, LivingEntity caster, ItemStack staff) {
        EntityType<?> type = seed.getType();
        if (focus == EffectFocus.SLIMY) {
            if (((SlimySpell)spell).specialStaffs(staff)) {
                if (spell.typeStaff(staff, SpellType.ABYSS)) type = ModEntityType.TROPICAL_SLIME_SERVANT.get();
                else if (spell.typeStaff(staff, SpellType.NECROMANCY)) type = ModEntityType.CRYPT_SLIME_SERVANT.get();
                else if (spell.typeStaff(staff, SpellType.NETHER)) type = ModEntityType.MAGMA_CUBE_SERVANT.get();
            } else if (world.isWaterAt(pos)) type = ModEntityType.TROPICAL_SLIME_SERVANT.get();
            else if (world.dimension() == Level.NETHER) type = ModEntityType.MAGMA_CUBE_SERVANT.get();
            else if (BlockFinder.findStructure(world, pos, ModTags.Structures.CRYPT)) type = ModEntityType.CRYPT_SLIME_SERVANT.get();
        } else if (focus == EffectFocus.WHISPER) {
            if (world.isWaterAt(pos) || spell.typeStaff(staff, SpellType.ABYSS)) type = ModEntityType.WAVEWHISPERER.get();
        } else if (focus == EffectFocus.BLACKGUARD) {
            type = NativeSummonHooks.blackguardType(staff);
        } else if (focus == EffectFocus.HUNTING) {
            if (spell.typeStaff(staff, SpellType.NECROMANCY)) type = ModEntityType.SKELETON_WOLF.get();
            else if ((world.isThundering() && world.canSeeSky(pos)) || spell.typeStaff(staff, SpellType.STORM)) type = ModEntityType.STORMHOUND.get();
            else if (NativeSummonHooks.mountain(world, pos) || spell.typeStaff(staff, SpellType.WIND)) type = ModEntityType.TWILIGHT_GOAT.get();
            else if (world.isWaterAt(pos) || spell.typeStaff(staff, SpellType.ABYSS)) type = ModEntityType.SNAPPER.get();
            else if (NativeSummonHooks.snow(world, pos) || spell.typeStaff(staff, SpellType.FROST)) type = ModEntityType.WINTER_WOLF.get();
            else if (world.dimensionType().ultraWarm() || world.getBiome(pos).is(BiomeTags.IS_NETHER) || spell.typeStaff(staff, SpellType.NETHER)) type = ModEntityType.HELLHOUND.get();
        } else if (focus == EffectFocus.MAULING) {
            if (world.isWaterAt(pos) || spell.typeStaff(staff, SpellType.ABYSS)) type = ModEntityType.GNASHER.get();
            else if (spell.typeStaff(staff, SpellType.FROST) || NativeSummonHooks.cold(world, pos)) type = ModEntityType.POLAR_BEAR_SERVANT.get();
            else if (pos.getY() <= 64 && !world.canSeeSky(pos) && seed instanceof BearServant bear) bear.setBearCave();
            else if (spell.typeStaff(staff, SpellType.NETHER) || world.dimension() == Level.NETHER) type = ModEntityType.HOGLIN_SERVANT.get();
        } else if (focus == EffectFocus.WRAITH) {
            if (spell.typeStaff(staff, SpellType.WILD)) type = ModEntityType.MUCK_WRAITH_SERVANT.get();
            else type = seed.getVariant(caster instanceof Player p ? p : null, world, pos);
        } else if (focus == EffectFocus.ZOMBIE || focus == EffectFocus.SKELETON) {
            boolean zombie = focus == EffectFocus.ZOMBIE;
            if (!special(spell, staff)) type = seed.getVariant(caster instanceof Player p ? p : null, world, pos);
            else if (spell.typeStaff(staff, SpellType.FROST)) type = zombie ? ModEntityType.FROZEN_ZOMBIE_SERVANT.get() : ModEntityType.STRAY_SERVANT.get();
            else if (spell.typeStaff(staff, SpellType.STORM)) type = zombie ? ModEntityType.FRAYED_SERVANT.get() : ModEntityType.RATTLED_SERVANT.get();
            else if (spell.typeStaff(staff, SpellType.WILD)) type = zombie ? ModEntityType.JUNGLE_ZOMBIE_SERVANT.get() : ModEntityType.MOSSY_SKELETON_SERVANT.get();
            else if (spell.typeStaff(staff, SpellType.NETHER)) type = zombie ? ModEntityType.ZPIGLIN_SERVANT.get() : ModEntityType.WITHER_SKELETON_SERVANT.get();
            else if (spell.typeStaff(staff, SpellType.ABYSS)) type = zombie ? ModEntityType.DROWNED_SERVANT.get() : ModEntityType.SUNKEN_SKELETON_SERVANT.get();
            else if (staff.is(ModItems.OMINOUS_STAFF.get())) type = zombie ? ModEntityType.ZOMBIE_VINDICATOR_SERVANT.get() : ModEntityType.SKELETON_PILLAGER_SERVANT.get();
        }
        if (type == null || type == seed.getType()) return seed;
        var candidate = type.create(world);
        if (candidate instanceof Owned replacement && spell.summonPredicate().test(replacement)) {
            seed.discard(); return replacement;
        }
        if (candidate != null) candidate.discard();
        return seed;
    }
    static boolean nearbyFlying(EffectFocus focus) {
        return focus == EffectFocus.VEX || focus == EffectFocus.CARRION || focus == EffectFocus.SPRITE;
    }
    static boolean waterSearch(EffectFocus focus) {
        return focus == EffectFocus.HUNTING || focus == EffectFocus.MAULING || focus == EffectFocus.SLIMY || focus == EffectFocus.WHISPER;
    }
    static boolean guard(EffectFocus focus) { return focus == EffectFocus.VANGUARD || focus == EffectFocus.BLACKGUARD; }
    static boolean formation(EffectFocus focus, ItemStack staff) { return guard(focus) && staff.is(ModItems.NAMELESS_STAFF.get()); }
    static boolean persistent(EffectFocus focus) { return focus != EffectFocus.VEX && focus != EffectFocus.PHANTOM && focus != EffectFocus.SPRITE; }
    static boolean uponSummon(EffectFocus focus) { return focus != EffectFocus.VEX && focus != EffectFocus.PHANTOM && focus != EffectFocus.SPRITE; }
    static boolean wildSound(EffectFocus focus) {
        return switch (focus) {
            case LEAPING, HUNTING, MAULING, SLIMY, CARRION, WHISPER, FROSTBORN, SPRITE -> true;
            default -> false;
        };
    }
    static boolean grounded(EffectFocus focus, Owned servant, LivingEntity caster) {
        if (nearbyFlying(focus) || focus == EffectFocus.PHANTOM) return false;
        if (focus == EffectFocus.MAULING) return !caster.isUnderWater();
        return servant.getType() != ModEntityType.TROPICAL_SLIME_SERVANT.get()
                && servant.getType() != ModEntityType.WAVEWHISPERER.get()
                && servant.getType() != ModEntityType.SNAPPER.get()
                && servant.getType() != ModEntityType.DROWNED_SERVANT.get()
                && servant.getType() != ModEntityType.SUNKEN_SKELETON_SERVANT.get();
    }
    static boolean recallable(EffectFocus focus, LivingEntity servant, LivingEntity caster) {
        return !((focus == EffectFocus.HUNTING && servant.getType() == ModEntityType.SNAPPER.get())
                || (focus == EffectFocus.MAULING && servant.getType() == ModEntityType.GNASHER.get())) || caster.isUnderWater();
    }
}
