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

@Mixin(value = LavaFogEnvironment.class, priority = 900)
public class MixinLavaFogEnvironment
{
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void tweakeroo_redirectLavaFog(FogData fog, Camera camera, ClientLevel level, float renderDistance,
                                           DeltaTracker deltaTracker, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            if (fog.environmentalStart == 0.25F)
            {
                fog.environmentalStart = 0.0F;
            }

            final float adjusted = RenderUtils.calculateLiquidFogDistance(camera.entity(), fog.environmentalEnd, false);

            if (fog.environmentalEnd != adjusted)
            {
                fog.environmentalEnd = adjusted;
            }
        }
    }
}
