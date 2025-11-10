package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.platform.Window;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(Window.class)
public abstract class MixinWindow
{
    @Shadow public abstract int getScreenWidth();
    @Shadow public abstract int getScreenHeight();

    @Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_customGuiScaleGetScale(CallbackInfoReturnable<Integer> cir)
    {
        if (FeatureToggle.TWEAK_CUSTOM_INVENTORY_GUI_SCALE.getBooleanValue() &&
            Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>)
        {
            int scale = Configs.Generic.CUSTOM_INVENTORY_GUI_SCALE.getIntegerValue();

            if (scale > 0)
            {
                cir.setReturnValue(scale);
            }
        }
    }

    @Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_customGuiScaleGetWidth(CallbackInfoReturnable<Integer> cir)
    {
        if (FeatureToggle.TWEAK_CUSTOM_INVENTORY_GUI_SCALE.getBooleanValue() &&
            Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>)
        {
            int scale = Configs.Generic.CUSTOM_INVENTORY_GUI_SCALE.getIntegerValue();

            if (scale > 0)
            {
                cir.setReturnValue((int) Math.ceil((double) this.getScreenWidth() / scale));
            }
        }
    }

    @Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_customGuiScaleGetHeight(CallbackInfoReturnable<Integer> cir)
    {
        if (FeatureToggle.TWEAK_CUSTOM_INVENTORY_GUI_SCALE.getBooleanValue() &&
            Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>)
        {
            int scale = Configs.Generic.CUSTOM_INVENTORY_GUI_SCALE.getIntegerValue();

            if (scale > 0)
            {
                cir.setReturnValue((int) Math.ceil((double) this.getScreenHeight() / scale));
            }
        }
    }
}
