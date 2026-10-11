package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.SurgingOrb;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import dev.arsconflux.api.casting.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class RapidFireCases {
    private record Config(boolean chanting,boolean cooldown,boolean creative) {
        static Config setup(){var c=new Config(GoetyConfig.CHANTING_ENABLED.get(),GoetyConfig.COOLDOWNS_ENABLED.get(),GoetyConfig.CREATIVE_COOLDOWN.get());GoetyConfig.CHANTING_ENABLED.set(true);GoetyConfig.COOLDOWNS_ENABLED.set(true);GoetyConfig.CREATIVE_COOLDOWN.set(true);return c;}
        void restore(){GoetyConfig.CHANTING_ENABLED.set(chanting);GoetyConfig.COOLDOWNS_ENABLED.set(cooldown);GoetyConfig.CREATIVE_COOLDOWN.set(creative);}
    }
    private static void prepare(GameTestHelper h,ServerPlayer p){p.setPos(h.absoluteVec(new Vec3(4,45,4)));p.setNoGravity(true);h.getLevel().addNewPlayer(p);p.setGameMode(GameType.SURVIVAL);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));p.setXRot(-90);SEHelper.setSEActive(p,true);SEHelper.setSESouls(p,100000);}
    private static SpellResolver resolver(GameTestHelper h,ServerPlayer p,Spell recipe){return new SpellResolver(new SpellContext(h.getLevel(),recipe,p,new LivingCaster(p){public boolean enoughMana(int n){return true;}public void expendMana(int n){}},p.getMainHandItem()));}
    private static int orbs(GameTestHelper h,ServerPlayer p){return h.getLevel().getEntitiesOfClass(SurgingOrb.class,new AABB(p.position(),p.position()).inflate(32)).size();}
    private static boolean cast(GameTestHelper h,ServerPlayer p,Spell recipe,AtomicInteger calls){return ActiveCasts.perform(recipe,p,InteractionHand.MAIN_HAND,ActiveCastSource.CASTER_TOOL,()->{calls.incrementAndGet();return resolver(h,p,recipe).onCast(p.getMainHandItem(),h.getLevel());},false,true);}
    public static void immediateRepeatedRealShotsAndPayment(GameTestHelper h,ServerPlayer p){
        prepare(h,p);var cfg=Config.setup();
        try {
            var recipe=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.SURGING,com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm.INSTANCE);
            var focus=new ItemStack(com.Polarice3.Goety.common.items.ModItems.SURGING_FOCUS.get());
            SEHelper.addCooldown(p,focus.getItem(),777);var existing=SEHelper.getCooldownInstance(p,focus.getItem());
            int fee=GoetyResources.quote(resolver(h,p,recipe).spellContext,GlyphCatalog.SURGING.focus().spell()).amount();h.assertTrue(fee>0,"Real repeated projectile has a positive native resource cost");SEHelper.setSESouls(p,2*fee);
            var calls=new AtomicInteger();int before=orbs(h,p);
            for(int i=0;i<2;i++)h.assertTrue(cast(h,p,recipe,calls),"Ars rapid-fire launch accepted despite old native focus cooldown");
            h.assertTrue(calls.get()==2 && !ActiveCasts.isChanting(p) && orbs(h,p)==before+2,"Both native surging orbs are emitted immediately without a chant queue");
            h.assertTrue(SEHelper.getSESouls(p)==0 && SEHelper.getCooldownInstance(p,focus.getItem())==existing,"Each actual shot pays, without replacing the ordinary wand cooldown record");
            cast(h,p,recipe,calls);h.assertTrue(calls.get()==3 && orbs(h,p)==before+2 && SEHelper.getSESouls(p)==0,"Zero resources cannot emit another native projectile");
            h.succeed();
        } finally {cfg.restore();p.discard();}
    }
    public static void mixedRecipeAndOtherChargingSpells(GameTestHelper h,ServerPlayer p){
        prepare(h,p);var cfg=Config.setup();
        try {
            var provider=new GoetyActiveCastProvider();var rapid=GlyphCatalog.SURGING.focus().spell();var steam=GlyphCatalog.STEAM.focus().spell();var rain=GlyphCatalog.ARROW_RAIN.focus().spell();
            h.assertTrue(GoetyActiveCastProvider.preparationTicks(rapid,p,ItemStack.EMPTY)==0 && !GoetyActiveCastProvider.rapidFire(steam) && !GoetyActiveCastProvider.rapidFire(rain),"Only unlimited-fire projectile payloads are exempt, not limited bursts or sustained rain");
            h.assertTrue(GoetyActiveCastProvider.preparationTicks(steam,p,ItemStack.EMPTY)==((com.Polarice3.Goety.api.magic.IChargingSpell)steam).castUp(p,ItemStack.EMPTY),"Steam retains its native charge-up");
            var recipe=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.SURGING,GlyphCatalog.IRON_HIDE);var request=new ActiveCastRequest(recipe,p,ActiveCastSource.CASTER_TOOL);var ticks=provider.chantTicks(request);
            int remaining=GoetyActiveCastProvider.preparationTicks(GlyphCatalog.IRON_HIDE.focus().spell(),p,ItemStack.EMPTY);
            h.assertTrue(remaining>0 && ticks.equals(List.of(0,remaining)) && ChantDurations.combine(ticks,false)==remaining && ChantDurations.combine(ticks,true)==remaining,"Other written focus still determines both maximum and sum preparations");
            var rapidFocus=com.Polarice3.Goety.common.items.ModItems.SURGING_FOCUS.get();var ordinaryFocus=com.Polarice3.Goety.common.items.ModItems.IRON_HIDE_FOCUS.get();
            SEHelper.addCooldown(p,rapidFocus,777);SEHelper.addCooldown(p,ordinaryFocus,888);var existing=SEHelper.getCooldownInstance(p,rapidFocus);
            var calls=new AtomicInteger();h.assertTrue(!cast(h,p,recipe,calls) && calls.get()==0,"Mixed recipe still respects the ordinary focus cooldown");
            SEHelper.getCooldowns(p).remove(ordinaryFocus);int before=orbs(h,p);
            var emitted=new AtomicInteger(before);
            h.assertTrue(ActiveCasts.perform(recipe,p,InteractionHand.MAIN_HAND,ActiveCastSource.CASTER_TOOL,()->{
                calls.incrementAndGet();boolean result=resolver(h,p,recipe).onCast(p.getMainHandItem(),h.getLevel());
                // Count at dispatch: the native projectile may already travel out of the test volume afterward.
                emitted.set(orbs(h,p));return result;
            },false,true) && calls.get()==0 && ActiveCasts.isChanting(p),"Other focus still queues the mixed recipe before firing");
            h.runAfterDelay(remaining+3,()->{
                try {
                    h.assertTrue(calls.get()==1 && !ActiveCasts.isChanting(p) && emitted.get()>before,"Mixed cast emits a real native orb after the remaining preparation");
                    h.assertTrue(SEHelper.isOnCooldown(p,new ItemStack(ordinaryFocus)) && SEHelper.getCooldownInstance(p,rapidFocus)==existing,"Mixed release writes only the non-exempt focus cooldown");
                    h.succeed();
                } finally {cfg.restore();p.discard();}
            });
        } catch (Throwable e) {cfg.restore();p.discard();throw e;}
    }
}
