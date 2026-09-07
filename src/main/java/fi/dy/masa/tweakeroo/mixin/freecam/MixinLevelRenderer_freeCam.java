package fi.dy.masa.tweakeroo.mixin.freecam;

import org.joml.Vector4f;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraUtils;

@Mixin(value = LevelRenderer.class)
public abstract class MixinLevelRenderer_freeCam
{
    @Inject(method = "render",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                     args = "ldc=repositionCamera"))
    private void tweakeroo_preSetupTerrain(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, com.mojang.renderpearl.api.buffers.GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci)
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
    private void tweakeroo_postSetupTerrain(GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, com.mojang.renderpearl.api.buffers.GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci)
    {
        CameraUtils.setFreeCameraSpectator(false);
    }
}
