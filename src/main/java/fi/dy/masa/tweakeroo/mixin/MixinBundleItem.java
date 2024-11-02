package fi.dy.masa.tweakeroo.mixin;

import java.util.Optional;

import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(BundleItem.class)
public class MixinBundleItem
{
    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_getTooltipData(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> cir)
    {
        if (FeatureToggle.TWEAK_BUNDLE_DISPLAY.getBooleanValue() &&
            Configs.Generic.BUNDLE_DISPLAY_REQUIRE_SHIFT.getBooleanValue() &&
            GuiBase.isShiftDown())
        {
            cir.setReturnValue(Optional.empty());
        }
        else if (FeatureToggle.TWEAK_BUNDLE_DISPLAY.getBooleanValue() &&
                !Configs.Generic.BUNDLE_DISPLAY_REQUIRE_SHIFT.getBooleanValue())
        {
            cir.setReturnValue(Optional.empty());
        }
    }
}
