package dev.goetynouveau.platform;

import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.projectiles.WaterHurtingProjectile;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;

public final class NativeBoltHooks {
    private NativeBoltHooks() {}
    public static int velocityLevel(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.VELOCITY.get(), caster); }
    public static boolean impactCancelled(WaterHurtingProjectile bolt, HitResult hit) {
        return net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(bolt, hit);
    }
}
