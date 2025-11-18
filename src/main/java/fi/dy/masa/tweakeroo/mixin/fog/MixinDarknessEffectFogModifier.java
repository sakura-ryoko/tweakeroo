package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.DarknessEffectFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(DarknessEffectFogModifier.class)
public class MixinDarknessEffectFogModifier
{
    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectDarknessFog(FogData data, Camera camera, ClientWorld clientWorld, float f,
                                               RenderTickCounter renderTickCounter, CallbackInfo ci)
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
