package dev.goetynouveau;

import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal;
import dev.arsconflux.api.casting.*;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.config.ConfluxConfig;
import dev.goetynouveau.casting.GoetyActiveCastProvider;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ActiveCastingCases {
    private static final InteractionHand HAND = InteractionHand.MAIN_HAND;
    private static final class Provider implements ActiveCastProvider {
        final Spell recipe; final int ticks;
        int releases, starts, ends; boolean enabled=true, allowed=true;
        Provider(Spell recipe,int ticks) { this.recipe=recipe; this.ticks=ticks; }
        public boolean supports(Spell spell) { return spell == recipe; }
        public boolean chantingEnabled(ActiveCastRequest request) { return enabled; }
        public List<Integer> chantTicks(ActiveCastRequest request) { return List.of(ticks); }
        public boolean allowed(ActiveCastRequest request) { return allowed; }
        public void released(ActiveCastRequest request,List<Spell> recipes) { releases++; }
        public void chantProgress(ActiveCastRequest request,UUID token,int total,int remaining) { if(remaining==total) starts++; if(remaining==0) ends++; }
    }
    private static Provider provider(Spell recipe,int ticks) {
        var p=new Provider(recipe,ticks); ActiveCasts.register(CarrierRegistry.id("casting_test:p"+UUID.randomUUID().toString().replace("-","")),p); return p;
    }
    public static void mixedPreparationAndNestedEntry(GameTestHelper h,ServerPlayer cow) {
        cow.setGameMode(GameType.CREATIVE); cow.setItemInHand(HAND,new ItemStack(Items.STICK));
        var recipe=new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE); var a=provider(recipe,2); var b=provider(recipe,4);
        var calls=new AtomicInteger();
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),recipe,cow,new LivingCaster(cow) {
            public boolean enoughMana(int amount) { return true; } public void expendMana(int amount) {}
        },cow.getMainHandItem()));
        boolean queued=ActiveCasts.perform(recipe,cow,HAND,ActiveCastSource.CASTER_TOOL,()->
                ActiveCasts.perform(recipe,cow,HAND,ActiveCastSource.CASTER_TOOL,()-> { calls.incrementAndGet(); return resolver.onCast(cow.getMainHandItem(),h.getLevel()); },false,true),false,true);
        h.assertTrue(queued && calls.get()==0 && a.starts==1 && b.starts==1,"Both providers prepare in one shared queue");
        ActiveCasts.perform(recipe,cow,HAND,ActiveCastSource.CASTER_TOOL,()-> { calls.addAndGet(100); return true; },false,true);
        h.runAfterDelay(7,()-> {
            h.assertTrue(calls.get()==1 && !ActiveCasts.isChanting(cow),"Longest preparation releases once; nested active entry cannot start a second chant");
            h.assertTrue(a.releases==1 && b.releases==1 && a.ends==1 && b.ends==1,"One acknowledged dispatch commits both providers and closes both native HUD tokens");
            // A subsequent direct trigger is outside the active transaction.
            ActiveCasts.dispatched(resolver,true);
            h.assertTrue(a.releases==1 && b.releases==1,"Deferred impacts cannot commit or recheck the launch cooldown");
            cow.discard(); h.succeed();
        });
    }
    public static void cancellationDeniedReleaseAndPureArs(GameTestHelper h) {
        var cow=h.spawn(EntityType.COW,2,2,2); cow.setNoAi(true); cow.setNoGravity(true); cow.setItemInHand(HAND,new ItemStack(Items.STICK));
        var recipe=new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE); var p=provider(recipe,3); var calls=new AtomicInteger();
        ActiveCasts.perform(recipe,cow,HAND,ActiveCastSource.CASTER_TOOL,()-> { calls.incrementAndGet(); return true; },false,true);
        cow.setItemInHand(HAND,new ItemStack(Items.DIRT)); ActiveCasts.tick();
        h.assertTrue(!ActiveCasts.isChanting(cow) && calls.get()==0 && p.ends==1 && p.releases==0,"Changing tool cancels without release or cooldown");
        p.allowed=false;
        h.assertTrue(!ActiveCasts.perform(recipe,cow,HAND,ActiveCastSource.CASTER_TOOL,()->true,false,true),"Native cooldown denial precedes the queue");
        var pure=new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE);
        h.assertTrue(ActiveCasts.perform(pure,cow,HAND,ActiveCastSource.CASTER_TOOL,()-> { calls.incrementAndGet(); return true; },false,true) && calls.get()==1,
                "Ordinary Ars remains immediate despite another provider's cooldown");
        p.allowed=true;
        ActiveCasts.perform(recipe,cow,HAND,ActiveCastSource.CASTER_TOOL,()-> { calls.incrementAndGet(); return true; },false,true);
        p.allowed=false;
        h.runAfterDelay(6,()-> {
            h.assertTrue(calls.get()==1 && p.ends==2 && p.releases==0,"A native cooldown acquired during preparation denies release without committing another cooldown");
            cow.discard(); h.succeed();
        });
    }
    public static void nativeGoetyCooldownAndChargeBoundary(GameTestHelper h,ServerPlayer p) {
        p.setGameMode(GameType.CREATIVE); p.setItemInHand(HAND,new ItemStack(Items.STICK)); p.setXRot(-90);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,10000);
        var oldChant=GoetyConfig.CHANTING_ENABLED.get(); var oldCooldown=GoetyConfig.COOLDOWNS_ENABLED.get();
        try {
            GoetyConfig.CHANTING_ENABLED.set(false); GoetyConfig.COOLDOWNS_ENABLED.set(true);
            var recipe=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.SOUL_BOLT,GlyphCatalog.SOUL_BOLT);
            // Use one payload occurrence in the actual resolver; duplicates below only test cooldown-key deduplication.
            var actual=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.SOUL_BOLT);
            var resolver=new SpellResolver(SpellContext.fromEntity(actual,p,p.getMainHandItem()));
            h.assertTrue(ActiveCasts.execute(recipe,p,ActiveCastSource.CASTER_TOOL,()->resolver.onCast(p.getMainHandItem(),h.getLevel()),false),"Active native projectile dispatch succeeds");
            var focus=new ItemStack(com.Polarice3.Goety.common.items.ModItems.SOUL_BOLT_FOCUS.get());
            h.assertTrue(SEHelper.isOnCooldown(p,focus) && !ActiveCasts.allowed(actual,p,ActiveCastSource.CASTER_TOOL),"Creative follows native Goety focus cooldown, shared across recipes");
            GoetyConfig.COOLDOWNS_ENABLED.set(false);
            h.assertTrue(ActiveCasts.allowed(actual,p,ActiveCastSource.CASTER_TOOL),"Independent cooldown switch restores immediate eligibility");
            var spell=GlyphCatalog.STEAM.focus().spell();
            h.assertTrue(spell instanceof com.Polarice3.Goety.api.magic.IChargingSpell,"Steam is a native charging focus");
            int ticks=GoetyActiveCastProvider.preparationTicks(spell,p,ItemStack.EMPTY);
            h.assertTrue(ticks==Math.max(0,((com.Polarice3.Goety.api.magic.IChargingSpell)spell).castUp(p,ItemStack.EMPTY)) && ticks<72000,
                    "Charging preparation uses castUp, never the 72000-tick item-use duration");
            h.assertTrue(ChantDurations.combine(List.of(20,40,40),false)==40 && ChantDurations.combine(List.of(20,40,40),true)==100,
                    "Mixed maximum and sum count written occurrences; split branches are absent from this quote");
            h.assertTrue(ChantDurations.combine(List.of(Integer.MAX_VALUE,40),true)==Integer.MAX_VALUE,"Large timing sums saturate safely");
            h.succeed();
        } finally { GoetyConfig.CHANTING_ENABLED.set(oldChant); GoetyConfig.COOLDOWNS_ENABLED.set(oldCooldown); p.discard(); }
    }
    public static void nativeGoetyPreparationRelease(GameTestHelper h,ServerPlayer p,java.util.function.Consumer<Spell> cast) {
        p.setGameMode(GameType.CREATIVE); p.setItemInHand(HAND,new ItemStack(Items.STICK)); p.setHealth(1);
        var oldChant=GoetyConfig.CHANTING_ENABLED.get(); var oldCooldown=GoetyConfig.COOLDOWNS_ENABLED.get();
        GoetyConfig.CHANTING_ENABLED.set(true); GoetyConfig.COOLDOWNS_ENABLED.set(true);
        var recipe=new Spell(MethodSelf.INSTANCE,GlyphCatalog.IRON_HIDE);
        int ticks=GoetyActiveCastProvider.preparationTicks(GlyphCatalog.IRON_HIDE.focus().spell(),p,ItemStack.EMPTY);
        h.assertTrue(ticks>0 && ticks<200,"Native Iron Hide has a practical preparation");
        cast.accept(recipe);
        h.assertTrue(ActiveCasts.isChanting(p) && !p.hasEffect(dev.goetynouveau.platform.NativeEffectHooks.ironHide(1,0).getEffect()),"Real Ars caster defers Goety healing");
        h.runAfterDelay(ticks+3,()-> {
            try {
                h.assertTrue(!ActiveCasts.isChanting(p) && p.hasEffect(dev.goetynouveau.platform.NativeEffectHooks.ironHide(1,0).getEffect()),"Real active entry releases the native effect after its preparation");
                h.assertTrue(SEHelper.isOnCooldown(p,new ItemStack(com.Polarice3.Goety.common.items.ModItems.IRON_HIDE_FOCUS.get())),
                        "Native Goety cooldown begins on actual release");
                h.succeed();
            } finally { GoetyConfig.CHANTING_ENABLED.set(oldChant); GoetyConfig.COOLDOWNS_ENABLED.set(oldCooldown); p.discard(); }
        });
    }

}
