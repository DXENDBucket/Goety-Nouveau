package dev.goetynouveau.platform;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class NativeEffectHooks {
    private NativeEffectHooks() {}
    public static void windStaff(com.Polarice3.Goety.common.magic.Spell spell, LivingEntity caster, net.minecraft.world.item.ItemStack staff, LivingEntity target) {
        if(spell.typeStaff(staff,com.Polarice3.Goety.api.magic.SpellType.FROST)) target.addEffect(freezing(100,0));
        if(spell.typeStaff(staff,com.Polarice3.Goety.api.magic.SpellType.WILD)) target.addEffect(new MobEffectInstance(
                com.Polarice3.Goety.utils.CuriosFinder.hasWildRobe(caster)?GoetyEffects.ACID_VENOM:net.minecraft.world.effect.MobEffects.POISON,100));
        if(spell.typeStaff(staff,com.Polarice3.Goety.api.magic.SpellType.STORM)) target.addEffect(spasms(100));
        if(spell.typeStaff(staff,com.Polarice3.Goety.api.magic.SpellType.VOID)) target.addEffect(new MobEffectInstance(GoetyEffects.VOID_TOUCHED,100));
        if(spell.typeStaff(staff,com.Polarice3.Goety.api.magic.SpellType.NETHER)) target.igniteForSeconds(5);
    }
    public static MobEffectInstance sensing(int ticks, int amplifier) { return new MobEffectInstance(GoetyEffects.TREMOR_SENSE,ticks,amplifier,false,false,true); }
    public static MobEffectInstance charged(LivingEntity entity) { return entity.getEffect(GoetyEffects.CHARGED); }
    public static int absorb(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.ABSORB, caster); }
    public static int soulEater(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.SOUL_EATER, caster); }
    public static int duration(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.DURATION, caster); }
    public static int radius(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.RADIUS, caster); }
    public static MobEffectInstance freezing(int ticks, int amplifier) { return new MobEffectInstance(GoetyEffects.FREEZING, ticks, amplifier); }
    public static MobEffectInstance ironHide(int ticks, int amplifier) { return new MobEffectInstance(GoetyEffects.IRON_HIDE, ticks, amplifier, false, false, true); }
    public static MobEffectInstance chillHide(int ticks, int amplifier) { return new MobEffectInstance(GoetyEffects.CHILL_HIDE, ticks, amplifier); }
    public static MobEffectInstance sapped(int ticks, int amplifier) { return new MobEffectInstance(GoetyEffects.SAPPED, ticks, amplifier); }
    public static MobEffectInstance spasms(int ticks) { return new MobEffectInstance(GoetyEffects.SPASMS, ticks); }
    public static MobEffectInstance cursed(int ticks) { return new MobEffectInstance(GoetyEffects.CURSED, ticks); }
}
