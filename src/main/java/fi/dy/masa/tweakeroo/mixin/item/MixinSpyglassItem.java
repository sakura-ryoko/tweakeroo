package fi.dy.masa.tweakeroo.mixin.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(SpyglassItem.class)
public class MixinSpyglassItem
{
	@Inject(method = "use", at = @At("RETURN"))
	private void tweakeroo_onSpyglassActivate(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir)
	{
		if (cir.getReturnValue() == InteractionResult.CONSUME)
		{
			// Don't check if the Tweak is active; just mark as in use
			// in case someone decides to toggle it while already using.
			MiscUtils.onSpyglassZoomActivated();
		}
	}

	@Inject(method = "stopUsing", at = @At("TAIL"))
	private void tweakeroo_onSpyglassDeactivate(LivingEntity livingEntity, CallbackInfo ci)
	{
		// Don't check if the Tweak is active; just mark as not in use
		// in case someone decides to toggle it while already using.
		MiscUtils.onSpyglassZoomDeactivated();
	}
}
