package fi.dy.masa.tweakeroo.mixin.freecam;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;

@Mixin(value = GameRenderer.class, priority = 1005)
public abstract class MixinGameRenderer_freeCam
{
    @Shadow @Final private Minecraft minecraft;

    @ModifyExpressionValue(method = "getFov", at = @At(value = "CONSTANT", args = "floatValue=70.0"))
    private float tweakeroo_applyFreeCameraFov(float original)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            return ((float) this.minecraft.options.fov().get());
        }

        return original;
    }

    @ModifyVariable(method = "getFov", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    private boolean tweakeroo_freezeFovOnFreeCamera(boolean value)
    {
        return !FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() && value;
    }

    @Redirect(method = "pick(F)V", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity tweakeroo_overrideCameraEntityForRayTrace(Minecraft mc)
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
