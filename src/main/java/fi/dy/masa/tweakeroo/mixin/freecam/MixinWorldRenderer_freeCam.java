package fi.dy.masa.tweakeroo.mixin.freecam;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.objectweb.asm.Opcodes;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Mixin(value = WorldRenderer.class, priority = 1005)
public abstract class MixinWorldRenderer_freeCam
{
    @Shadow private int cameraChunkX;
    @Shadow private int cameraChunkZ;

    @Unique private int lastUpdatePosX;
    @Unique private int lastUpdatePosZ;

    @Inject(method = "render", at = @At(value = "INVOKE_STRING",
                                        target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=cullTerrain"))
    private void preSetupTerrain(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
                                 Camera camera, Matrix4f matrix4f, Matrix4f projectionMatrix, Matrix4f matrix4f2,
                                 GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            CameraUtils.setFreeCameraSpectator(true);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE_STRING",
                                        target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=compileSections"))
    private void postSetupTerrain(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
                                  Camera camera, Matrix4f matrix4f, Matrix4f projectionMatrix, Matrix4f matrix4f2,
                                  GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl, CallbackInfo ci)
    {
        CameraUtils.setFreeCameraSpectator(false);
    }

    // Allow rendering the client player entity by spoofing one of the entity rendering conditions while in Free Camera mode
    @Redirect(method = "fillEntityRenderStates", require = 0, at = @At(value = "INVOKE",
                                                                    target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 3))
    private Entity allowRenderingClientPlayerInFreeCameraMode(Camera camera)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            return MinecraftClient.getInstance().player;
        }

        return camera.getFocusedEntity();
    }

	// cullTerrain -> method_74752
    // These injections will fail when Sodium is present, but the Free Camera
    // rendering seems to work fine with Sodium without these anyway
    @Inject(method = "method_74752", require = 0,
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                     target = "Lnet/minecraft/client/render/WorldRenderer;lastCameraX:D"))
    private void rebuildChunksAroundCamera1(
            Camera camera, Frustum frustum, boolean bl, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            // Hold on to the previous update position before it gets updated
            this.lastUpdatePosX = this.cameraChunkX;
            this.lastUpdatePosZ = this.cameraChunkZ;
        }
    }

	// cullTerrain -> method_74752
    // These injections will fail when Sodium is present, but the Free Camera
    // rendering seems to work fine with Sodium without these anyway
    @Inject(method = "method_74752", require = 0,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/render/BuiltChunkStorage;updateCameraPosition(Lnet/minecraft/util/math/ChunkSectionPos;)V"))
    private void rebuildChunksAroundCamera2(
            Camera camera, Frustum frustum, boolean bl, CallbackInfo ci)
    {
        // Mark the chunks at the edge of the free camera's render range for rebuilding
        // when the camera moves around.
        // Normally these rebuilds would happen when the server sends chunks to the client when the player moves around.
        // But in Free Camera mode moving the ViewFrustum/BuiltChunkStorage would cause the terrain
        // to disappear because of no dirty marking calls from chunk loading.
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            int x = MathHelper.floor(camera.getPos().x) >> 4;
            int z = MathHelper.floor(camera.getPos().z) >> 4;
            CameraUtils.markChunksForRebuild(x, z, this.lastUpdatePosX, this.lastUpdatePosZ);
            // Could send this to Servux in the future
        }
    }
}
