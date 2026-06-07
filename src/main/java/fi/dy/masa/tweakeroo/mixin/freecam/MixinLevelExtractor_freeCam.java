package fi.dy.masa.tweakeroo.mixin.freecam;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = LevelExtractor.class, priority = 1005)
public abstract class MixinLevelExtractor_freeCam
{
	// Allow rendering the client player entity by spoofing one of the entity rendering conditions while in Free Camera mode
	@WrapOperation(method = "extractVisibleEntities",
	               require = 0,
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;",
	                        ordinal = 3
	               ))
	private Entity tweakeroo_allowRenderingClientPlayerInFreeCameraMode(Camera instance, Operation<Entity> original)
	{
		if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
		{
			return Minecraft.getInstance().player;
		}

		return original.call(instance);
	}
}
