package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.projectiles.Spike;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.platform.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.*;

/** Native royal spikes and geometry, rooted at the actual Ars hit instead of the caster. */
public final class GoetySpikeCasting {
    private GoetySpikeCasting() {}
    private record Parameters(float potency,int burning,int soulEater) {}
    public static boolean apply(ServerLevel world,CastContext frame,Spell spell,NativeSpellStats defaults,SpellStats ars) {
        var caster=frame.caster(); var hit=frame.target();
        var point=hit instanceof EntityHitResult e ? e.getEntity().position() : hit.getLocation();
        if(!Double.isFinite(point.lengthSqr())) return false;
        boolean gear=WandUtil.enchantedFocus(caster), staff=spell.rightStaff(GoetyResources.staff(caster));
        // Native WandUtil replaces default potency/burning with gear values for enchanted foci.
        var p=new Parameters(Math.max(0,(gear?WandUtil.getPotencyLevel(caster):defaults.potency())+(float)ars.getAmpMultiplier()),
                Math.max(0,(gear?NativeSummonHooks.burning(caster):defaults.burning())+ars.getBuffCount(AugmentBurning.INSTANCE)),
                gear?NativeEffectHooks.soulEater(caster):0);
        int range=Math.max(0,defaults.range()+(gear?WandUtil.getRangeLevel(caster):0));
        var direction=frame.incomingDirection(); if(direction.horizontalDistanceSqr()<1e-10) direction=caster.getLookAngle();
        float angle=(float)Math.atan2(direction.z,direction.x); boolean added=false;
        if(GoetyResources.sneaking(caster)) {
            added|=ring(world,frame,point,angle,5,1.5,(float)Math.PI*.4F,0,0,p);
            added|=ring(world,frame,point,angle,8,2.5,(float)Math.PI*2/8,1.2566371F,3,p);
            if(staff) added|=ring(world,frame,point,angle,11,3.5,(float)Math.PI*4/16,2.5133462F,6,p);
        } else for(int i=0;i<range;i++) {
            added|=spawn(world,frame,point.add(Mth.cos(angle)*1.25*i,0,Mth.sin(angle)*1.25*i),angle,i,p);
            if(staff) for(float side:new float[]{angle+.2F,angle-.2F})
                added|=spawn(world,frame,point.add(Mth.cos(side)*1.25*i,0,Mth.sin(side)*1.25*i),side,i,p);
        }
        if(added) world.playSound(null,point.x,point.y,point.z,SoundEvents.EVOKER_CAST_SPELL,spell.getSoundSource(),1,.5F);
        return added;
    }
    private static boolean ring(ServerLevel world,CastContext frame,Vec3 point,float angle,int count,double radius,float step,float offset,int warmup,Parameters p) {
        boolean added=false;
        for(int i=0;i<count;i++) { float a=angle+i*step+offset; added|=spawn(world,frame,point.add(Mth.cos(a)*radius,0,Mth.sin(a)*radius),a,warmup,p); }
        return added;
    }
    private static boolean spawn(ServerLevel world,CastContext frame,Vec3 point,float angle,int warmup,Parameters p) {
        var pos=BlockPos.containing(point.x,point.y+1,point.z);
        while(pos.getY()>=Math.max(world.getMinBuildHeight(),Mth.floor(point.y)-1)) {
            if(pos.getY()>=world.getMaxBuildHeight() || !world.hasChunkAt(pos)) return false;
            var below=pos.below();
            if(world.getBlockState(below).isFaceSturdy(world,below,Direction.UP)) {
                var shape=world.getBlockState(pos).getCollisionShape(world,pos);
                double surface=shape.isEmpty()?0:shape.max(Direction.Axis.Y);
                var owner=frame.damageOwner()!=null?frame.damageOwner():frame.caster();
                var spike=new Spike(world,point.x,pos.getY()+surface,point.z,angle,warmup,owner);
                spike.setExtraDamage(p.potency()); spike.setBurning(p.burning()); spike.setSoulEater(p.soulEater());
                return world.addFreshEntity(spike);
            }
            pos=pos.below();
        }
        return false;
    }
}
