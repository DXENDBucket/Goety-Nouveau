package dev.goetynouveau;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Focused native capability, damage, allegiance and billing checks shared by both loaders. */
public final class CombatEffectCases {
    private static Vec3 scene(GameTestHelper h) {
        var origin = h.absoluteVec(new Vec3(3, 0, 3));
        return new Vec3(origin.x, h.getLevel().getMaxBuildHeight() - 24, origin.z);
    }
    private static void prepare(GameTestHelper h, ServerPlayer player) {
        var pos = BlockPos.containing(scene(h));
        for (int x=-9; x<=10; x++) for (int z=-4; z<=6; z++) for (int y=0; y<=4; y++)
            h.getLevel().setBlock(pos.offset(x,y,z), Blocks.AIR.defaultBlockState(),2);
        player.setPos(scene(h).add(-8,0,0)); player.setYRot(90);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(player,true); SEHelper.setSESouls(player,10000);
    }
    private static LivingEntity cow(GameTestHelper h, Vec3 point) {
        var cow=EntityType.COW.create(h.getLevel()); cow.setPos(point);
        cow.setNoAi(true); cow.setNoGravity(true);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); cow.setHealth(200);
        h.getLevel().addFreshEntity(cow); return cow;
    }
    private static SpellResolver effect(GameTestHelper h, ServerPlayer player, GoetyEffectGlyph glyph, AbstractAugment... augments) {
        var spell=new Spell(MethodTouch.INSTANCE,glyph);
        for(var augment:augments) spell=spell.add(augment);
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,player,LivingCaster.from(player),ItemStack.EMPTY));
        resolver.spellContext.nextPart(); return resolver;
    }
    public static void bulwarkNativeShieldsAndBilling(GameTestHelper h, ServerPlayer player) {
        var previous=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("shield"+UUID.randomUUID().toString().substring(0,8));
        prepare(h,player); var ally=cow(h,scene(h)); var enemy=cow(h,scene(h).add(2,0,0));
        board.addPlayerToTeam(player.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            var nativeSpell=GlyphCatalog.BULWARK.focus().spell();
            nativeSpell.SpellResult(h.getLevel(),player,ItemStack.EMPTY,nativeSpell.defaultStats());
            int count=MiscCapHelper.getShields(player), ticks=MiscCapHelper.getShieldTime(player);
            h.assertTrue(count>0 && ticks>0,"Native reference grants shields and a tick lifetime");
            MiscCapHelper.setShields(player,0); MiscCapHelper.setShieldTime(player,0);
            var cast=effect(h,player,GlyphCatalog.BULWARK);
            int price=GoetyResources.quote(cast.spellContext,nativeSpell).amount();
            cast.onResolveEffect(h.getLevel(),new EntityHitResult(player));
            h.assertTrue(MiscCapHelper.getShields(player)==count && MiscCapHelper.getShieldTime(player)==ticks
                    && SEHelper.getSESouls(player)==10000-price,"Unmodified Ars effect matches native self shields and pays once");
            effect(h,player,GlyphCatalog.BULWARK,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE)
                    .onResolveEffect(h.getLevel(),new EntityHitResult(ally));
            h.assertTrue(MiscCapHelper.getShields(ally)==count+1 && MiscCapHelper.getShieldTime(ally)==ticks*2,
                    "Already shielded caster can grant a modified native shield to an unshielded ally");
            int balance=SEHelper.getSESouls(player);
            effect(h,player,GlyphCatalog.BULWARK).onResolveEffect(h.getLevel(),new EntityHitResult(ally));
            effect(h,player,GlyphCatalog.BULWARK).onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            h.assertTrue(SEHelper.getSESouls(player)==balance && MiscCapHelper.getShields(enemy)==0,
                    "Existing shields and hostile recipients do not charge");
            float health=ally.getHealth();
            ally.hurt(h.getLevel().damageSources().mobAttack(enemy),1);
            h.assertTrue(ally.getHealth()==health && MiscCapHelper.getShields(ally)==count,
                    "Goety's real damage event consumes a shield and blocks the attack");
            MiscCapHelper.setShields(ally,0); MiscCapHelper.setShieldTime(ally,0);
            SEHelper.setSESouls(player,0);
            effect(h,player,GlyphCatalog.BULWARK).onResolveEffect(h.getLevel(),new EntityHitResult(ally));
            h.assertTrue(MiscCapHelper.getShields(ally)==0,"Insufficient native resource cannot grant shields");
            player.setGameMode(GameType.CREATIVE);
            effect(h,player,GlyphCatalog.BULWARK).onResolveEffect(h.getLevel(),new EntityHitResult(ally));
            h.assertTrue(MiscCapHelper.getShields(ally)==count && SEHelper.getSESouls(player)==0,"Creative shields are free");
            h.succeed();
        } finally { ally.discard(); enemy.discard(); player.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void dischargeNativeAreaChainAndBilling(GameTestHelper h, ServerPlayer player) {
        var previous=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("spark"+UUID.randomUUID().toString().substring(0,8));
        prepare(h,player);
        var center=scene(h); var primary=cow(h,center.add(1,0,0)); var ally=cow(h,center.add(1,0,2));
        // Outside even the doubled native explosion radius, within chain range of the primary.
        var chain=cow(h,center.add(9,0,0)); var near=cow(h,center.add(4,0,0));
        board.addPlayerToTeam(player.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            var cast=effect(h,player,GlyphCatalog.DISCHARGE);
            int price=GoetyResources.quote(cast.spellContext,GlyphCatalog.DISCHARGE.focus().spell()).amount();
            var hit=new BlockHitResult(center,Direction.UP,BlockPos.containing(center),false);
            cast.onResolveEffect(h.getLevel(),hit);
            h.assertTrue(primary.getHealth()<200 && ally.getHealth()==200 && chain.getHealth()==200
                    && SEHelper.getSESouls(player)==10000-price,"Selected-point native burst excludes allies and charges once");
            h.assertTrue(primary.getLastHurtByMob()==player,"Native damage remains attributed to the original caster");
            primary.invulnerableTime=0; near.invulnerableTime=0;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.STORM_STAFF.get()));
            effect(h,player,GlyphCatalog.DISCHARGE).onResolveEffect(h.getLevel(),hit);
            h.assertTrue(chain.getHealth()<200 && ally.getHealth()==200,"Native Storm staff chaining reaches outside the burst and respects allies");
            SEHelper.setSESouls(player,0); float health=primary.getHealth(); primary.invulnerableTime=0;
            effect(h,player,GlyphCatalog.DISCHARGE).onResolveEffect(h.getLevel(),new EntityHitResult(primary));
            h.assertTrue(primary.getHealth()==health,"Unpaid electrical burst causes no damage");
            player.setGameMode(GameType.CREATIVE);
            effect(h,player,GlyphCatalog.DISCHARGE,AugmentAmplify.INSTANCE).onResolveEffect(h.getLevel(),new EntityHitResult(primary));
            h.assertTrue(primary.getHealth()<health && SEHelper.getSESouls(player)==0,"Creative selected-entity discharge is free");
            h.succeed();
        } finally { primary.discard(); ally.discard(); chain.discard(); near.discard(); player.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
}
