package fi.dy.masa.tweakeroo.mixin.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;

@Mixin(value = WorldRenderer.class, priority = 1001)
public abstract class MixinWorldRenderer
{
    @Shadow
    private @Nullable ClientWorld world;
    @Unique private boolean hasSkylight;
    @Unique private Vec3d color;

    @Inject(method = "tickRainSplashing", at = @At("HEAD"), cancellable = true) // renderRain
    private void tweakeroo_cancelRainRender(Camera camera, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_RAIN_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_cancelRainRender(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci)
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
    private void tweakeroo_spawnParticleInject(ParticleEffect parameters, boolean alwaysSpawn, boolean canSpawnOnMinimal, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir)
    {
        if (Configs.Generic.SELECTIVE_BLOCKS_HIDE_PARTICLES.getBooleanValue())
        {
            if (!RenderTweaks.isPositionValidForRendering(BlockPos.ofFloored(x, y, z)))
            {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }

    // fixme
//    @Redirect(method = "renderSky",
//              at = @At(value = "INVOKE",
//                       target = "Lnet/minecraft/client/world/ClientWorld;getSkyColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
//    private Vec3d tweakeroo_adjustFogColor1(ClientWorld instance, Vec3d cameraPos, float tickDelta)
//    {
//        this.color = instance.getSkyColor(cameraPos, tickDelta);
//        this.hasSkylight = instance.getDimension().hasSkyLight();
//
//        return this.color;
//    }
//
//    @Redirect(method = "renderSky",
//              at = @At(value = "INVOKE",
//                       target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFogColor()V"))
//    private void tweakeroo_adjustFogColor2()
//    {
//        if (FeatureToggle.TWEAK_MATCHING_SKY_FOG.getBooleanValue() && this.hasSkylight)
//        {
//            // x = red, y = green, z = blue (alpha 255f)
//            RenderSystem.setShaderFogColor((float) this.color.x, (float) this.color.y, (float) this.color.z);
//        }
//        else
//        {
//            BackgroundRenderer.applyFogColor();
//        }
//    }
}
