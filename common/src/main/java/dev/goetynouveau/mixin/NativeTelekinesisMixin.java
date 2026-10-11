package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.magic.spells.TelekinesisSpell;
import com.Polarice3.Goety.utils.*;
import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=TelekinesisSpell.class,remap=false)
public abstract class NativeTelekinesisMixin {
    @Shadow public Entity victim;
    @Unique private LivingEntity goetyNouveau$gear(LivingEntity actor) {
        var view=GoetyNativeGeometry.current();return view!=null && view.spell()==(Object)this && actor==view.host()?view.context().caster():actor;
    }
    @ModifyVariable(method={"conditionsMet","findVictim","SpellResult"},at=@At("HEAD"),argsOnly=true,index=2)
    private LivingEntity goetyNouveau$recipient(LivingEntity caster){return GoetyNativeGeometry.spatial(this,caster);}
    @Redirect(method={"conditionsMet","findVictim","SpellResult"},at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean goetyNouveau$enchanted(LivingEntity caster){return WandUtil.enchantedFocus(goetyNouveau$gear(caster));}
    @Redirect(method={"findVictim","SpellResult"},at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;getPotencyLevel(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int goetyNouveau$potency(LivingEntity caster){return WandUtil.getPotencyLevel(goetyNouveau$gear(caster));}
    @Redirect(method={"conditionsMet","findVictim"},at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;getRangeLevel(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int goetyNouveau$range(LivingEntity caster){return WandUtil.getRangeLevel(goetyNouveau$gear(caster));}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/CuriosFinder;hasCurio(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Z"))
    private boolean goetyNouveau$ring(LivingEntity caster,Item item){return CuriosFinder.hasCurio(goetyNouveau$gear(caster),item);}
    @Inject(method="findVictim",at=@At("RETURN"))
    private void goetyNouveau$filter(net.minecraft.server.level.ServerLevel world,LivingEntity caster,com.Polarice3.Goety.common.magic.SpellStat stats,CallbackInfo ci){
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell()==(Object)this && victim!=null && (!GoetyTelekinesisCasting.victimAllowed(view,victim)
                || view.host().distanceTo(victim)>stats.getRange()+(WandUtil.enchantedFocus(view.context().caster())?WandUtil.getRangeLevel(view.context().caster()):0)))victim=null;
    }
    @Unique private DamageSource goetyNouveau$damage(DamageSource source){
        var view=GoetyNativeGeometry.current();if(view==null || view.spell()!=(Object)this)return source;
        var owner=view.context().damageOwner()!=null?view.context().damageOwner():view.context().caster();
        return new DamageSource(source.typeHolder(),owner,owner);
    }
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true))
    private boolean goetyNouveau$wall(LivingEntity target,DamageSource source,float damage){return target.hurt(goetyNouveau$damage(source),damage);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true))
    private boolean goetyNouveau$choke(Entity target,DamageSource source,float damage){return target.hurt(goetyNouveau$damage(source),damage);}
}
