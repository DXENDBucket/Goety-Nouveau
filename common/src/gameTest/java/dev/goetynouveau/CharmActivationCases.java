package dev.goetynouveau;

import com.Polarice3.Goety.api.entities.IOwned;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetycharms.item.Charms;
import dev.goetycharms.platform.CharmPlatform;
import dev.goetynouveau.compat.charms.ActivateCharmGlyph;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;

/** Compile-checked optional integration cases; run with Goety Charms present. */
public final class CharmActivationCases {
    private static ItemStack charm(GameTestHelper h,String path,boolean sealed) {
        var id=CarrierRegistry.id("goety:"+path);var source=CharmPlatform.source(h.getLevel(),id);
        return Charms.create(id,source,sealed?source.getActivationItem().getItems()[0].copy():ItemStack.EMPTY);
    }
    private static void prepare(GameTestHelper h,ServerPlayer player) {
        player.getInventory().clearContent();player.setGameMode(GameType.SURVIVAL);
        player.setPos(h.absoluteVec(new Vec3(1,2,1)));
    }
    private static void cast(GameTestHelper h,ServerPlayer player,HitResult hit) {
        var glyph=GlyphRegistry.getSpellpartMap().get(CarrierRegistry.id("goety_nouveau:glyph_activate_charm"));
        var context=new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,glyph),player,LivingCaster.from(player),ItemStack.EMPTY);
        var resolver=new SpellResolver(context);context.nextPart();resolver.onResolveEffect(h.getLevel(),hit);
    }
    public static void inventoryOrder(GameTestHelper h,ServerPlayer player) {
        prepare(h,player);
        var summon=charm(h,"thrall/summon_zombie_thrall",true);var convert=charm(h,"convert_blazes_to_wildfire",true);
        player.setItemInHand(InteractionHand.OFF_HAND,charm(h,"locate/ancient_city",true));
        player.getInventory().setItem(0,charm(h,"thrall/summon_zombie_thrall",false));
        player.getInventory().setItem(8,summon);player.getInventory().setItem(9,convert);
        h.assertTrue(ActivateCharmGlyph.select(player)==summon,"Location charms and unsealed samples are skipped; hotbar beats backpack");
        player.setItemInHand(InteractionHand.OFF_HAND,convert.copy());
        h.assertTrue(ActivateCharmGlyph.select(player)==player.getOffhandItem(),"Offhand has first priority");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);player.getInventory().setItem(8,ItemStack.EMPTY);
        var rowEnd=convert.copy();var nextRow=summon.copy();
        player.getInventory().setItem(17,rowEnd);player.getInventory().setItem(18,nextRow);
        h.assertTrue(ActivateCharmGlyph.select(player)==convert,"First backpack cell beats later columns and rows");
        player.getInventory().setItem(9,ItemStack.EMPTY);
        h.assertTrue(ActivateCharmGlyph.select(player)==rowEnd,"Backpack scans across a row before moving down");h.succeed();
    }
    public static void wrongTargetNoFallback(GameTestHelper h,ServerPlayer player) {
        prepare(h,player);
        var convert=charm(h,"convert_blazes_to_wildfire",true);var summon=charm(h,"thrall/summon_zombie_thrall",true);
        player.setItemInHand(InteractionHand.OFF_HAND,convert);player.getInventory().setItem(0,summon);
        var pig=EntityType.PIG.create(h.getLevel());pig.setPos(h.absoluteVec(new Vec3(5,1,5)));h.getLevel().addFreshEntity(pig);
        cast(h,player,new EntityHitResult(pig));
        h.assertTrue(convert.getCount()==1&&summon.getCount()==1&&pig.isAlive(),"Failed conversion retains its charm without falling back to a summon");
        pig.discard();h.succeed();
    }
    public static void selectedLocationAndCreative(GameTestHelper h,ServerPlayer player) {
        prepare(h,player);var summon=charm(h,"thrall/summon_zombie_thrall",true);player.getInventory().setItem(0,summon);
        var pos=h.absolutePos(new BlockPos(5,0,5));var hit=new BlockHitResult(pos.getCenter(),Direction.UP,pos,false);
        com.Polarice3.Goety.utils.SEHelper.setSESouls(player,17);cast(h,player,hit);
        var servants=h.getLevel().getEntitiesOfClass(Mob.class,new AABB(pos).inflate(3),m->m instanceof IOwned owned&&player.getUUID().equals(owned.getOwnerId()));
        h.assertTrue(summon.isEmpty()&&!servants.isEmpty(),"Ars block hit produces a native owned summon at the selected position and consumes one charm");
        h.assertTrue(com.Polarice3.Goety.utils.SEHelper.getSESouls(player)==17,"Already-paid ritual does not charge Goety soul energy again");servants.forEach(Entity::discard);
        var creative=charm(h,"thrall/summon_zombie_thrall",true);player.getInventory().setItem(0,creative);player.setGameMode(GameType.CREATIVE);cast(h,player,hit);
        h.assertTrue(creative.getCount()==1,"Creative activation retains the actual charm");h.succeed();
    }
    public static void selectedConversion(GameTestHelper h,ServerPlayer player) {
        prepare(h,player);var convert=charm(h,"convert_blazes_to_wildfire",true);player.setItemInHand(InteractionHand.OFF_HAND,convert);
        var pos=h.absolutePos(new BlockPos(5,1,5));var blaze=EntityType.BLAZE.create(h.getLevel());blaze.setPos(pos.getCenter());h.getLevel().addFreshEntity(blaze);
        cast(h,player,new EntityHitResult(blaze));
        var results=h.getLevel().getEntitiesOfClass(Mob.class,new AABB(pos).inflate(3),m->m.getType()==com.Polarice3.Goety.common.entities.ModEntityType.WILDFIRE.get());
        h.assertTrue(convert.isEmpty()&&!results.isEmpty()&&results.get(0) instanceof IOwned owned&&player.getUUID().equals(owned.getOwnerId()),"Ars-selected Blaze converts through native ritual with original player ownership");
        results.forEach(Entity::discard);h.succeed();
    }
    private CharmActivationCases() { }
}
