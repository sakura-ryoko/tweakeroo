package fi.dy.masa.tweakeroo.mixin;

import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(WeatherRendering.class)
public class MixinWeatherRendering
{
    // TODO --> tickRainSplashing
    @Inject(method = "method_62319", at = @At("HEAD"), cancellable = true) // renderRain
    private void cancelRainRender(ClientWorld clientWorld, Camera camera, int i, ParticlesMode particlesMode, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_RAIN_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }

}
