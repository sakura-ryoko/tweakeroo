package fi.dy.masa.tweakeroo.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = Screen.class)
public abstract class MixinScreen
{
	@Shadow public abstract boolean isPauseScreen();

	@WrapOperation(method = "isAllowedInPortal",
	               at = @At(value = "INVOKE",
					 target = "Lnet/minecraft/client/gui/screens/Screen;isPauseScreen()Z"))
	private boolean tweakeroo_keepGuiOpenThroughPortals(Screen instance, Operation<Boolean> original)
	{
		// Spoof the return value to prevent entering the if block
		if (Configs.Disable.DISABLE_PORTAL_GUI_CLOSING.getBooleanValue())
		{
			return true;
		}

		return this.isPauseScreen();
	}
}
