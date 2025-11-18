package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(AtmosphericFogModifier.class)
public class MixinAtmosphericFogModifier
{
    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectAtmosphericFog(FogData data, Camera camera, ClientWorld clientWorld, float f,
                                                  RenderTickCounter renderTickCounter, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_ATMOSPHERIC_FOG.getBooleanValue())
        {
			float limit = data.cloudEnd;
	        data.environmentalStart = limit - 4.0F;
	        data.environmentalEnd = limit;
        }
    }
}
