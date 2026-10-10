package dev.goetynouveau.platform;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class NativeEffectHooks {
    private NativeEffectHooks() {}
    public static int duration(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.DURATION.get(), caster); }
    public static int radius(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.RADIUS.get(), caster); }
    public static MobEffectInstance freezing(int ticks, int amplifier) { return new MobEffectInstance(GoetyEffects.FREEZING.get(), ticks, amplifier); }
}
