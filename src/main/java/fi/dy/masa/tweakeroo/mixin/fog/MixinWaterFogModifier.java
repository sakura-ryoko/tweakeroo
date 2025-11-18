package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.WaterFogModifier;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;

@Mixin(WaterFogModifier.class)
public class MixinWaterFogModifier
{
    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectWaterFog(FogData data, Camera camera, ClientWorld clientWorld, float f, RenderTickCounter renderTickCounter, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_WATER_VISIBILITY.getBooleanValue())
        {
            if (data.environmentalStart > 0.0F)
            {
                data.environmentalStart = -8.0F;
            }

            final float adjusted = RenderUtils.calculateLiquidFogDistance(camera.getFocusedEntity(), data.environmentalEnd, true);

            if (data.environmentalEnd != adjusted)
            {
                data.environmentalEnd = adjusted;
            }
        }
    }
}
