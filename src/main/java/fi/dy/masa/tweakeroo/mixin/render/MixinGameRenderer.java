package fi.dy.masa.tweakeroo.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Callbacks;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(value = GameRenderer.class, priority = 990)
public abstract class MixinGameRenderer
{
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private Camera mainCamera;
    @Shadow @Final private GameRenderState gameRenderState;

    @Unique private float realYaw;
    @Unique private float realPitch;

    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_onRenderWorld(DeltaTracker deltaTracker, CallbackInfo ci)
    {
        if (Callbacks.skipWorldRendering)
        {
            ci.cancel();
        }
    }

    @Inject(method = "update", at = @At(value = "HEAD"))
    private void tweakeroo_overrideRenderViewEntityPre(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_ELYTRA_CAMERA.getBooleanValue() && Hotkeys.ELYTRA_CAMERA.getKeybind().isKeybindHeld())
        {
            Entity entity = this.minecraft.getCameraEntity();

            if (entity != null)
            {
                this.realYaw = entity.getYRot();
                this.realPitch = entity.getXRot();
                MiscUtils.setEntityRotations(entity, CameraUtils.getCameraYaw(), CameraUtils.getCameraPitch());
//                this.mainCamera.update(deltaTracker);
            }
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void tweakeroo_onRenderLevelPost(DeltaTracker deltaTracker, CallbackInfo ci,
                                             @Local(name = "cameraState") CameraRenderState cameraState)
    {
        if (FeatureToggle.TWEAK_F3_CURSOR.getBooleanValue())
        {
            this.minecraft.getDebugOverlay().render3dCrosshair(cameraState, this.gameRenderState.windowRenderState.guiScale);
        }

        if (FeatureToggle.TWEAK_ELYTRA_CAMERA.getBooleanValue() && Hotkeys.ELYTRA_CAMERA.getKeybind().isKeybindHeld())
        {
            Entity entity = this.minecraft.getCameraEntity();

            if (entity != null)
            {
                MiscUtils.setEntityRotations(entity, this.realYaw, this.realPitch);
                this.mainCamera.update(deltaTracker);
            }
        }
    }
}
