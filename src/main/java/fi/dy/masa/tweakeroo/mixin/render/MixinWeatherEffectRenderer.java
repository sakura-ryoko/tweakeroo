package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(WeatherEffectRenderer.class)
public class MixinWeatherEffectRenderer
{
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_cancelWeatherRender(Level level, int ticks, float partialTicks, Vec3 cameraPos, WeatherRenderState renderState, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_RAIN_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "tickRainParticles", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_cancelParticlesAndSounds(ClientLevel world, Camera camera, int ticks, ParticleStatus particlesMode, int weatherRadius, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_RAIN_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
