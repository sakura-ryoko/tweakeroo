package fi.dy.masa.tweakeroo.mixin.hud;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.gui.spectator.PlayerMenuItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerMenuItem.class)
public class MixinPlayerMenuItem {
    @ModifyReturnValue(method = "isEnabled", at = @At("RETURN"))
    private boolean allowSpectatorTeleport(boolean original) {
        return original || fi.dy.masa.tweakeroo.config.FeatureToggle.TWEAK_SPECTATOR_TELEPORT.getBooleanValue();
    }
}
