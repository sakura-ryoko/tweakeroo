package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(ScreenEffectRenderer.class)
public abstract class MixinScreenEffectRenderer
{
    @Inject(method = "submit", at = @At(value = "HEAD"))
    private void tweakeroo_disableFireOverlay(float partialTicks, SubmitNodeCollector submitNodeCollector, PlayerRenderState playerRenderState, CameraRenderState cameraRenderState, boolean hideGui, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_FIRE_OVERLAY.getBooleanValue())
        {
            playerRenderState.isOnFire = false;
        }
    }
}
