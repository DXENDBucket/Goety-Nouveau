package dev.goetynouveau;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.spells.geomancy.TemblorClapSpell;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.*;
import dev.goetynouveau.casting.GoetyResources;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

@GameTestHolder("goety_nouveau_temblor") @PrefixGameTestTemplate(false)
public final class TemblorGameTests {
 private static void inventory(){
  var covered=new java.util.HashSet<net.minecraft.resources.ResourceLocation>();
  for(var part:GlyphCatalog.ALL){if(part instanceof dev.goetynouveau.glyph.GoetyEffectGlyph g)covered.add(g.focus().focusId());if(part instanceof dev.goetynouveau.glyph.GoetyBoltGlyph g)covered.add(dev.arsconflux.api.projectile.CarrierRegistry.id("goety:"+g.focus().focus));}
  var missing=new java.util.TreeMap<String,String>();int registered=0;
  for(var item:net.minecraft.core.registries.BuiltInRegistries.ITEM){var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);if(!id.getNamespace().equals("goety"))continue;
   var spell=dev.goetynouveau.api.FocusSpells.read(new net.minecraft.world.item.ItemStack(item));if(spell.isPresent()){registered++;if(!covered.contains(id))missing.put(id.toString(),spell.get().getClass().getName());}}
  System.out.println("GOETY_NATIVE_FOCUS_INVENTORY registered="+registered+" missing="+missing);
 }

 private static ServerPlayer player(GameTestHelper h){return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"TemblorSource"));}
 private static void prepare(GameTestHelper h,ServerPlayer p){RainStormCases.prepare(h,p);p.setOnGround(false);var center=BlockPos.containing(RainStormCases.scene(h));for(int x=-32;x<=32;x++)for(int z=-16;z<=16;z++)h.getLevel().setBlock(center.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);}
 private static LivingEntity cow(GameTestHelper h,Vec3 pos){var c=EntityType.COW.create(h.getLevel());c.setNoAi(true);c.setNoGravity(true);c.setPos(pos);c.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);c.setHealth(200);h.getLevel().addFreshEntity(c);return c;}
 private static void clean(GameTestHelper h,ServerPlayer p){p.discard();RainStormCases.clear(h,p);}
 private static void cast(GameTestHelper h,ServerPlayer p,HitResult hit,LivingEntity owner,boolean paid,AbstractAugment... augments){
  var spell=new Spell(MethodTouch.INSTANCE,GlyphCatalog.TEMBLOR_CLAP);for(var a:augments)spell=spell.add(a);
  var context=new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY);var resolver=new SpellResolver(context);context.nextPart();CastContexts.bind(context,CastContext.of(context,hit,null).withDamageOwner(owner));
  int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(context,GlyphCatalog.TEMBLOR_CLAP.focus().spell(),List.of(augments)).amount();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(paid&&!p.isCreative()?fee:0),"Actual one successful wave commits original source fee");
 }
 private static HitResult hit(Vec3 pos){return new BlockHitResult(pos,Direction.UP,BlockPos.containing(pos).below(),false);}
 private static void restore(LivingEntity e){e.setHealth(e.getMaxHealth());e.invulnerableTime=0;e.removeAllEffects();e.setDeltaMovement(Vec3.ZERO);}
 @GameTest(template="empty",batch="temblor_0",timeoutTicks=100) public static void nativeDamageStunAlliesAndAttribution(GameTestHelper h){inventory();
  var p=player(h);prepare(h,p);try{
   h.assertTrue(GlyphCatalog.TEMBLOR_CLAP!=null && GlyphCatalog.TEMBLOR_CLAP.focus().spell() instanceof TemblorClapSpell,"Real pinned Forge native focus binding");
   var point=RainStormCases.scene(h);var enemy=cow(h,point.add(1,0,0));var friendly=cow(h,point.add(0,0,1));var owner=cow(h,point.add(0,0,-12));var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("clap_"+UUID.randomUUID().toString().substring(0,7));board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(friendly.getScoreboardName(),team);
   var start=p.position();cast(h,p,hit(point),owner,true);var stun=enemy.getEffect(GoetyEffects.STUNNED.get());
   h.assertTrue(enemy.getHealth()<200 && stun!=null && stun.getDuration()==60,"Complete native explosion damage plus three-second stunned effect");
   h.assertTrue(enemy.getLastDamageSource().getEntity()==owner && enemy.getLastDamageSource().is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK),"Native mob attack damage type with explicit original damage owner");
   h.assertTrue(friendly.getHealth()==200 && !friendly.hasEffect(GoetyEffects.STUNNED.get()) && p.position().distanceToSqr(start)<.001,"Native alliance filter and original source body remain unchanged");h.succeed();
  }finally{clean(h,p);}
 }
 @GameTest(template="empty",batch="temblor_1",timeoutTicks=100) public static void fullNativeStaffAndSourceParameters(GameTestHelper h){
  var p=player(h);prepare(h,p);try{
   var point=RainStormCases.scene(h);var start=p.position();var enemy=cow(h,point.add(3.5,0,0));var farther=cow(h,point.add(5,0,0));p.setOnGround(true);p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
   var nativeSpell=new TemblorClapSpell();var stats=WandUtil.getStats(p,nativeSpell);stats.setPotency(stats.getPotency()+1);stats.setRadius(stats.getRadius()+1);p.setPos(point);h.getLevel().random.setSeed(9);nativeSpell.SpellResult(h.getLevel(),p,GoetyResources.staff(p),stats);float hp=enemy.getHealth(),farHp=farther.getHealth();var impulse=enemy.getDeltaMovement();h.assertTrue(hp<200,"Actual native ground/staff/radius reference has damage");
   restore(enemy);restore(farther);p.setPos(start);h.getLevel().random.setSeed(9);cast(h,p,hit(point),p,true,AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE);
   h.assertTrue(enemy.getHealth()==hp && farther.getHealth()==farHp && enemy.getDeltaMovement().distanceToSqr(impulse)<.001,"Complete native staff/ground radius and random damage/knockback with source stats plus Ars modifiers once");h.succeed();
  }finally{clean(h,p);}
 }
 @GameTest(template="empty",batch="temblor_2",timeoutTicks=100) public static void resourceCreativeAndTargetBounds(GameTestHelper h){
  var p=player(h);prepare(h,p);try{
   var point=RainStormCases.scene(h);var enemy=cow(h,point);SEHelper.setSESouls(p,0);cast(h,p,new EntityHitResult(enemy),p,false);h.assertTrue(enemy.getHealth()==200,"Empty source cannot create native wave");
   p.setGameMode(GameType.CREATIVE);cast(h,p,new EntityHitResult(enemy),p,true);h.assertTrue(enemy.getHealth()<200,"Creative free native entity-centered wave");restore(enemy);p.setGameMode(GameType.SURVIVAL);SEHelper.setSESouls(p,10000);
   var unloaded=new BlockHitResult(new Vec3(1000000,point.y,1000000),Direction.UP,new BlockPos(1000000,(int)point.y,1000000),false);
   var context=new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.TEMBLOR_CLAP),p,LivingCaster.from(p),ItemStack.EMPTY);var resolver=new SpellResolver(context);context.nextPart();int before=SEHelper.getSESouls(p);GlyphCatalog.TEMBLOR_CLAP.onResolve(unloaded,h.getLevel(),p,new SpellStats.Builder().build(),context,resolver);h.assertTrue(SEHelper.getSESouls(p)==before,"Addon unloaded position rejects before native block search");
   cast(h,p,hit(point.add(0,h.getLevel().getMaxBuildHeight(),0)),p,false);enemy.discard();cast(h,p,new EntityHitResult(enemy),p,false);h.succeed();
  }finally{clean(h,p);}
 }
 @GameTest(template="empty",batch="temblor_3",timeoutTicks=100) public static void nativeWandIsolationAfterArsScope(GameTestHelper h){
  var p=player(h);prepare(h,p);try{
   var point=RainStormCases.scene(h);var remote=cow(h,point.add(1,0,0));var local=cow(h,p.position().add(1,0,0));cast(h,p,hit(point),p,true);h.assertTrue(remote.getHealth()<200 && local.getHealth()==200,"Ars result uses selected point rather than caster body");restore(remote);restore(local);
   var nativeSpell=new TemblorClapSpell();nativeSpell.SpellResult(h.getLevel(),p,ItemStack.EMPTY,WandUtil.getStats(p,nativeSpell));h.assertTrue(local.getHealth()<200 && remote.getHealth()==200,"After scope native wand remains source-centered with native visual/damage callback");h.succeed();
  }finally{clean(h,p);}
 }
}
