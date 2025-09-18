package fi.dy.masa.tweakeroo.mixin.render;

import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class MixinParticleManager
{
    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
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
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void tweakeroo_spawnParticleInject(ParticleEffect parameters, double x, double y, double z,
                                               double velocityX, double velocityY, double velocityZ,
                                               CallbackInfoReturnable<Particle> cir)
    {
        if (Configs.Generic.SELECTIVE_BLOCKS_HIDE_PARTICLES.getBooleanValue())
        {
            if (!RenderTweaks.isPositionValidForRendering(BlockPos.ofFloored(x, y, z)))
            {
                cir.setReturnValue(null);
            }
        }
    }
}
