package dev.goetynouveau;

import com.Polarice3.Goety.client.inventory.container.CraftingFocusMenu;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.arsconflux.api.context.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import java.util.*;

/** Actual native menus, player inventory ownership, protocol opening and transactional payment. */
public final class MenuCases {
    public record Fixture(ServerPlayer player, List<Packet<?>> packets) {}
    private static void prepare(GameTestHelper h, ServerPlayer p) {
        p.setPos(h.absoluteVec(new Vec3(4, 40, 4))); p.setNoGravity(true);
        h.getLevel().addNewPlayer(p); p.setGameMode(GameType.SURVIVAL);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,100000);
    }
    private static void cleanup(ServerPlayer p) {p.closeContainer();p.discard();}
    private static void cast(GameTestHelper h, ServerPlayer p, GoetyEffectGlyph glyph, HitResult hit,
                             LivingEntity selectedOwner, boolean success) {
        var context=new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,glyph),p,LivingCaster.from(p),ItemStack.EMPTY);
        var resolver=new SpellResolver(context);context.nextPart();
        if(selectedOwner!=null) CastContexts.bind(context,CastContext.of(context,hit,null).withExecutor(selectedOwner).withDamageOwner(selectedOwner));
        int before=SEHelper.getSESouls(p),price=GoetyResources.quote(context,glyph.focus().spell()).amount();
        h.assertTrue(price>0,"Native menu fee is meaningful in this fixture");
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(success&&!p.isCreative()?price:0),"Only actual menu opening pays once: "+glyph.focus());
    }
    private static BlockHitResult block(ServerPlayer p) {return new BlockHitResult(p.position().add(20,0,0),Direction.UP,p.blockPosition().offset(20,0,0),false);}
    private static long screens(Fixture f) {return f.packets().stream().filter(ClientboundOpenScreenPacket.class::isInstance).count();}
    public static void nativeCraftingAndClose(GameTestHelper h, Fixture f) {
        var p=f.player();prepare(h,p);
        try {
            long before=screens(f);cast(h,p,GlyphCatalog.CRAFTING,block(p),null,true);
            h.assertTrue(p.containerMenu instanceof CraftingFocusMenu && screens(f)==before+1,"Actual native crafting menu sends an open-screen packet");
            h.assertTrue(p.containerMenu.stillValid(p),"Portable crafting works without a physical crafting table");
            p.containerMenu.getSlot(1).set(new ItemStack(Items.BONE,3));p.closeContainer();
            h.assertTrue(p.containerMenu==p.inventoryMenu && p.getInventory().countItem(Items.BONE)==3,"Closing returns actual crafting inputs without loss");
            var nativeSpell=GlyphCatalog.CRAFTING.focus().spell();nativeSpell.SpellResult(h.getLevel(),p,ItemStack.EMPTY,WandUtil.getStats(p,nativeSpell));
            h.assertTrue(p.containerMenu instanceof CraftingFocusMenu,"Ordinary Goety callback still opens its original menu");
            h.succeed();
        } finally {cleanup(p);}
    }
    public static void personalEnderChestAndSourceIsolation(GameTestHelper h, Fixture f, Fixture other) {
        var p=f.player();var q=other.player();prepare(h,p);prepare(h,q);q.setPos(p.position().add(4,0,0));
        try {
            p.getEnderChestInventory().setItem(0,new ItemStack(Items.DIAMOND,2));q.getEnderChestInventory().setItem(0,new ItemStack(Items.EMERALD,5));
            long before=screens(f);cast(h,p,GlyphCatalog.ENDER_CHEST,new EntityHitResult(q),q,true);
            h.assertTrue(p.containerMenu instanceof ChestMenu && screens(f)==before+1,"Native ender menu sends its actual open-screen packet");
            var chest=(ChestMenu)p.containerMenu;
            h.assertTrue(chest.getContainer()==p.getEnderChestInventory() && chest.getSlot(0).getItem().is(Items.DIAMOND),"Selected executor/damage owner never exposes another player's ender chest");
            h.assertTrue(q.containerMenu==q.inventoryMenu && screens(other)==0 && q.getEnderChestInventory().getItem(0).getCount()==5,"Other player's menu and storage remain private");
            chest.getSlot(0).set(new ItemStack(Items.DIAMOND,1));p.closeContainer();
            h.assertTrue(p.getEnderChestInventory().getItem(0).getCount()==1,"Opened menu is the real persistent ender inventory");
            var nativeSpell=GlyphCatalog.ENDER_CHEST.focus().spell();nativeSpell.SpellResult(h.getLevel(),p,ItemStack.EMPTY,WandUtil.getStats(p,nativeSpell));
            h.assertTrue(((ChestMenu)p.containerMenu).getContainer()==p.getEnderChestInventory(),"Unscoped Goety callback unchanged");
            h.succeed();
        } finally {cleanup(q);cleanup(p);}
    }
    public static void invalidResourceCreativeAndDeniedOpen(GameTestHelper h, Fixture f, Fixture denied, ServerPlayer fake) {
        var p=f.player();var blocked=denied.player();prepare(h,p);prepare(h,blocked);prepare(h,fake);
        try {
            SEHelper.setSESouls(p,0);
            for(var glyph:List.of(GlyphCatalog.CRAFTING,GlyphCatalog.ENDER_CHEST))cast(h,p,glyph,new EntityHitResult(p),null,false);
            h.assertTrue(p.containerMenu==p.inventoryMenu && screens(f)==0,"Empty resources open no menu or packets");
            SEHelper.setSESouls(p,100000);cast(h,p,GlyphCatalog.ENDER_CHEST,block(p),null,false);
            p.setGameMode(GameType.SPECTATOR);cast(h,p,GlyphCatalog.CRAFTING,new EntityHitResult(p),null,false);
            h.assertTrue(p.containerMenu==p.inventoryMenu,"Block ender triggers and spectator casts are rejected without payment");
            p.setGameMode(GameType.CREATIVE);SEHelper.setSESouls(p,0);
            for(var glyph:List.of(GlyphCatalog.CRAFTING,GlyphCatalog.ENDER_CHEST)) {cast(h,p,glyph,new EntityHitResult(p),null,true);h.assertTrue(p.containerMenu!=p.inventoryMenu,"Creative opens actual native menu free");p.closeContainer();}
            for(var glyph:List.of(GlyphCatalog.CRAFTING,GlyphCatalog.ENDER_CHEST)) {
                cast(h,blocked,glyph,new EntityHitResult(blocked),null,false);
                cast(h,fake,glyph,new EntityHitResult(fake),null,false);
            }
            h.assertTrue(blocked.containerMenu==blocked.inventoryMenu && screens(denied)==0 && fake.containerMenu==fake.inventoryMenu,"Denied openings and fake players never receive a usable menu");
            var cow=EntityType.COW.create(h.getLevel());cow.setPos(p.position());
            var context=new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.CRAFTING),cow,LivingCaster.from(cow),ItemStack.EMPTY);
            h.assertTrue(!GoetyMenuCasting.apply(GlyphCatalog.CRAFTING.focus(),h.getLevel(),CastContext.of(context,new EntityHitResult(cow),null)),"Mob casters cannot create player menus");
            h.succeed();
        } finally {cleanup(fake);cleanup(blocked);cleanup(p);}
    }
}
