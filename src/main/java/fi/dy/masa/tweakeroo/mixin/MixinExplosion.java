package fi.dy.masa.tweakeroo.mixin;

import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.explosion.Explosion;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(Explosion.class)
public abstract class MixinExplosion
{
    /**
     * I don't know how long this was broken (1.20.4 also?), but let's fix it.
     */
    @ModifyArg(method = "affectWorld",
    at = @At(value = "INVOKE",
    target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
    private ParticleEffect addParticleModify(ParticleEffect parameters)
    {
        if (FeatureToggle.TWEAK_EXPLOSION_REDUCED_PARTICLES.getBooleanValue())
        {
            return ParticleTypes.EXPLOSION;
        }

        return ParticleTypes.EXPLOSION_EMITTER;
    }

}
/*
@Redirect(method = "affectWorld",
              slice = @Slice(
                            from = @At("HEAD"),
                            to = @At(value = "FIELD",
                                     target = "Lnet/minecraft/world/explosion/Explosion;affectedBlocks:Lit/unimi/dsi/fastutil/objects/ObjectArrayList;")),
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/particle/ParticleTypes;EXPLOSION_EMITTER:Lnet/minecraft/particle/DefaultParticleType;"))
 */