package dev.goetynouveau.platform;

import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.spells.necromancy.HauntedSkullSpell;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;

/** Retain the loader's actual native spell differences. */
public final class NativeSummonHooks {
    private NativeSummonHooks() {}
    public static LivingEntity owner(LivingEntity caster) { return MobUtil.getSummonOwner(caster); }
    public static int count(HauntedSkullSpell spell, ItemStack staff) { return spell.rightStaff(staff) ? 3 : 1; }
    public static int burning(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.BURNING, caster); }
    public static int additionalDuration(LivingEntity caster) { return 0; }
    public static void sharpen(com.Polarice3.Goety.common.entities.ally.illager.raider.AllyVex vex, int potency) {
        vex.getMainHandItem().enchant(vex.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS),potency);
        vex.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,vex.getMainHandItem());
    }
    public static void finalizeSpawn(ServerLevel world, net.minecraft.world.entity.Mob skull, BlockPos position) {
        skull.finalizeSpawn(world, world.getCurrentDifficultyAt(position), MobSpawnType.MOB_SUMMONED, null);
    }
    public static net.minecraft.world.entity.SpawnGroupData finalizeGroup(ServerLevel world, net.minecraft.world.entity.Mob mob,
            BlockPos pos, net.minecraft.world.entity.SpawnGroupData group) {
        return mob.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, group);
    }
    public static boolean buffPhantom() { return true; }
    public static boolean mountain(ServerLevel world, BlockPos pos) { return world.getBiome(pos).is(net.neoforged.neoforge.common.Tags.Biomes.IS_MOUNTAIN); }
    public static boolean cold(ServerLevel world, BlockPos pos) { return world.getBiome(pos).is(net.neoforged.neoforge.common.Tags.Biomes.IS_COLD_OVERWORLD); }
    public static boolean snow(ServerLevel world, BlockPos pos) { return world.getBiome(pos).value().coldEnoughToSnow(pos); }
    public static void huntingArmor(com.Polarice3.Goety.common.entities.neutral.Owned servant) {
        if(servant instanceof com.Polarice3.Goety.common.entities.ally.Summoned summoned) com.Vivideru.Goety.common.items.VivideruWolfArmorUtil.equipRingGrantedArmor(summoned);
    }
    public static int necromancyCount(dev.goetynouveau.api.EffectFocus focus, com.Polarice3.Goety.common.magic.SummonSpell spell,
                                      LivingEntity caster, ItemStack staff) {

        if(focus==dev.goetynouveau.api.EffectFocus.PHANTOM) return spell.rightStaff(staff)?1+caster.getRandom().nextInt(4):1;
        if(spell.rightStaff(staff)) return 2+caster.getRandom().nextInt(4);

        return 1;
    }
    public static int guardCount(dev.goetynouveau.api.EffectFocus focus, com.Polarice3.Goety.common.magic.SummonSpell spell,
                                 LivingEntity caster, ItemStack staff) {
        if(staff.is(com.Polarice3.Goety.common.items.ModItems.NAMELESS_STAFF.get())) return 7;
        if(spell.rightStaff(staff)) return 2+caster.getRandom().nextInt(6);

        return 1;
    }
    public static net.minecraft.world.entity.EntityType<? extends com.Polarice3.Goety.common.entities.neutral.Owned> blackguardType(ItemStack staff) {

        return com.Polarice3.Goety.common.entities.ModEntityType.BLACKGUARD_SERVANT.get();
    }
    public static void guardParticles(com.Polarice3.Goety.common.magic.SummonSpell spell, ServerLevel world,
                                     LivingEntity caster, ItemStack staff, LivingEntity servant) {
        world.sendParticles(com.Polarice3.Goety.client.particles.ModParticleTypes.LICH.get(),servant.getX(),servant.getY(),servant.getZ(),1,0,0,0,0);
        com.Polarice3.Goety.utils.ServerParticleUtil.summonPowerfulUndeadParticles(world,servant);
        spell.playSound(world,servant,com.Polarice3.Goety.init.ModSounds.SOUL_EXPLODE.get(),0.25F+world.random.nextFloat()/2,1);
        spell.playSound(world,servant,net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,0.25F+world.random.nextFloat()/2,1);
    }
}
