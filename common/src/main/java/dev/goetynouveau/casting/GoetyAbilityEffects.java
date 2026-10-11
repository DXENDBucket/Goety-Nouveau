package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SAddBrewParticlesPacket;
import com.Polarice3.Goety.client.particles.TeleportInShockwaveParticleOption;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.*;
import dev.goetynouveau.mixin.ProjectileHitAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import java.util.*;

/** MIT native brews/projectiles own their rules and visuals. No dummy mobs or players. */
public final class GoetyAbilityEffects {
    private GoetyAbilityEffects() {}
    public static boolean apply(EffectFocus focus,ServerLevel world,CastContext frame,SpellStats stats) {
        var point=frame.target() instanceof EntityHitResult hit?hit.getEntity().position():frame.target().getLocation();
        var pos=BlockPos.containing(point);
        if(!world.hasChunkAt(pos)) return false;
        var owner=frame.damageOwner()!=null?frame.damageOwner():frame.caster();
        if(focus==EffectFocus.SNARELING_TRAP) {
            var trap=frame.target() instanceof EntityHitResult hit
                    ? new SnarelingGoop(world,owner,hit.getEntity()) : new SnarelingGoop(world,owner,pos);
            trap.setLifeSpan(Math.max(1,60+20*(int)stats.getDurationMultiplier()));
            return world.addFreshEntity(trap);
        }
        if(focus==EffectFocus.BERSERK_CLOUD) {
            var fungus=new BerserkFungus(frame.caster(),world);fungus.setPos(point);fungus.setOwner(owner);
            if(!world.addFreshEntity(fungus)) return false;
            // Run the complete original impact: splash, entity-event particles and sound.
            ((ProjectileHitAccess)fungus).goetyNouveau$hit(frame.target());
            return true;
        }
        if(focus==EffectFocus.BONE_PULL) {
            double radius=Math.max(.1,3.5+stats.getAoeMultiplier());
            boolean applied=false;
            for(var target:world.getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(radius,1,radius))) {
                if(target==frame.caster() || !target.isAlive() || MobUtil.areAllies(frame.caster(),target)) continue;
                var direction=target.position().subtract(point).normalize();
                MobUtil.pull(target,direction.x,direction.y,direction.z);
                target.playSound(com.Polarice3.Goety.init.ModSounds.GRAVITY.get());
                world.sendParticles(new TeleportInShockwaveParticleOption(),target.getX(),target.getY()+.25,target.getZ(),0,0,0,0,.5);
                applied=true;
            }
            return applied;
        }
        if(frame.target() instanceof BlockHitResult hit) pos=hit.getBlockPos();
        var ability=AbilitySpells.ability(focus.glyph).orElseThrow();
        var brew=ability.brewEffect();
        int amplifier=Math.max(0,(int)stats.getAmpMultiplier()),area=Math.max(0,(int)stats.getAoeMultiplier());
        // Native helpers may floor Y / replace air; choose the same surface location as a landed bottle.
        if(focus==EffectFocus.THORN_TRAP) {
            if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target) || !target.isAlive()) return false;
            brew.applyEntityEffect(target,owner,owner,amplifier);
        } else brew.applyBlockEffect(world,pos,owner,amplifier,area);
        var bottle=BrewUtils.setCustomEffects(new ItemStack(ModItems.SPLASH_BREW.get()),List.of(),List.of(new BrewEffectInstance(brew,1,amplifier)));
        BrewUtils.setAreaOfEffect(bottle,area);
        // The native splash's instant flag refers to vanilla potion effects; this bottle has none.
        ModNetwork.sendToALL(new SAddBrewParticlesPacket(bottle,pos,false,brew.getColor()));
        return true;
    }
}
