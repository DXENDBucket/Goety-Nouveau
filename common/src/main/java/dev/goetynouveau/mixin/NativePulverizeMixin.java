package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.geomancy.PulverizeSpell;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=PulverizeSpell.class,remap=false)
public abstract class NativePulverizeMixin {
 @Inject(method="pulverize",at=@At("HEAD"),cancellable=true)
 private void goetyNouveau$loaded(ServerLevel world,LivingEntity caster,BlockPos pos,CallbackInfo ci){var v=GoetyNativeGeometry.current();if(v!=null && v.spell()==(Object)this && (!world.isInWorldBounds(pos) || !world.hasChunkAt(pos)))ci.cancel();}
}
