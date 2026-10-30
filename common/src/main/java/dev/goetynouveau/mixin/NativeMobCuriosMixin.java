package dev.goetynouveau.mixin;

import com.Polarice3.Goety.utils.CuriosFinder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.function.Predicate;

/** Read actual equipped Curios for non-player wearers; never grant virtual gear. */
@Mixin(value=CuriosFinder.class,remap=false)
public abstract class NativeMobCuriosMixin {
    @Inject(method="findCurio(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/item/ItemStack;",
            at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$mobPredicate(LivingEntity wearer,Predicate<ItemStack> filter,
                                                CallbackInfoReturnable<ItemStack> cir) {
        if(wearer!=null&&!(wearer instanceof Player))
            cir.setReturnValue(CuriosApi.getCuriosInventory(wearer)
                    .map(inv->inv.findFirstCurio(filter).map(SlotResult::stack).orElse(ItemStack.EMPTY))
                    .orElse(ItemStack.EMPTY));
    }

    @Inject(method="findCurio(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/item/ItemStack;",
            at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$mobItem(LivingEntity wearer,Item item,CallbackInfoReturnable<ItemStack> cir) {
        if(wearer!=null&&!(wearer instanceof Player))
            cir.setReturnValue(CuriosApi.getCuriosInventory(wearer)
                    .map(inv->inv.findFirstCurio(item).map(SlotResult::stack).orElse(ItemStack.EMPTY))
                    .orElse(ItemStack.EMPTY));
    }
}
