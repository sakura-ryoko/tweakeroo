package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

@Mixin(value = WorldRenderer.class, priority = 1001)
public abstract class MixinWorldRenderer
{
    @Inject(method = "addWeatherParticlesAndSound", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_cancelWeatherParticlesAndSounds(Camera camera, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_RAIN_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_cancelRainRender(FrameGraphBuilder frameGraphBuilder, LightmapTextureManager lightmapTextureManager, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_RAIN_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "spawnParticle(Lnet/minecraft/particle/ParticleEffect;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_spawnParticleInject(ParticleEffect parameters, boolean alwaysSpawn, boolean canSpawnOnMinimal, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> ci)
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
