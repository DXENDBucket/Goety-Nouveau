package dev.goetynouveau;

import com.Polarice3.Goety.init.ModAttributes;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.items.ModItems;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

@GameTestHolder("goety_nouveau_native_growth") @PrefixGameTestTemplate(false)
public final class NativeGrowthGameTests {
    static ServerPlayer player(GameTestHelper h) {
        var p=net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"NativeGrowth"));
        var mana=com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(p).orElseThrow(IllegalStateException::new);mana.setMaxMana(10000);mana.setMana(10000);return p;
    }
    static void attributes(ServerPlayer p) {
        for(var attribute:java.util.List.of(ModAttributes.SPELL_POTENCY.get(), ModAttributes.NECROMANCY_POTENCY.get(), ModAttributes.FROST_POTENCY.get(), ModAttributes.NETHER_POTENCY.get(), ModAttributes.VOID_POTENCY.get(), ModAttributes.WIND_POTENCY.get())) p.getAttribute(attribute).setBaseValue(2);
        p.getAttribute(ModAttributes.SPELL_DURATION.get()).setBaseValue(3);
        p.getAttribute(ModAttributes.SPELL_RANGE.get()).setBaseValue(4);
        p.getAttribute(ModAttributes.SPELL_RADIUS.get()).setBaseValue(1);
        p.getAttribute(ModAttributes.SPELL_BURNING.get()).setBaseValue(2);
        p.getAttribute(ModAttributes.SPELL_VELOCITY.get()).setBaseValue(1);
    }
    static void focus(GameTestHelper h,ServerPlayer p,boolean enchanted) {
        var f=new ItemStack(ModItems.SOUL_BOLT_FOCUS.get());
        if(enchanted) {
            f.enchant(ModEnchantments.POTENCY.get(),2);
            f.enchant(ModEnchantments.DURATION.get(),1);
            f.enchant(ModEnchantments.RADIUS.get(),1);
            f.enchant(ModEnchantments.VELOCITY.get(),1);
        }
        var wand=new ItemStack(ModItems.DARK_WAND.get());
        com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler.get(wand).insertItem(f);
        p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,wand);
    }
    @GameTest(template="empty",timeoutTicks=80,batch="native_growth_0")
    public static void oldProjectileAttributes(GameTestHelper h) {
        NativeGrowthCases.oldProjectileAttributes(h,player(h),NativeGrowthGameTests::attributes,(p,e)->focus(h,p,e));
    }
    @GameTest(template="empty",timeoutTicks=80,batch="native_growth_1")
    public static void oldEffectAttributes(GameTestHelper h) {
        NativeGrowthCases.oldEffectAttributes(h,player(h),NativeGrowthGameTests::attributes,(p,e)->focus(h,p,e));
    }
    @GameTest(template="empty",timeoutTicks=80,batch="native_growth_2")
    public static void nativeCallbacksDoNotDoubleGrowth(GameTestHelper h) {
        NativeGrowthCases.nativeCallbacksDoNotDoubleGrowth(h,player(h),NativeGrowthGameTests::attributes,(p,e)->focus(h,p,e));
    }
    @GameTest(template="empty",timeoutTicks=80,batch="native_growth_3")
    public static void nativeEnchantBillingSwitch(GameTestHelper h) {
        NativeGrowthCases.nativeEnchantBillingSwitch(h,player(h),(p,e)->focus(h,p,e),stack->stack.enchant(ModEnchantments.POTENCY.get(),2));
    }
    @GameTest(template="empty",timeoutTicks=80,batch="native_growth_4")
    public static void discountsFailedActionsAndAccounts(GameTestHelper h) {
        var p=player(h);var mana=com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(p).orElseThrow(IllegalStateException::new);
        NativeGrowthCases.discountsFailedActionsAndAccounts(h,p,(who,e)->focus(h,who,e),mana::setMana,()->(int)mana.getCurrentMana());
    }
}
