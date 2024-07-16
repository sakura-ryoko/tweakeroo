package fi.dy.masa.tweakeroo.mixin;

import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class MixinItemStack
{
    @Shadow
    public abstract Item getItem();

    @Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
    public void getMaxStackSizeStackSensitive(CallbackInfoReturnable<Integer> cir)
    {
        if (FeatureToggle.TWEAK_SHULKERBOX_STACKING.getBooleanValue() &&
            this.getItem() instanceof BlockItem block &&
            block.getBlock() instanceof ShulkerBoxBlock &&
            InventoryUtils.shulkerBoxHasItems((ItemStack) (Object) this) == false)
        {
            cir.setReturnValue(64);
        }
    }
}
