package fi.dy.masa.tweakeroo.mixin.screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.gui.screens.Screen;

@Mixin(value = Screen.class)
public abstract class MixinScreen
{
	@Shadow public abstract boolean isPauseScreen();

	@Redirect(method = "isAllowedInPortal",
			  at = @At(value = "INVOKE",
					 target = "Lnet/minecraft/client/gui/screens/Screen;isPauseScreen()Z"))
	private boolean tweakeroo_keepGuiOpenThroughPortals(Screen instance)
	{
		// Spoof the return value to prevent entering the if block
		if (Configs.Disable.DISABLE_PORTAL_GUI_CLOSING.getBooleanValue())
		{
			return true;
		}

		return this.isPauseScreen();
	}
}
