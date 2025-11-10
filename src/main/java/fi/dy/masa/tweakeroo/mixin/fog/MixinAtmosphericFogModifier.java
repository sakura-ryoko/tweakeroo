package fi.dy.masa.tweakeroo.mixin.fog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

@Mixin(AtmosphericFogEnvironment.class)
public class MixinAtmosphericFogModifier
{
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void tweakeroo_redirectAtmosphericFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientLevel world, float viewDistance, DeltaTracker tickCounter, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_ATMOSPHERIC_FOG.getBooleanValue())
        {
            data.environmentalStart = data.cloudEnd - 4.0F;
            data.environmentalEnd = data.cloudEnd;
        }
    }
}
