package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Callbacks;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(value = GameRenderer.class, priority = 1001)
public abstract class MixinGameRenderer
{
    @Shadow @Final private MinecraftClient client;

    @Unique private float realYaw;
    @Unique private float realPitch;

    @Inject(method = "renderWorld", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_onRenderWorld(CallbackInfo ci)
    {
        if (Callbacks.skipWorldRendering)
        {
            ci.cancel();
        }
    }

    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_applyZoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir)
    {
        if (MiscUtils.isZoomActive())
        {
            cir.setReturnValue((float) Configs.Generic.ZOOM_FOV.getDoubleValue());
        }
    }

    @Inject(method = "renderWorld", at = @At(
                value = "INVOKE", shift = Shift.AFTER,
                target = "Lnet/minecraft/client/render/GameRenderer;updateCrosshairTarget(F)V"))
    private void tweakeroo_overrideRenderViewEntityPre(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_ELYTRA_CAMERA.getBooleanValue() && Hotkeys.ELYTRA_CAMERA.getKeybind().isKeybindHeld())
        {
            Entity entity = this.client.getCameraEntity();

            if (entity != null)
            {
                this.realYaw = entity.getYaw();
                this.realPitch = entity.getPitch();
                MiscUtils.setEntityRotations(entity, CameraUtils.getCameraYaw(), CameraUtils.getCameraPitch());
            }
        }
    }

    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void tweakeroo_overrideRenderViewEntityPost(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_ELYTRA_CAMERA.getBooleanValue() && Hotkeys.ELYTRA_CAMERA.getKeybind().isKeybindHeld())
        {
            Entity entity = this.client.getCameraEntity();

            if (entity != null)
            {
                MiscUtils.setEntityRotations(entity, this.realYaw, this.realPitch);
            }
        }
    }
}
