package fi.dy.masa.tweakeroo.mixin.fog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;

@Mixin(LavaFogEnvironment.class)
public class MixinLavaFogEnvironment
{
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void tweakeroo_redirectLavaFog(FogData data, Camera camera, ClientLevel clientWorld, float f,
                                           DeltaTracker renderTickCounter, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            if (data.environmentalStart == 0.25F)
            {
                data.environmentalStart = 0.0F;
            }

            final float adjusted = RenderUtils.calculateLiquidFogDistance(camera.entity(), data.environmentalEnd, false);

            if (data.environmentalEnd != adjusted)
            {
                data.environmentalEnd = adjusted;
            }
        }
    }
}
