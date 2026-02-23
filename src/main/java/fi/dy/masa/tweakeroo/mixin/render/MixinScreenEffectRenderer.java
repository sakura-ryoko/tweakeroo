package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(ScreenEffectRenderer.class)
public abstract class MixinScreenEffectRenderer
{
    @Redirect(method = "renderScreenEffect",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    private boolean tweakeroo_disableFireOverlay(LocalPlayer player)
    {
        if (Configs.Disable.DISABLE_FIRE_OVERLAY.getBooleanValue())
        {
            return false;
        }

        return player.isOnFire();
    }
}
