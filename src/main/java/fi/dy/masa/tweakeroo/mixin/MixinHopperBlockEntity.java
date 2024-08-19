package fi.dy.masa.tweakeroo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.util.MiscUtils;

/**
 * <a href="https://github.com/kikugie/stackable-shulkers-fix">...</a> by KikuGie
 * Priority 999 if installed with stackable-shulkers-fix
 */
@Mixin(value = HopperBlockEntity.class, priority = 999)
public class MixinHopperBlockEntity
{
    @WrapOperation(
            method = "isFull",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private int modifyShulkerMaxCount(ItemStack instance, Operation<Integer> original)
    {
        return MiscUtils.isShulkerBox(instance) ? instance.getCount() : original.call(instance);
    }

    @WrapOperation(
            method = "isInventoryFull",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private static int modifyShulkerMaxCountStatic(ItemStack instance, Operation<Integer> original)
    {
        return MiscUtils.isShulkerBox(instance) ? 1 : original.call(instance);
    }

    @Inject(
            method = "canMergeItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelItemMerging(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir)
    {
        if (MiscUtils.isShulkerBox(first) || MiscUtils.isShulkerBox(second)) cir.setReturnValue(false);
    }
}
