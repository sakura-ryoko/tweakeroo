package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionSeedParticle;
import net.minecraft.client.particle.NoRenderParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(HugeExplosionSeedParticle.class)
public class MixinHugeExplosionSeedParticle extends NoRenderParticle
{
    protected MixinHugeExplosionSeedParticle(ClientLevel clientWorld, double d, double e, double f)
    {
        super(clientWorld, d, e, f);
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 6))
    private int addParticleModify(int constant)
    {
        if (FeatureToggle.TWEAK_EXPLOSION_REDUCED_PARTICLES.getBooleanValue())
        {
            this.age = 1;
            this.lifetime = 2;
            return 1;
        }

        return constant;
    }
}
