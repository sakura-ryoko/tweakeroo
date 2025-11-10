package fi.dy.masa.tweakeroo.mixin.fog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

@Mixin(WaterFogEnvironment.class)
public class MixinWaterFogModifier
{
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void tweakeroo_redirectWaterFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientLevel world, float viewDistance, DeltaTracker tickCounter, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_WATER_VISIBILITY.getBooleanValue())
        {
            if (data.environmentalStart > 0.0F)
            {
                data.environmentalStart = -8.0F;
            }

            final float adjusted = RenderUtils.calculateLiquidFogDistance(cameraEntity, data.environmentalEnd, true);

            if (data.environmentalEnd != adjusted)
            {
                data.environmentalEnd = adjusted;
            }
        }
    }
}
