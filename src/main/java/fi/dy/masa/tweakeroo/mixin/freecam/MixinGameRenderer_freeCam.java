package fi.dy.masa.tweakeroo.mixin.freecam;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = GameRenderer.class, priority = 1005)
public abstract class MixinGameRenderer_freeCam
{
    @Shadow @Final private MinecraftClient client;

    @ModifyExpressionValue(method = "getFov", at = @At(value = "CONSTANT", args = "floatValue=70.0"))
    private float tweakeroo_applyFreeCameraFov(float original)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            return ((float) this.client.options.getFov().getValue());
        }

        return original;
    }

    @ModifyVariable(method = "getFov", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    private boolean tweakeroo_freezeFovOnFreeCamera(boolean value)
    {
        return !FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() && value;
    }

    @Redirect(method = "updateCrosshairTarget", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
    private Entity tweakeroo_overrideCameraEntityForRayTrace(MinecraftClient mc)
    {
        // Return the real player for the hit target ray tracing if the
        // player inputs option is enabled in Free Camera mode.
        // Normally in Free Camera mode the Tweakeroo CameraEntity is set as the
        // render view/camera entity, which would then also ray trace from the camera point of view.
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
            Configs.Generic.FREE_CAMERA_PLAYER_INPUTS.getBooleanValue()  &&
            !FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue() &&
            mc.player != null)
        {
            return mc.player;
        }

        return mc.getCameraEntity();
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_removeHandRendering(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			!Configs.Generic.FREE_CAMERA_SHOW_HANDS.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
