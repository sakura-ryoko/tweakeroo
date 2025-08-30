package fi.dy.masa.tweakeroo.mixin.screen;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = Screen.class)
public abstract class MixinScreen
{
	@Shadow public abstract boolean shouldPause();

	@Redirect(method = "keepOpenThroughPortal",
			  at = @At(value = "INVOKE",
					 target = "Lnet/minecraft/client/gui/screen/Screen;shouldPause()Z"))
	private boolean tweakeroo_keepGuiOpenThroughPortals(Screen instance)
	{
		// Spoof the return value to prevent entering the if block
		if (Configs.Disable.DISABLE_PORTAL_GUI_CLOSING.getBooleanValue())
		{
			return true;
		}

		return this.shouldPause();
	}
}
