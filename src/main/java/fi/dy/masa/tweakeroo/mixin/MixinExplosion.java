package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.explosion.Explosion;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(Explosion.class)
public abstract class MixinExplosion
{
    @Shadow @Final private ParticleEffect particle;

    @Shadow @Final private ParticleEffect emitterParticle;

    @ModifyArg(method = "affectWorld",
               at = @At(value = "INVOKE",
               target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
    private ParticleEffect addParticleModify(ParticleEffect original)
    {
        if (FeatureToggle.TWEAK_EXPLOSION_REDUCED_PARTICLES.getBooleanValue())
        {
            return particle;
        }

        return original;
    }
}
