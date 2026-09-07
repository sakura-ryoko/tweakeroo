package fi.dy.masa.tweakeroo.mixin.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DebugCrosshairRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
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
    @Shadow @Final private DebugCrosshairRenderer debugCrosshairRenderer;
    @Shadow @Final private RenderTarget hud3DTarget;
    @Shadow @Final private RenderTarget mainRenderTarget;
    @Unique private float realYaw;
    @Unique private float realPitch;

    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_onRenderWorld(CallbackInfo ci)
    {
        if (Callbacks.skipWorldRendering)
        {
            ci.cancel();
        }
    }

    @Inject(method = "update", at = @At(value = "HEAD"))
    private void tweakeroo_overrideRenderViewEntityPre(DeltaTracker deltaTracker, CallbackInfo ci)
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
    private void tweakeroo_onRenderLevelPost(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_ELYTRA_CAMERA.getBooleanValue() && Hotkeys.ELYTRA_CAMERA.getKeybind().isKeybindHeld())
        {
            Entity entity = this.minecraft.getCameraEntity();

            if (entity != null)
            {
                MiscUtils.setEntityRotations(entity, this.realYaw, this.realPitch);
                this.mainCamera.update(this.minecraft.getDeltaTracker());
            }
        }
    }

    @Inject(method = "render3dHud",
            at = @At(value = "INVOKE",
                     target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFog(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V",
                     shift = At.Shift.AFTER
            )
    )
    private void tweakeroo_render3dCursorAlways(CameraRenderState cameraState, PlayerRenderState playerState,
                                                OptionsRenderState optionsState, boolean consistentDepthRequired, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_F3_CURSOR.getBooleanValue() &&
                (!this.gameRenderState.levelRenderState.render3dCrosshair || !optionsState.cameraType.isFirstPerson() || this.gameRenderState.guiRenderState.isHudHidden))
        {
            GpuTextureView depthTextureView = consistentDepthRequired ? this.hud3DTarget.getDepthTextureView() : this.mainRenderTarget.getDepthTextureView();

            if (this.mainRenderTarget.getColorTextureView() != null && depthTextureView != null)
            {
                this.debugCrosshairRenderer.render(cameraState, this.gameRenderState.windowRenderState.guiScale, this.mainRenderTarget.getColorTextureView(), depthTextureView);
            }
        }
    }
}
