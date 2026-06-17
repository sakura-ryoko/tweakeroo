package fi.dy.masa.tweakeroo.mixin.freecam;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = Minecraft.class, priority = 850)
public class MixinMinecraft_freeCam
{
	@Shadow @Nullable public LocalPlayer player;

	@WrapOperation(method = "pick",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
	private Entity tweakeroo_overrideCameraEntityForRayTrace(Minecraft instance, Operation<Entity> original)
	{
		// Return the real player for the hit target ray tracing if the
		// player inputs option is enabled in Free Camera mode.
		// Normally in Free Camera mode the Tweakeroo CameraEntity is set as the
		// render view/camera entity, which would then also ray trace from the camera point of view.
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
			Configs.Generic.FREE_CAMERA_PLAYER_INPUTS.getBooleanValue()  &&
			!FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue() &&
			this.player != null)
		{
			return this.player;
		}

		return original.call(instance);
	}
}
