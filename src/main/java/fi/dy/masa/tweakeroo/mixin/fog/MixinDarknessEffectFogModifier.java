package fi.dy.masa.tweakeroo.mixin.fog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.DarknessFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

@Mixin(DarknessFogEnvironment.class)
public class MixinDarknessEffectFogModifier
{
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void tweakeroo_redirectDarknessFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientLevel world, float viewDistance, DeltaTracker tickCounter, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_DARKNESS_VISIBILITY.getBooleanValue())
        {
            // Trying not to make this overpowered...
            // To me, this should suffice to
            // improve a players' Quality of Life of they also
            // change the Accessibility Setting Pulse Strength.
            // I wanted to make this bound to some enchantment if I could;
            // but I couldn't think of a good one to counter this effect.
            // The actual strength value returned by Vanilla here
            // tends to be somewhat random and unpredictable and varies.
            // Someone could remove the fog by changing the 'adj' value higher, but
            // that would be no fun; now would it? :)
            final float adj = data.skyEnd * 3.0F;

            data.environmentalStart = adj * 0.75F;
            data.environmentalEnd = adj;
            data.skyEnd = adj;
            data.cloudEnd = adj;
        }
    }
}
