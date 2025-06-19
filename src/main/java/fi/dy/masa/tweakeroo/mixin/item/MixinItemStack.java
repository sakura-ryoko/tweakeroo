package fi.dy.masa.tweakeroo.mixin.item;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(ItemStack.class)
public abstract class MixinItemStack
{
    @Shadow public abstract Item getItem();
    @Shadow public abstract ComponentMap getComponents();

    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    public void getMaxStackSizeStackSensitive(CallbackInfoReturnable<Integer> cir)
    {
        if (FeatureToggle.TWEAK_SHULKERBOX_STACKING.getBooleanValue() &&
            this.getItem() instanceof BlockItem block &&
            block.getBlock() instanceof ShulkerBoxBlock &&
            InventoryUtils.shulkerBoxHasItems((ItemStack) (Object) this) == false)
        {
            if (this.getComponents().getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) < Configs.Internal.SHULKER_MAX_STACK_SIZE.getIntegerValue())
            {
                cir.setReturnValue(Configs.Internal.SHULKER_MAX_STACK_SIZE.getIntegerValue());
            }
        }
    }
}
