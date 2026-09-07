package fi.dy.masa.tweakeroo.mixin.freecam;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.util.CameraUtils;

@Mixin(value = LivingEntity.class)
public abstract class MixinLivingEntity_freeCam extends Entity
{
	public MixinLivingEntity_freeCam(EntityType<?> type, Level level)
	{
		super(type, level);
	}


	@Inject(method = "swing", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_preventHandSwing(InteractionHand hand, SwingAnimation animation, boolean sendToSwingingEntity,
	                                        CallbackInfoReturnable<Boolean> cir)
	{
		if (((LivingEntity) (Object) this) instanceof LocalPlayer &&
			CameraUtils.shouldPreventPlayerInputs())
		{
			cir.setReturnValue(false);
		}
	}
}
