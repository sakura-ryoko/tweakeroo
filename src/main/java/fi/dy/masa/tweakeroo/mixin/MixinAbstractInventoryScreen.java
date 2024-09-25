package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(AbstractInventoryScreen.class)
public abstract class MixinAbstractInventoryScreen
{
    private MixinAbstractInventoryScreen(HandledScreen<?> handledScreen)
    {
        super();
    }

    @Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
    private void disableStatusEffectRendering(DrawContext drawContext, int mouseX, int mouseY, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_INVENTORY_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
