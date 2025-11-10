package fi.dy.masa.tweakeroo.mixin.fog;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AirBasedFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AirBasedFogEnvironment.class)
public class MixinStandardFogModifier {

    @Inject(method = "getBaseColor", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_adjustFogColor(ClientLevel world, Camera camera, int viewDistance, float skyDarkness, CallbackInfoReturnable<Integer> cir)
    {
        if (FeatureToggle.TWEAK_MATCHING_SKY_FOG.getBooleanValue())
        {
            if (world.dimensionType().hasSkyLight())
            {
                int color = world.getSkyColor(camera.getPosition(), skyDarkness);
                cir.setReturnValue(color);
            }
        }
    }
}
