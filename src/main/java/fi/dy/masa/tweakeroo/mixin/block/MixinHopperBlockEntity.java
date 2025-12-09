package fi.dy.masa.tweakeroo.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

/**
 * <a href="https://github.com/kikugie/stackable-shulkers-fix">...</a> by KikuGie
 * Priority 999 if installed with stackable-shulkers-fix
 */
@Mixin(value = HopperBlockEntity.class, priority = 999)
public class MixinHopperBlockEntity
{
    @WrapOperation(
            method = "inventoryFull",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I")
    )
    private int modifyShulkerMaxCount(ItemStack instance, Operation<Integer> original)
    {
        if (Configs.Fixes.STACKABLE_SHULKERS_IN_HOPPER_FIX.getBooleanValue())
        {
            return MiscUtils.isShulkerBox(instance) ? instance.getCount() : original.call(instance);
        }

        return original.call(instance);
    }

    @WrapOperation(
            method = "isFullContainer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I")
    )
    private static int modifyShulkerMaxCountStatic(ItemStack instance, Operation<Integer> original)
    {
        if (Configs.Fixes.STACKABLE_SHULKERS_IN_HOPPER_FIX.getBooleanValue())
        {
            return MiscUtils.isShulkerBox(instance) ? 1 : original.call(instance);
        }

        return original.call(instance);
    }

    @Inject(
            method = "canMergeItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelItemMerging(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir)
    {
        if (Configs.Fixes.STACKABLE_SHULKERS_IN_HOPPER_FIX.getBooleanValue())
        {
            if (MiscUtils.isShulkerBox(first) || MiscUtils.isShulkerBox(second)) cir.setReturnValue(false);
        }
    }
}
