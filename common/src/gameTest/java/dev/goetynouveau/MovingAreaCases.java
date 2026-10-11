package dev.goetynouveau;
import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import java.util.*;
public final class MovingAreaCases {
 private static Mob cow(GameTestHelper h,Vec3 pos){var e=EntityType.COW.create(h.getLevel());e.setNoAi(true);e.setNoGravity(true);e.setPos(pos);h.getLevel().addFreshEntity(e);return e;}
 private static void cast(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,LivingEntity owner,Vec3 direction,boolean success,AbstractAugment... augments){
  var spell=new Spell(MethodTouch.INSTANCE,glyph);for(var a:augments)spell=spell.add(a);
  var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));resolver.spellContext.nextPart();
  CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,hit,null).withDamageOwner(owner).withIncomingDirection(direction));
  int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(resolver.spellContext,glyph.focus().spell(),List.of(augments)).amount();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(success&&!p.isCreative()?fee:0),"One successful native area batch pays one charge");
 }
 private static BlockHitResult point(Vec3 pos){return new BlockHitResult(pos,net.minecraft.core.Direction.UP,net.minecraft.core.BlockPos.containing(pos),false);}
 public static void cycloneTargetCloneAndFriends(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var owner=RainStormCases.host(h,p);var stale=owner.getTarget();var target=cow(h,RainStormCases.scene(h).add(6,0,0));
   cast(h,p,GlyphCatalog.CYCLONE,new EntityHitResult(target),owner,new Vec3(1,0,0),true,AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE,AugmentExtendTime.INSTANCE);
   var area=RainStormCases.entities(h,Cyclone.class).get(0);
   h.assertTrue(area.getTrueOwner()==owner && area.getTarget()==target && area.getTarget()!=stale,"Explicit owner does not override Ars target with its AI target");
   h.assertTrue(area.getTotalLife()==1200 && area.getDamage()==1 && area.getSize()==2,"Native base stats plus Ars augments");
   h.assertTrue(area.position().distanceTo(target.getEyePosition().add(.5,-.2,0))<.001,"Native half-block launch offset at the Ars impact");
   p.setPos(area.position());float hp=p.getHealth();var motion=p.getDeltaMovement();area.suckInMobs(p);
   h.assertTrue(p.getHealth()==hp && p.getDeltaMovement().equals(motion) && area.getSpun()==0,"Original source ally excluded from native pull and damage");
   area.boltSpeed=3;area.fakeRemove(0,0,1);
   var next=RainStormCases.entities(h,Cyclone.class).stream().filter(e->!e.isRemoved()).findFirst().orElseThrow();
   h.assertTrue(next!=area && area.isRemoved() && next.getTarget()==target && next.boltSpeed==3 && GoetyAreaProvenance.friendly(next,p),"Actual native retarget replacement preserves provenance, target and velocity");
   target.invulnerableTime=0;next.hurtMobs(target);h.assertTrue(target.getHealth()<target.getMaxHealth() && target.getLastHurtByMob()==owner,"Actual native damage belongs to explicit owner");
   var saved=new net.minecraft.nbt.CompoundTag();next.saveWithoutId(saved);var loaded=new Cyclone(h.getLevel(),owner,0,0,1);loaded.load(saved);
   h.assertTrue(GoetyAreaProvenance.friendly(loaded,p),"Provenance survives native entity save/load");
   next.setTotalLife(next.getLifespan());next.tick();h.assertTrue(next.isRemoved(),"Native area lifetime removes entity");h.succeed();
  }finally{p.discard();RainStormCases.clear(h,p);}
 }
 public static void nativeFireVariantAndSourceGear(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var inventory=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new);var items=List.of(ModItems.UNHOLY_ROBE.get(),ModItems.UNHOLY_HAT.get());int n=0;
   for(var handler:inventory.getCurios().values())for(int i=0;i<handler.getStacks().getSlots();i++)handler.getStacks().setStackInSlot(i,n<items.size()?new ItemStack(items.get(n++)):ItemStack.EMPTY);
   h.assertTrue(CuriosFinder.hasUnholySet(p),"Original source wears actual full Unholy set");
   var owner=cow(h,RainStormCases.scene(h).add(-10,0,0));var target=cow(h,RainStormCases.scene(h));
   cast(h,p,GlyphCatalog.CYCLONE,new EntityHitResult(target),owner,new Vec3(0,0,1),true);
   var area=RainStormCases.entities(h,FireTornado.class).get(0);h.assertTrue(!CuriosFinder.hasUnholySet(owner),"Explicit owner has no copied gear");
   area.hurtMobs(target);h.assertTrue(!target.getActiveEffects().isEmpty() && target.getLastHurtByMob()==owner,"Native hellfire/Burn Hex branch reads source gear and attributes damage to owner");
   area.fakeRemove(0,1,0);var next=RainStormCases.entities(h,FireTornado.class).stream().filter(e->!e.isRemoved()).findFirst().orElseThrow();
   h.assertTrue(GoetyAreaProvenance.source(next,null)==p,"Native fiery replacement preserves source");h.succeed();
  }finally{var inventory=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new);for(var handler:inventory.getCurios().values())for(int i=0;i<handler.getStacks().getSlots();i++)handler.getStacks().setStackInSlot(i,ItemStack.EMPTY);p.discard();RainStormCases.clear(h,p);}
 }
 public static void tidalNativeTravelDamageAndStaff(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var owner=cow(h,RainStormCases.scene(h).add(-10,0,0));var target=cow(h,RainStormCases.scene(h));
   cast(h,p,GlyphCatalog.TIDAL,point(target.position()),owner,new Vec3(1,0,0),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
   var wave=RainStormCases.entities(h,TidalSurge.class).get(0);var origin=wave.position();
   h.assertTrue(wave.getOwner()==owner && wave.getLifespan()==20 && wave.getWaveScale()==1.5f && Math.abs(wave.getYRot()+90)<.001,"Native entity, owner, time, scale and impact direction");
   p.setPos(origin);float hp=p.getHealth();var motion=p.getDeltaMovement();wave.tick();
   h.assertTrue(p.getHealth()==hp && p.getDeltaMovement().equals(motion),"Original source friend excluded from native wave damage and push");
   h.assertTrue(target.getHealth()<target.getMaxHealth() && target.getLastHurtByMob()==owner && wave.isSlamming(),"Native drench damage, source and slam collision");
   target.discard();p.setPos(origin.add(-20,0,0));for(int i=0;i<4;i++)wave.tick();h.assertTrue(wave.getX()>origin.x,"Native wave acceleration and motion retained");
   for(int i=0;i<80&&!wave.isRemoved();i++)wave.tick();h.assertTrue(wave.isRemoved(),"Native slam/shrink expiry retained");
   p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));
   cast(h,p,GlyphCatalog.TIDAL,point(origin.add(0,0,8)),owner,new Vec3(0,0,1),true);
   var waves=RainStormCases.entities(h,TidalSurge.class).stream().filter(e->!e.isRemoved()).toList();
   h.assertTrue(waves.size()==3 && waves.stream().map(Entity::getYRot).sorted().toList().equals(List.of(-10f,0f,10f)),"Native Abyss staff triple wave keeps symmetric spread and one batch charge");h.succeed();
  }finally{p.discard();RainStormCases.clear(h,p);}
 }
 public static void rejectedCastsAndCreative(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var hit=point(RainStormCases.scene(h));SEHelper.setSESouls(p,0);
   cast(h,p,GlyphCatalog.CYCLONE,hit,p,new Vec3(0,0,1),false);cast(h,p,GlyphCatalog.TIDAL,hit,p,new Vec3(0,0,1),false);
   h.assertTrue(RainStormCases.entities(h,AbstractCyclone.class).isEmpty() && RainStormCases.entities(h,TidalSurge.class).isEmpty(),"Resource rejection creates no areas");
   p.setGameMode(GameType.CREATIVE);cast(h,p,GlyphCatalog.CYCLONE,hit,p,new Vec3(0,1,0),true);
   var cyclone=RainStormCases.entities(h,Cyclone.class).get(0);var start=cyclone.position();for(int i=0;i<3;i++)cyclone.tick();h.assertTrue(cyclone.getY()>start.y && Math.abs(cyclone.getX()-start.x)<.001,"Vertical carrier direction preserved for cyclone");
   cast(h,p,GlyphCatalog.TIDAL,hit,p,new Vec3(0,0,1),false,AugmentDurationDown.INSTANCE);
   var gone=cow(h,RainStormCases.scene(h));gone.discard();cast(h,p,GlyphCatalog.CYCLONE,new EntityHitResult(gone),p,new Vec3(0,0,1),false);
   h.assertTrue(RainStormCases.entities(h,TidalSurge.class).isEmpty(),"Zero duration and removed targets rejected without spawning");h.succeed();
  }finally{p.discard();RainStormCases.clear(h,p);}
 }
}
