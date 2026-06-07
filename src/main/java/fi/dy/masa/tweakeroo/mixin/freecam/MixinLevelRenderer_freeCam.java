package fi.dy.masa.tweakeroo.mixin.freecam;

import com.llamalad7.mixinextras.sugar.Local;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraUtils;

@Mixin(value = LevelRenderer.class, priority = 1005)
public abstract class MixinLevelRenderer_freeCam
{
    @Shadow
    private @Nullable ViewArea viewArea;
    @Unique private int lastUpdatePosX;
    @Unique private int lastUpdatePosZ;

    @Inject(method = "render",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                     args = "ldc=repositionCamera"))
    private void tweakeroo_preSetupTerrain(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            CameraUtils.setFreeCameraSpectator(true);
        }
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                     args = "ldc=compileSections"))
    private void tweakeroo_postSetupTerrain(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci)
    {
        CameraUtils.setFreeCameraSpectator(false);
    }

    // These injections will fail when Sodium is present, but the Free Camera
    // rendering seems to work fine with Sodium without these anyway
    @Inject(method = "repositionCamera", at = @At(value = "HEAD"))
    private void tweakeroo_rebuildChunksAroundCamera1(CameraRenderState camera, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            // Hold on to the previous update position before it gets updated
            if (this.viewArea != null)
            {
                SectionPos lastCameraPos = this.viewArea.getCameraSectionPos();
                this.lastUpdatePosX = lastCameraPos.x();
                this.lastUpdatePosZ = lastCameraPos.z();
            }
        }
    }

	// cullTerrain -> method_74752
    // These injections will fail when Sodium is present, but the Free Camera
    // rendering seems to work fine with Sodium without these anyway
    @Inject(method = "repositionCamera",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/renderer/ViewArea;repositionCamera(Lnet/minecraft/core/SectionPos;)Z"
            ))
    private void tweakeroo_rebuildChunksAroundCamera2(CameraRenderState camera, CallbackInfo ci,
                                                      @Local(name = "cameraPos") Vec3 cameraPos)
    {
        // Mark the chunks at the edge of the free camera's render range for rebuilding
        // when the camera moves around.
        // Normally these rebuilds would happen when the server sends chunks to the client when the player moves around.
        // But in Free Camera mode moving the ViewFrustum/BuiltChunkStorage would cause the terrain
        // to disappear because of no dirty marking calls from chunk loading.
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            int x = Mth.floor(cameraPos.x) >> 4;
            int z = Mth.floor(cameraPos.z) >> 4;
            CameraUtils.markChunksForRebuild(x, z, this.lastUpdatePosX, this.lastUpdatePosZ);
            // Could send this to Servux in the future
        }
    }
}
