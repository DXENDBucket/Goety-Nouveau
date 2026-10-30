package dev.goetynouveau;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.damage.SpellPower;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import java.util.*;

/** Real Iron and Goety native damage, through the public Ars entrypoint; no Iron compile dependency. */
public final class PowerCases {
 private static LivingEntity cow(GameTestHelper h,Vec3 pos) {
  var cow=EntityType.COW.create(h.getLevel());cow.setPos(pos);cow.setNoAi(true);cow.setNoGravity(true);
  cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);cow.setHealth(1000);h.getLevel().addFreshEntity(cow);return cow;
 }
 private static float projectile(GameTestHelper h,ServerPlayer p,AbstractSpellPart glyph,Vec3 scene) {
  p.setPos(scene);p.setYRot(-90);p.setXRot(0);
  p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(glyph.getGlyph()));glyph.getGlyph().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
  var target=cow(h,scene.add(5,p.getEyeHeight()-.1-.7,0));
  try {
   var resolver=new SpellResolver(SpellContext.fromEntity(new Spell(MethodProjectile.INSTANCE,glyph),p,ItemStack.EMPTY));
   h.assertTrue(resolver.onCast(ItemStack.EMPTY,h.getLevel()),"Native provider projectile launches");
   var shots=h.getLevel().getEntities(p,p.getBoundingBox().inflate(20),e->e instanceof Projectile shot&&shot.getOwner()==p);
   h.assertTrue(shots.size()==1,"One native projectile, without duplicate Ars carrier");
   try {
    for(int i=0;i<40&&target.getHealth()==1000;i++)for(var shot:shots)if(!shot.isRemoved()){++shot.tickCount;shot.tick();ExecutionSessions.tick();}
    float damage=1000-target.getHealth();h.assertTrue(damage>0,"Real native collision damages the target");return damage;
   }finally{shots.forEach(Entity::discard);ExecutionSessions.tick();}
  }finally{target.discard();}
 }
 public static void sharedNativeDamage(GameTestHelper h,ServerPlayer p,java.util.function.DoubleConsumer general) {
  var scene=h.absoluteVec(new Vec3(2,3,2));scene=new Vec3(scene.x,h.getLevel().getMaxBuildHeight()-20,scene.z);
  p.setGameMode(GameType.CREATIVE);p.setPos(scene);h.getLevel().addNewPlayer(p);
  var iron=GlyphRegistry.getSpellPart(CarrierRegistry.id("irons_nouveau:glyph_firebolt"));
  h.assertTrue(iron!=null,"The optional real Iron provider is installed for coexistence verification");
  try {
   for(var glyph:List.of(GlyphCatalog.SOUL_BOLT,iron)) {
    general.accept(0);float reference=projectile(h,p,glyph,scene);
    general.accept(.5);float boosted=projectile(h,p,glyph,scene);
    h.assertTrue(Math.abs(boosted-reference*1.5)<.003,"Exactly one +50% Confluence multiplier on "+glyph.getRegistryName()+": "+reference+" -> "+boosted);
   }
   // Immediate native effect damage uses the same path, without a physical projectile.
   general.accept(0);float base=effect(h,p,scene);
   general.accept(.5);float boosted=effect(h,p,scene);
   h.assertTrue(Math.abs(boosted-base*1.5)<.003,"Native Goety effect is scaled once");
   h.assertTrue(SpellPower.current()==null,"Provider callbacks do not leak damage context");h.succeed();
  }finally{general.accept(0);p.discard();}
 }
 private static float effect(GameTestHelper h,ServerPlayer p,Vec3 scene) {
  var target=cow(h,scene.add(4,0,0));p.setYRot(-90);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
  try {
   var resolver=new SpellResolver(SpellContext.fromEntity(new Spell(MethodTouch.INSTANCE,GlyphCatalog.THUNDERBOLT),p,ItemStack.EMPTY));
   resolver.spellContext.nextPart();resolver.onResolveEffect(h.getLevel(),new EntityHitResult(target));
   float damage=1000-target.getHealth();h.assertTrue(damage>0,"Native Thunderbolt damages");return damage;
  }finally{target.discard();}
 }
}
