package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

@Mixin(value = WorldRenderer.class, priority = 1001)
public abstract class MixinWorldRenderer
{
    // This Injection point also disables the World Border Rendering.

//    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
//    private void cancelRainRender(FrameGraphBuilder frameGraphBuilder, Vec3d cameraPos, float tickProgress, GpuBufferSlice fog, CallbackInfo ci)
//    {
//        if (Configs.Disable.DISABLE_RAIN_EFFECTS.getBooleanValue())
//        {
//            ci.cancel();
//        }
//    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "spawnParticle(Lnet/minecraft/particle/ParticleEffect;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void spawnParticleInject(ParticleEffect parameters, boolean alwaysSpawn, boolean canSpawnOnMinimal, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> ci)
    {
        if (Configs.Generic.SELECTIVE_BLOCKS_HIDE_PARTICLES.getBooleanValue())
        {
            if (!RenderTweaks.isPositionValidForRendering(BlockPos.ofFloored(x, y, z)))
            {
                ci.setReturnValue(null);
                ci.cancel();
            }
        }
    }
}
