package fi.dy.masa.tweakeroo.mixin.render;

import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngine
{
    @Inject(method = "add(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void disableAllParticles(Particle effect, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_PARTICLES.getBooleanValue())
        {
            ci.cancel();
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void tweakeroo_spawnParticleInject(ParticleOptions parameters, double x, double y, double z,
                                               double velocityX, double velocityY, double velocityZ,
                                               CallbackInfoReturnable<Particle> cir)
    {
        if (Configs.Generic.SELECTIVE_BLOCKS_HIDE_PARTICLES.getBooleanValue())
        {
            if (!RenderTweaks.isPositionValidForRendering(BlockPos.containing(x, y, z)))
            {
                cir.setReturnValue(null);
            }
        }
    }
}
