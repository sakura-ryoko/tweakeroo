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
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;

@Mixin(value = WaterFogEnvironment.class, priority = 900)
public class MixinWaterFogEnvironment
{
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void tweakeroo_redirectWaterFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_WATER_VISIBILITY.getBooleanValue())
        {
            if (fog.environmentalStart > 0.0F)
            {
                fog.environmentalStart = -8.0F;
            }

            final float adjusted = RenderUtils.calculateLiquidFogDistance(camera.entity(), fog.environmentalEnd, true);

            if (fog.environmentalEnd != adjusted)
            {
                fog.environmentalEnd = adjusted;
            }
        }
    }
}
