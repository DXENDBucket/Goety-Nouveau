package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.entities.util.VoidRift;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.*;

/** Focused native-stat and resource regression checks, shared by both supported loaders. */
public final class NativeGrowthCases {
    private NativeGrowthCases() {}
    private static Vec3 prepare(GameTestHelper h, ServerPlayer p) {
        var start=h.absoluteVec(new Vec3(4,0,4));
        var origin=new Vec3(start.x,h.getLevel().getMaxBuildHeight()-40,start.z);
        var pos=BlockPos.containing(origin);
        for(int x=-4;x<=4;x++) for(int z=-3;z<=12;z++) for(int y=-1;y<=8;y++)
            h.getLevel().setBlock(pos.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        p.setPos(origin);p.setYRot(0);p.setYHeadRot(0);p.setXRot(0);p.setGameMode(GameType.SURVIVAL);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemsRegistry.NOVICE_SPELLBOOK.get()));
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(p,true);SEHelper.setSESouls(p,10000);
        return origin;
    }
    private static SpellResolver recipe(GameTestHelper h,ServerPlayer p,AbstractSpellPart method,
                                         AbstractSpellPart glyph,AbstractAugment... augments) {
        var spell=new Spell(method,glyph);for(var a:augments)spell=spell.add(a);
        return new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),p.getMainHandItem()));
    }
    private static void effect(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult target,
                               AbstractAugment... augments) {
        var resolver=recipe(h,p,MethodTouch.INSTANCE,glyph,augments);resolver.spellContext.nextPart();
        CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,target,null)
                .withIncomingDirection(new Vec3(0,0,1)));
        resolver.onResolveEffect(h.getLevel(),target);
    }
    private static <T extends Entity> List<T> entities(GameTestHelper h,ServerPlayer p,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,p.getBoundingBox().inflate(24));
    }
    private static void clear(GameTestHelper h,ServerPlayer p) {
        entities(h,p,Projectile.class).forEach(Entity::discard);ExecutionSessions.tick();
    }
    public static void oldProjectileAttributes(GameTestHelper h,ServerPlayer p,Consumer<ServerPlayer> attributes,
                                              BiConsumer<ServerPlayer,Boolean> focus) {
        var mode=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);attributes.accept(p);
            for(boolean enchanted:List.of(false,true)) {
                if(enchanted)focus.accept(p,true);
                int gear=WandUtil.getPotencyLevel(p);
                var soul=GlyphCatalog.SOUL_BOLT.focus().spell();
                h.assertTrue(WandUtil.getStats(p,soul).getPotency()>soul.defaultStats().getPotency(),"Fixture adds native school attributes without requiring a focus");
                var cast=recipe(h,p,MethodProjectile.INSTANCE,GlyphCatalog.SOUL_BOLT,AugmentAmplify.INSTANCE);
                h.assertTrue(cast.onCast(p.getMainHandItem(),h.getLevel()),"Soul Bolt launches through the actual Ars recipe");
                var bolt=entities(h,p,SoulBolt.class).get(0);
                h.assertTrue(bolt.getExtraDamage()==WandUtil.getStats(p,soul).getPotency()+gear+1,"Legacy Soul Bolt includes native attribute, focus potency and Ars Amplify exactly once");
                var reference=new SoulBolt(p.getX(),p.getY(),p.getZ(),0,0,1,h.getLevel());
                reference.setBoltSpeed((int)(WandUtil.getStats(p,soul).getVelocity()+dev.goetynouveau.platform.NativeBoltHooks.velocityLevel(p)));
                h.assertTrue(bolt.boltSpeed==reference.boltSpeed,"Legacy Soul Bolt keeps native velocity attributes and enchantment");
                clear(h,p);
                h.assertTrue(recipe(h,p,MethodProjectile.INSTANCE,GlyphCatalog.FIREBALL,AugmentAmplify.INSTANCE).onCast(p.getMainHandItem(),h.getLevel()),"Native projectile adapter launches");
                var fire=entities(h,p,ModFireball.class).get(0);
                h.assertTrue(fire.getExtraDamage()==WandUtil.getStats(p,GlyphCatalog.FIREBALL.focus().spell()).getPotency()+gear+1,"Fireball branch receives attributes and enchantment once");
                clear(h,p);
                h.assertTrue(recipe(h,p,MethodProjectile.INSTANCE,GlyphCatalog.ICE_SPIKE,AugmentAmplify.INSTANCE).onCast(p.getMainHandItem(),h.getLevel()),"Additional factory launches");
                var ice=entities(h,p,IceSpike.class).get(0);
                h.assertTrue(ice.getExtraDamage()==WandUtil.getStats(p,GlyphCatalog.ICE_SPIKE.focus().spell()).getPotency()+gear+1,"Additional factory does not reapply the outer adapter baseline");
                clear(h,p);
            }
            h.assertTrue(recipe(h,p,MethodProjectile.INSTANCE,GlyphCatalog.MAGIC_BOLT,AugmentExtendTime.INSTANCE).onCast(p.getMainHandItem(),h.getLevel()),"Magic Bolt launches");
            var magic=entities(h,p,MagicBolt.class).get(0);
            h.assertTrue(magic.getExtraDuration()==WandUtil.getStats(p,GlyphCatalog.MAGIC_BOLT.focus().spell()).getDuration()+dev.goetynouveau.platform.NativeEffectHooks.duration(p)+1,"Magic Bolt duration includes all three sources once");
            h.succeed();
        } finally {clear(h,p);p.discard();GoetyConfig.RESOURCE_SOURCE.set(mode);}
    }
    public static void oldEffectAttributes(GameTestHelper h,ServerPlayer p,Consumer<ServerPlayer> attributes,
                                           BiConsumer<ServerPlayer,Boolean> focus) {
        var mode=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            var origin=prepare(h,p);attributes.accept(p);focus.accept(p,true);
            var spell=GlyphCatalog.VOID_RIFT.focus().spell();var nativeStats=WandUtil.getStats(p,spell);
            var point=origin.add(0,0,5);
            effect(h,p,GlyphCatalog.VOID_RIFT,new BlockHitResult(point,Direction.UP,BlockPos.containing(point).below(),false),
                    AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE,AugmentAOE.INSTANCE);
            var rift=entities(h,p,VoidRift.class).get(0);
            h.assertTrue(rift.getExtraDamage()==nativeStats.getPotency()+WandUtil.getPotencyLevel(p)+1,"Manual effect damage includes native attributes once");
            h.assertTrue(rift.getDuration()==nativeStats.getDuration()*2*(1+dev.goetynouveau.platform.NativeEffectHooks.duration(p)),"Native effect duration multiplies its attribute baseline once");
            h.assertTrue(Math.abs(rift.getSize()-(nativeStats.getRadius()+1+dev.goetynouveau.platform.NativeEffectHooks.radius(p)))<.001,"Native effect radius has attributes plus focus plus Ars AOE");
            rift.discard();h.succeed();
        } finally {clear(h,p);p.discard();GoetyConfig.RESOURCE_SOURCE.set(mode);}
    }
    public static void nativeCallbacksDoNotDoubleGrowth(GameTestHelper h,ServerPlayer p,Consumer<ServerPlayer> attributes,
                                                        BiConsumer<ServerPlayer,Boolean> focus) {
        var mode=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        Mob target=null;
        try {
            var origin=prepare(h,p);attributes.accept(p);focus.accept(p,true);
            h.getLevel().addNewPlayer(p);
            target=EntityType.COW.create(h.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(origin.add(0,0,4));
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);target.setHealth(200);h.getLevel().addFreshEntity(target);
            var spell=GlyphCatalog.FIRE_BREATH.focus().spell();
            float expected=SpellConfig.FireBreathDamage.get().floatValue()*WandUtil.damageMultiply()
                    +WandUtil.getStats(p,spell).getPotency()+WandUtil.getPotencyLevel(p)+1;
            effect(h,p,GlyphCatalog.FIRE_BREATH,new EntityHitResult(p),AugmentAmplify.INSTANCE);
            h.assertTrue(Math.abs(200-target.getHealth()-expected)<.001,"Native SpellResult applies attribute, focus and Ars potency exactly once: actual="+(200-target.getHealth())+", expected="+expected);
            h.assertTrue(GoetyWindStormCasting.flyingPotency(2,true,3,true)==5
                    && GoetyWindStormCasting.flyingPotency(2,true,3,false)==3
                    && GoetyWindStormCasting.flyingPotency(2,false,0,false)==2,"Flying preserves current additive and supported legacy replacement rules");
            h.succeed();
        } finally {if(target!=null)target.discard();p.discard();ExecutionSessions.tick();GoetyConfig.RESOURCE_SOURCE.set(mode);}
    }
    public static void flyingMatchesLoadedNativeRule(GameTestHelper h,ServerPlayer p,Consumer<ServerPlayer> attributes,
                                                     BiConsumer<ServerPlayer,Boolean> focus) {
        var mode=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);attributes.accept(p);focus.accept(p,true);
            var spell=(com.Polarice3.Goety.common.magic.Spell)GlyphCatalog.FLYING.focus().spell();
            dev.goetynouveau.platform.NativeBreathHooks.result(spell,h.getLevel(),p,GoetyResources.staff(p),WandUtil.getStats(p,spell));
            double nativePower=p.getDeltaMovement().length();p.setDeltaMovement(Vec3.ZERO);
            effect(h,p,GlyphCatalog.FLYING,new EntityHitResult(p),AugmentAmplify.INSTANCE,AugmentAmplify.INSTANCE);
            h.assertTrue(Math.abs(p.getDeltaMovement().length()-nativePower-1)<.0001,
                    "Loaded native Flying equipment/stat rule plus two Ars potency (native integer division): native="+nativePower+", bridge="+p.getDeltaMovement().length());
            h.succeed();
        } finally {p.discard();ExecutionSessions.tick();GoetyConfig.RESOURCE_SOURCE.set(mode);}
    }
    public static void nativeEnchantBillingSwitch(GameTestHelper h,ServerPlayer p,BiConsumer<ServerPlayer,Boolean> focus,
                                                 Consumer<ItemStack> enchant) {
        var mode=GoetyConfig.RESOURCE_SOURCE.get();var enabled=SpellConfig.EnchantMultiCost.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.EnchantMultiCost.set(true);
        try {
            prepare(h,p);var context=recipe(h,p,MethodProjectile.INSTANCE,GlyphCatalog.SOUL_BOLT).spellContext;
            var spell=GlyphCatalog.SOUL_BOLT.focus().spell();int plain=GoetyResources.quote(context,spell).amount();
            h.assertTrue(plain>0,"Plain focus quote is positive");
            var emptyWand=new ItemStack(ModItems.DARK_WAND.get());enchant.accept(emptyWand);
            p.setItemInHand(InteractionHand.OFF_HAND,emptyWand);
            h.assertTrue(GoetyResources.quote(context,spell).amount()==plain,"Enchanting the staff itself without a focus cannot double the glyph fee");
            focus.accept(p,false);
            h.assertTrue(GoetyResources.quote(context,spell).amount()==plain,"An unenchanted equipped focus retains its original fee");
            focus.accept(p,true);
            h.assertTrue(GoetyResources.quote(context,spell).amount()==plain*2,"Enchanted offhand focus pays the native doubled fee");
            h.assertTrue(GoetyResources.quote(context,spell).amount()==WandUtil.getSoulUse(p,WandUtil.findFocus(p),spell.soulCost(p,GoetyResources.staff(p))),"Ordinary glyph price equals native wand focus billing");
            SpellConfig.EnchantMultiCost.set(false);
            int disabled=GoetyResources.quote(context,spell).amount();
            h.assertTrue(disabled==plain,"Native config switch disables the surcharge: plain="+plain+", disabled="+disabled+", switch="+SpellConfig.EnchantMultiCost.get()+", native="+WandUtil.getSoulUse(p,WandUtil.findFocus(p),spell.soulCost(p,GoetyResources.staff(p))));
            h.succeed();
        } finally {p.discard();SpellConfig.EnchantMultiCost.set(enabled);GoetyConfig.RESOURCE_SOURCE.set(mode);}
    }
    public static void discountsFailedActionsAndAccounts(GameTestHelper h,ServerPlayer p,BiConsumer<ServerPlayer,Boolean> focus,
                                                         IntConsumer mana,IntSupplier balance) {
        var mode=GoetyConfig.RESOURCE_SOURCE.get();var enabled=SpellConfig.EnchantMultiCost.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.EnchantMultiCost.set(true);
        try {
            prepare(h,p);focus.accept(p,true);p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(ModItems.DARK_HELMET.get()));
            var context=recipe(h,p,MethodTouch.INSTANCE,GlyphCatalog.FIRE_BREATH).spellContext;
            var spell=GlyphCatalog.FIRE_BREATH.focus().spell();var augments=List.<AbstractAugment>of(AugmentBurning.INSTANCE);
            int nativeBase=spell.soulCost(p,GoetyResources.staff(p));
            int raw=nativeBase*2+GoetyConfig.BURNING_COST.get();
            int expected=(int)(raw*SEHelper.soulDiscount(p));
            h.assertTrue(SEHelper.soulDiscount(p)<1,"Native armor discount is active in the fixture");
            var quote=GoetyResources.quote(context,spell,augments);
            h.assertTrue(quote.amount()==expected,"Multiply native focus fee before addon flat surcharge, then discount and truncate once");
            int before=SEHelper.getSESouls(p);
            h.assertTrue(!GoetyResources.activation(quote).trigger(()->false)&&SEHelper.getSESouls(p)==before,"Failed activation releases its reservation without a soul charge");
            try {GoetyResources.activation(quote).trigger(()->{throw new IllegalStateException("fixture failure");});}
            catch(IllegalStateException expectedFailure) {h.assertTrue(expectedFailure.getMessage().equals("fixture failure"),"Expected fixture exception only");}
            h.assertTrue(GoetyResources.activation(quote).trigger(()->true)&&SEHelper.getSESouls(p)==before-expected,"Exception cleanup permits the next activation and only one discounted spend");
            SEHelper.setSESouls(p,expected-1);
            h.assertTrue(!GoetyResources.activation(quote).trigger(()->{throw new AssertionError("Unaffordable action executed");}),"Insufficient discounted balance rejects before effect");
            GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.ARS_MANA);mana.accept(1000);
            var manaQuote=GoetyResources.quote(context,spell,augments);int souls=SEHelper.getSESouls(p);
            h.assertTrue(manaQuote.amount()==raw,"ARS_MANA preserves the old bypass of Soul armor discount and uses the native enchant multiplier");
            h.assertTrue(!GoetyResources.activation(manaQuote).trigger(()->false)&&balance.getAsInt()==1000,"Failed Mana action is uncharged");
            h.assertTrue(GoetyResources.activation(manaQuote).trigger(()->true)&&balance.getAsInt()==1000-raw&&SEHelper.getSESouls(p)==souls,"Successful alternate account spends Mana once and leaves Soul untouched");
            p.setGameMode(GameType.CREATIVE);int current=balance.getAsInt();
            var creativeQuote=GoetyResources.quote(context,spell,augments); // A new cast takes a new account snapshot.
            h.assertTrue(GoetyResources.activation(creativeQuote).trigger(()->true)&&balance.getAsInt()==current,"Creative remains free with an enchanted focus");
            h.succeed();
        } finally {p.discard();SpellConfig.EnchantMultiCost.set(enabled);GoetyConfig.RESOURCE_SOURCE.set(mode);}
    }
}
