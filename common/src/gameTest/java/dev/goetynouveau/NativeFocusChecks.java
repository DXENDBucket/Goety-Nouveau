package dev.goetynouveau;
import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodSelf;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.casting.NativeFocusScope;
import dev.goetynouveau.compat.*;
import dev.goetynouveau.glyph.NativeFocusGlyph;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.InteractionHand;

public final class NativeFocusChecks {
    static NativeFocusGlyph glyph(String path){return NativeFocusIndex.glyph(CarrierRegistry.id("goety_nouveau:glyph_native/native_focus_fixture/"+path));}
    public static void prepare(GameTestHelper h,ServerPlayer player){
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.setPos(h.absoluteVec(new Vec3(2,4,2)));h.getLevel().addNewPlayer(player);
        SEHelper.setSEActive(player,true);SEHelper.setSESouls(player,1000);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry.NOVICE_SPELLBOOK.get()));
    }
    private static void resolve(GameTestHelper h,ServerPlayer player,NativeFocusGlyph glyph,HitResult hit){
        var spell=new Spell(MethodSelf.INSTANCE,glyph);var context=new SpellContext(h.getLevel(),spell,player,new LivingCaster(player),player.getMainHandItem());
        glyph.onResolve(hit,h.getLevel(),player,new SpellStats.Builder().setAmplification(2).build(),context,new SpellResolver(context));
    }
    public static void catalogue(GameTestHelper h,ServerPlayer p){
        prepare(h,p);h.assertTrue(NativeFocusIndex.all().stream().filter(g->g.focusId().getNamespace().equals("native_focus_fixture")).count()==4,"Early and late registrations all create glyphs");
        for(var name:java.util.List.of("early","warmup","charge","ever")){
            var g=glyph(name);h.assertTrue(g!=null&&BuiltInRegistries.ITEM.containsKey(g.getRegistryName()),"Native glyph item registered");
            h.assertTrue(NativeFocusResources.model(CarrierRegistry.id("goety_nouveau:item/"+g.getRegistryName().getPath())).contains("native_focus_fixture:item/"+name),"Source focus item model is reused");
            h.assertTrue(h.getLevel().getRecipeManager().byKey(g.getRegistryName()).isPresent(),"Native Scribe recipe loaded");
        }
        h.assertTrue(glyph("catalogue_alias")==null,"An Item-valued custom registry is not another item registration");
        var before=NativeFocusIndex.all().size();
        NativeFocusIndex.registered(CarrierRegistry.id("native_focus_fixture:warmup"),BuiltInRegistries.ITEM.get(CarrierRegistry.id("native_focus_fixture:warmup")));
        h.assertTrue(NativeFocusIndex.all().size()==before,"Repeated observations do not duplicate items, configs or catalogue entries");
        h.assertTrue(NativeFocusIndex.all().stream().noneMatch(g->g.focusId().getNamespace().equals("goety")),"Existing native catalogue isn't duplicated");p.discard();h.succeed();
    }
    public static void warmupStaffAndScope(GameTestHelper h,ServerPlayer p){
        prepare(h,p);var staff=new ItemStack(BuiltInRegistries.ITEM.get(CarrierRegistry.id("goety:frost_staff")));
        h.assertTrue(!staff.isEmpty(),"Fixture uses actual native Frost staff");dev.goetynouveau.platform.NativeFocusPlatform.state(staff,99,17);p.setItemInHand(InteractionHand.OFF_HAND,staff);
        resolve(h,p,glyph("warmup"),new EntityHitResult(p));h.assertTrue(p.getPersistentData().getInt("fixture_start")==1&&p.getPersistentData().getInt("fixture_result")==0,"Start runs before native warmup completes");
        h.runAfterDelay(9,()->{
            h.assertTrue(p.getPersistentData().getInt("fixture_result")==1&&p.getPersistentData().getInt("fixture_stop")==1,"One result and one cleanup");
            h.assertTrue(p.getPersistentData().getBoolean("fixture_frost")&&p.getPersistentData().getBoolean("fixture_virtual"),"Native callbacks see actual staff type and scoped focus");
            h.assertTrue(p.getPersistentData().getInt("fixture_potency")>=2,"Ars potency reaches native WandUtil stats");
            h.assertTrue(p.getPersistentData().getInt("fixture_potency")==p.getPersistentData().getInt("fixture_result_potency"),"Callbacks cannot mutate the next callback's base stats");
            h.assertTrue(p.getOffhandItem()==staff&&((DarkWand)staff.getItem()).ShotsFired(staff)==17,"Equipped staff state isn't overwritten");
            h.assertTrue(WandUtil.findWand(p)==staff&&NativeFocusScope.current(p)==null,"Scope does not leak into ordinary Goety casts");p.discard();h.succeed();
        });
    }
    public static void chargingBilling(GameTestHelper h,ServerPlayer p){
        prepare(h,p);resolve(h,p,glyph("charge"),new EntityHitResult(p));
        h.runAfterDelay(12,()->{h.assertTrue(p.getPersistentData().getInt("fixture_result")==3&&p.getPersistentData().getInt("fixture_stop")==1,"Native finite magazine and stop callback");
            h.assertTrue(SEHelper.getSESouls(p)==979,"Three results pay three native costs, without duplicate wand payment");
            h.assertTrue(SEHelper.isOnCooldown(p,glyph("charge").source()),"Native focus cooldown shared with wands");p.discard();h.succeed();});
    }
    public static void emptyAccountAndInvalidTarget(GameTestHelper h,ServerPlayer p){
        prepare(h,p);var cow=h.spawn(net.minecraft.world.entity.EntityType.COW,4,4,4);
        resolve(h,p,glyph("warmup"),new EntityHitResult(cow));p.getPersistentData().putBoolean("fixture_reject",true);resolve(h,p,glyph("warmup"),new EntityHitResult(p));
        h.assertTrue(SEHelper.getSESouls(p)==1000&&p.getPersistentData().getInt("fixture_start")==0,"Remote targets and native condition rejection are free");
        p.getPersistentData().putBoolean("fixture_reject",false);SEHelper.setSESouls(p,7);resolve(h,p,glyph("ever"),new EntityHitResult(p));
        h.runAfterDelay(35,()->{h.assertTrue(p.getPersistentData().getInt("fixture_result")==20&&p.getPersistentData().getInt("fixture_stop")==1,"EverCharge prepays groups of twenty, then stops on empty account");
            h.assertTrue(SEHelper.getSESouls(p)==0&&NativeFocusScope.current(p)==null,"No negative resources or leaked state");p.discard();cow.discard();h.succeed();});
    }
    public static void arsAlternativeAndCreative(GameTestHelper h,ServerPlayer p){
        prepare(h,p);
        var mana=new java.util.concurrent.atomic.AtomicInteger(1000);
        var wrapped=new LivingCaster(p){public boolean enoughMana(int amount){return mana.get()>=amount;}public void expendMana(int amount){mana.addAndGet(-amount);}};
        arsAlternativeAndCreative(h,p,wrapped,mana::get);
    }
    public static void arsAlternativeAndCreative(GameTestHelper h,ServerPlayer p,LivingCaster wrapped,java.util.function.IntSupplier mana){
        var previous=dev.goetynouveau.platform.GoetyConfig.RESOURCE_SOURCE.get();
        dev.goetynouveau.platform.GoetyConfig.RESOURCE_SOURCE.set(dev.goetynouveau.api.ResourceSource.ARS_MANA);
        var g=glyph("warmup");
        java.util.function.Consumer<NativeFocusGlyph> cast=glyph->{
            var spell=new Spell(MethodSelf.INSTANCE,glyph);
            var context=new SpellContext(h.getLevel(),spell,p,wrapped,p.getMainHandItem());
            glyph.onResolve(new EntityHitResult(p),h.getLevel(),p,new SpellStats.Builder().build(),context,new SpellResolver(context));
        };
        try{cast.accept(g);h.assertTrue(mana.getAsInt()==993&&SEHelper.getSESouls(p)==1000,"Ars resource mode bills only its selected account; mana="+mana.getAsInt()+", souls="+SEHelper.getSESouls(p)+", started="+p.getPersistentData().getInt("fixture_start"));}
        catch(RuntimeException|Error error){dev.goetynouveau.platform.GoetyConfig.RESOURCE_SOURCE.set(previous);p.discard();throw error;}
        h.runAfterDelay(9,()->{
            try{
                h.assertTrue(SEHelper.getSESouls(p)==1000&&p.getPersistentData().getInt("fixture_result")==1,"Native result completed without charging the soul account");
                int before=mana.getAsInt();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);cast.accept(g);
                h.assertTrue(mana.getAsInt()==before&&p.getPersistentData().getInt("fixture_start")==2,"Creative ignores existing native cooldown and resource charge");
            }catch(RuntimeException|Error error){dev.goetynouveau.platform.GoetyConfig.RESOURCE_SOURCE.set(previous);p.discard();throw error;}
            h.runAfterDelay(9,()->{try{h.assertTrue(SEHelper.getSESouls(p)==1000&&p.getPersistentData().getInt("fixture_result")==2,"Creative completes the same native lifecycle for free");h.succeed();}
                finally{dev.goetynouveau.platform.GoetyConfig.RESOURCE_SOURCE.set(previous);p.discard();}});
        });
    }
}
