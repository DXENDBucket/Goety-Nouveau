package dev.goetynouveau.tests;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.common.spell.method.MethodSelf;
import dev.arsconflux.api.casting.*;
import dev.arsconflux.api.resource.*;
import dev.arsconflux.api.glyph.GlyphAbilities;
import dev.arsconflux.api.glyph.AbilityTags;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
public final class MobCastingCases {
 public static void run(GameTestHelper h) {
  var source=GoetyConfig.RESOURCE_SOURCE.get();boolean chants=GoetyConfig.CHANTING_ENABLED.get(),cooldowns=GoetyConfig.COOLDOWNS_ENABLED.get();
  try {
   var actor=h.spawn(EntityType.COW,2,8,2);actor.setNoAi(true);actor.setNoGravity(true);
   MobMana.configure(actor,new MobMana.Profile(10000,0));var pool=ArsManaPools.of(actor);
   var glyph=GlyphRegistry.getSpellPart(dev.arsconflux.api.projectile.CarrierRegistry.id("goety_nouveau:glyph_iron_hide"));
   h.assertTrue(glyph!=null&&GlyphAbilities.has(glyph,AbilityTags.DEFENSE),"Built-in Goety buff publishes its capability hints");
   var spell=new Spell(MethodSelf.INSTANCE,glyph);var context=SpellContext.fromEntity(spell,actor,ItemStack.EMPTY);
   GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
   h.assertTrue(!SpellBudgets.estimate(context).possible()&&!SpellBudgets.estimate(context).affordable(),"Mob without soul account cannot bypass soul costs");
   GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.ARS_MANA);GoetyConfig.CHANTING_ENABLED.set(false);GoetyConfig.COOLDOWNS_ENABLED.set(true);
   h.assertTrue(SpellBudgets.estimate(context).affordable(),"Explicit Ars mode uses the mob's native pool");
   boolean cast=ActiveCasts.perform(spell,actor,InteractionHand.MAIN_HAND,ActiveCastSource.CASTER_TOOL,
       ()->new SpellResolver(context).withSilent(true).onCast(ItemStack.EMPTY,actor.level()),false,true);
   h.assertTrue(cast&&pool.current()<pool.maximum()&&!actor.getActiveEffects().isEmpty(),"Actual non-player buff spends native Ars mana and applies its native effect");
   var facts=ActiveCasts.cooldowns(spell,actor,ActiveCastSource.CASTER_TOOL);
   h.assertTrue(facts.stream().anyMatch(q->q.duration()>0&&q.remaining()>0)&&ActiveCasts.rejection(spell,actor,ActiveCastSource.CASTER_TOOL)!=null,
       "Provider commits and publishes a real non-player focus cooldown");
   h.succeed();
  }finally{GoetyConfig.RESOURCE_SOURCE.set(source);GoetyConfig.CHANTING_ENABLED.set(chants);GoetyConfig.COOLDOWNS_ENABLED.set(cooldowns);}
 }
 private MobCastingCases() {}
}
