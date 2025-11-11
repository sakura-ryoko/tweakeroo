package fi.dy.masa.tweakeroo.mixin.fog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Mixin(LavaFogModifier.class)
public class MixinLavaFogModifier
{
    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectLavaFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            if (data.environmentalStart == 0.25F)
            {
                data.environmentalStart = 0.0F;
            }

            final float adjusted = RenderUtils.calculateLiquidFogDistance(cameraEntity, data.environmentalEnd, false);

            if (data.environmentalEnd != adjusted)
            {
                data.environmentalEnd = adjusted;
            }
        }
    }
}
