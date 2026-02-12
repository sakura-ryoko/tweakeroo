package fi.dy.masa.tweakeroo.mixin.fog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;

@Mixin(AtmosphericFogEnvironment.class)
public class MixinAtmosphericFogEnvironment
{
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void tweakeroo_redirectAtmosphericFog(FogData data, Camera camera, ClientLevel clientWorld, float f,
                                                  DeltaTracker renderTickCounter, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_ATMOSPHERIC_FOG.getBooleanValue())
        {
			float limit = data.cloudEnd;
	        data.environmentalStart = limit - 4.0F;
	        data.environmentalEnd = limit;
        }
    }
}
