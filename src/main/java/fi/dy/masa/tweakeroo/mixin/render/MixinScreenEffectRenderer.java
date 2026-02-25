package fi.dy.masa.tweakeroo.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(ScreenEffectRenderer.class)
public abstract class MixinScreenEffectRenderer
{
    @WrapOperation(method = "renderScreenEffect",
                   at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    private boolean tweakeroo_disableFireOverlay(LocalPlayer instance, Operation<Boolean> original)
    {
        if (Configs.Disable.DISABLE_FIRE_OVERLAY.getBooleanValue())
        {
            return false;
        }

        return original.call(instance);
//        return player.isOnFire();
    }
}
