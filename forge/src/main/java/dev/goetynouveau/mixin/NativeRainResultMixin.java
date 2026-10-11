package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.magic.spells.ArrowRainSpell;
import com.Polarice3.Goety.common.magic.spells.frost.BlizzardSpell;
import com.Polarice3.Goety.common.magic.spells.nether.MeteorShowerSpell;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Retain explicit Conflux ownership without making a fake caster or moving the real one. */
@Mixin(value={ArrowRainSpell.class,BlizzardSpell.class,MeteorShowerSpell.class,com.Polarice3.Goety.common.magic.spells.nether.BombardmentSpell.class},remap=false)
public abstract class NativeRainResultMixin {
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean goetyNouveau$owner(ServerLevel world,Entity entity) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && entity instanceof Projectile projectile) {
            var owner=view.context().damageOwner();
            projectile.setOwner(owner!=null?owner:view.context().caster());
        }
        return world.addFreshEntity(entity);
    }
}
