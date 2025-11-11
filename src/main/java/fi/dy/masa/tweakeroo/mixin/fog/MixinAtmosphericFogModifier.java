package fi.dy.masa.tweakeroo.mixin.fog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Mixin(AtmosphericFogModifier.class)
public class MixinAtmosphericFogModifier
{
    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectAtmosphericFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_ATMOSPHERIC_FOG.getBooleanValue())
        {
            data.environmentalStart = data.cloudEnd - 4.0F;
            data.environmentalEnd = data.cloudEnd;
        }
    }
}
