package dev.goetynouveau.casting;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.spells.geomancy.*;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;

/** Complete native block processing and mine deployment, adapted to actual Ars hits. */
public final class GoetyGeomancyCasting {
 private GoetyGeomancyCasting(){}
 public static boolean apply(EffectFocus focus,ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge){
  var caster=frame.caster();var spell=(Spell)focus.spell();var staff=GoetyResources.staff(caster).copy();var stats=WandUtil.getStats(caster,spell);
  stats.setRadius(Math.max(0,stats.getRadius()+ars.getAoeMultiplier()));
  if(focus==EffectFocus.PULVERIZE){
   if(!(frame.target() instanceof BlockHitResult hit) || !world.isInWorldBounds(hit.getBlockPos()) || !world.hasChunkAt(hit.getBlockPos()))return false;
   var pulverize=(PulverizeSpell)spell;if(!pulverize.rightBlock(world,caster,hit.getBlockPos(),hit.getDirection(),stats))return false;
   return GoetyNativeGeometry.scoped(new GoetyNativeGeometry.View(frame,caster,spell),()->{
    pulverize.blockResult(world,caster,staff,hit.getBlockPos(),hit.getDirection(),stats);return true;
   });
  }
  if(focus!=EffectFocus.SCATTER)return false;
  if(frame.target() instanceof EntityHitResult hit && (!hit.getEntity().isAlive() || hit.getEntity().isRemoved() || hit.getEntity().level()!=world))return false;
  var point=frame.origin();if(!Double.isFinite(point.lengthSqr()) || !world.isInWorldBounds(net.minecraft.core.BlockPos.containing(point)) || !world.hasChunkAt(net.minecraft.core.BlockPos.containing(point)))return false;
  stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));stats.setDuration(Math.max(0,stats.getDuration()+(int)ars.getDurationMultiplier()));
  if(stats.getDuration()<=0)return false;
  boolean[] spawned={false},worked={false};LivingEntity host=caster;
  if(spell.rightStaff(staff)){
   if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity selected) || !GoetyChannelCasting.eligible(world,caster,selected))return false;
   host=selected;
  }
  var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,ignored->spawned[0]=true,stats);
  if(!spell.rightStaff(staff))return GoetyNativeGeometry.scoped(view,()->{spell.SpellResult(world,caster,staff,stats);return spawned[0];});
  int interval=com.Polarice3.Goety.utils.MathHelper.secondsToTicks(4.15f)/14;
  int shots=(int)Math.max(1,((long)spell.castDuration(caster,staff)+interval-1)/interval);int[] phase={0};
  return GoetyChannelCasting.start(focus,world,frame,host,spell,stats,charge,new GoetyChannelCasting.Program(shots,interval,()->GoetyNativeGeometry.scoped(view,()->{
   spawned[0]=false;spell.useSpell(world,caster,staff,phase[0],stats);phase[0]+=interval;worked[0]|=spawned[0];return spawned[0];
  }),()->GoetyNativeGeometry.scoped(view,()->{spell.useParticle(world,view.host(),staff);return null;}),null,null,()->true,()->{if(worked[0])GoetyFocusCooldowns.atLeast(caster,net.minecraft.core.registries.BuiltInRegistries.ITEM.get(focus.focusId()),spell.spellCooldown(caster));}));
 }
}
