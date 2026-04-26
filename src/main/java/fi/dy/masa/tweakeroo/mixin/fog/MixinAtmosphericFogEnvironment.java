package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = AtmosphericFogEnvironment.class, priority = 900)
public class MixinAtmosphericFogEnvironment
{
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void tweakeroo_redirectAtmosphericFog(FogData fog, Camera camera, ClientLevel level, float renderDistance,
                                                  DeltaTracker deltaTracker, CallbackInfo ci)
    {
		// Apparently, our old IMPL wasn't powerful enough.
        if (Configs.Disable.DISABLE_ATMOSPHERIC_FOG.getBooleanValue())
        {
//	        fog.skyEnd = Float.MAX_VALUE;
	        fog.environmentalStart = Float.MAX_VALUE - 4.0F;
	        fog.environmentalEnd = Float.MAX_VALUE;
	        fog.renderDistanceStart = Float.MAX_VALUE - 4.0F;
	        fog.renderDistanceEnd = Float.MAX_VALUE;
        }
    }
}
