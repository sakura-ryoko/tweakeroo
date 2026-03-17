package fi.dy.masa.tweakeroo.mixin.item;

import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = ItemInstance.class, priority = 1005)
public interface IMixinItemInstance extends TypedInstance<Item>, DataComponentGetter
{
	@Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_getMaxStackSizeStackSensitive(CallbackInfoReturnable<Integer> cir)
	{
		if (FeatureToggle.TWEAK_SHULKERBOX_STACKING.getBooleanValue())
		{
			try
			{
				ItemStack stack = (ItemStack) (Object) this;

				if (stack.getItem() instanceof BlockItem block &&
					block.getBlock() instanceof ShulkerBoxBlock &&
					InventoryUtils.shulkerBoxHasItems(stack) == false)
				{
					if (this.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) < Configs.Internal.SHULKER_MAX_STACK_SIZE.getIntegerValue())
					{
						cir.setReturnValue(Configs.Internal.SHULKER_MAX_STACK_SIZE.getIntegerValue());
					}
				}
			}
			catch (Throwable ignored) {}
		}
	}
}
