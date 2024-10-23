package fi.dy.masa.tweakeroo.mixin;

import net.minecraft.client.particle.ExplosionEmitterParticle;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(ExplosionEmitterParticle.class)
public class MixinExplosionEmitterParticle extends NoRenderParticle
{
    protected MixinExplosionEmitterParticle(ClientWorld clientWorld, double d, double e, double f)
    {
        super(clientWorld, d, e, f);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 6))
    private int addParticleModify(int constant)
    {
        if (FeatureToggle.TWEAK_EXPLOSION_REDUCED_PARTICLES.getBooleanValue())
        {
            this.age = 1;
            this.maxAge = 2;
            return 1;
        }

        return constant;
    }
}
