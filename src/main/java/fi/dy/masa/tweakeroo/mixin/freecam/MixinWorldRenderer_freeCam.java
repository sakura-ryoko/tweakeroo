package fi.dy.masa.tweakeroo.mixin.freecam;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.objectweb.asm.Opcodes;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Mixin(value = LevelRenderer.class, priority = 1005)
public abstract class MixinWorldRenderer_freeCam
{
    @Shadow private int lastCameraSectionX;
    @Shadow private int lastCameraSectionZ;

    @Unique private int lastUpdatePosX;
    @Unique private int lastUpdatePosZ;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING",
                                        target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=cullTerrain"))
    private void tweakeroo_preSetupTerrain(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline,
                                           Camera camera, Matrix4f matrix4f, Matrix4f projectionMatrix, Matrix4f matrix4f2,
                                           GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            CameraUtils.setFreeCameraSpectator(true);
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING",
                                        target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=compileSections"))
    private void tweakeroo_postSetupTerrain(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline,
                                            Camera camera, Matrix4f matrix4f, Matrix4f projectionMatrix, Matrix4f matrix4f2,
                                            GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl, CallbackInfo ci)
    {
        CameraUtils.setFreeCameraSpectator(false);
    }

    // Allow rendering the client player entity by spoofing one of the entity rendering conditions while in Free Camera mode
    @Redirect(method = "extractVisibleEntities", require = 0, at = @At(value = "INVOKE",
                                                                    target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;", ordinal = 3))
    private Entity tweakeroo_allowRenderingClientPlayerInFreeCameraMode(Camera camera)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            return Minecraft.getInstance().player;
        }

        return camera.entity();
    }

	// cullTerrain -> method_74752
    // These injections will fail when Sodium is present, but the Free Camera
    // rendering seems to work fine with Sodium without these anyway
    @Inject(method = "cullTerrain", require = 0,
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                     target = "Lnet/minecraft/client/renderer/LevelRenderer;prevCamX:D"))
    private void tweakeroo_rebuildChunksAroundCamera1(
            Camera camera, Frustum frustum, boolean bl, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            // Hold on to the previous update position before it gets updated
            this.lastUpdatePosX = this.lastCameraSectionX;
            this.lastUpdatePosZ = this.lastCameraSectionZ;
        }
    }

	// cullTerrain -> method_74752
    // These injections will fail when Sodium is present, but the Free Camera
    // rendering seems to work fine with Sodium without these anyway
    @Inject(method = "cullTerrain", require = 0,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/renderer/ViewArea;repositionCamera(Lnet/minecraft/core/SectionPos;)V"))
    private void tweakeroo_rebuildChunksAroundCamera2(Camera camera, Frustum frustum, boolean bl, CallbackInfo ci)
    {
        // Mark the chunks at the edge of the free camera's render range for rebuilding
        // when the camera moves around.
        // Normally these rebuilds would happen when the server sends chunks to the client when the player moves around.
        // But in Free Camera mode moving the ViewFrustum/BuiltChunkStorage would cause the terrain
        // to disappear because of no dirty marking calls from chunk loading.
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            int x = Mth.floor(camera.position().x) >> 4;
            int z = Mth.floor(camera.position().z) >> 4;
            CameraUtils.markChunksForRebuild(x, z, this.lastUpdatePosX, this.lastUpdatePosZ);
            // Could send this to Servux in the future
        }
    }
}
