package fi.dy.masa.tweakeroo.mixin.freecam;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = GameRenderer.class, priority = 850)
public abstract class MixinGameRenderer_freeCam
{
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_removeHandRendering(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_HANDS.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
