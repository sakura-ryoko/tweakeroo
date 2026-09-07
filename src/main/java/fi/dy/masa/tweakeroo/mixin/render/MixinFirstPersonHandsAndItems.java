package fi.dy.masa.tweakeroo.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(FirstPersonHandsAndItems.class)
public class MixinFirstPersonHandsAndItems
{
	@WrapOperation(method = "tick",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F"
	               )
	)
	public float tweakeroo_redirectedGetCooledAttackStrength(LocalPlayer instance, float v, Operation<Float> original)
	{
		return Configs.Disable.DISABLE_ITEM_SWITCH_COOLDOWN.getBooleanValue() ? 1.0F : original.call(instance, v);
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void tweakeroo_onExtractPlayerHands(LocalPlayer player, float partialTicks, FirstPersonHandsAndItemsRenderState state, CallbackInfo ci)
	{
		if (Configs.Disable.DISABLE_OFFHAND_RENDERING.getBooleanValue())
		{
			state.offHandRenderState.clear();
			state.hasOffHandMapData = false;
			state.offHandItem = ItemStack.EMPTY;
		}
	}
}
