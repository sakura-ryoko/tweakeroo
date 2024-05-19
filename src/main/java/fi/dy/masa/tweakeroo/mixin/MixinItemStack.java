package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import fi.dy.masa.tweakeroo.util.IItemStackLimit;
import fi.dy.masa.tweakeroo.util.InventoryUtils;

@Mixin(ItemStack.class)
public abstract class MixinItemStack
{
    @Shadow
    public abstract Item getItem();

    @Shadow public abstract ComponentMap getComponents();

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    public void getMaxStackSizeStackSensitive(CallbackInfoReturnable<Integer> ci)
    {
        Integer new_max_size = ((IItemStackLimit) this.getItem()).getMaxStackSize((ItemStack) (Object) this);
        Integer orig_max_size = this.getComponents().getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);

        if (orig_max_size < new_max_size)
        {
            // Sets the Data Component manually
            InventoryUtils.updateMaxStackSize((ItemStack) (Object) this, new_max_size);
        }
        ci.setReturnValue(new_max_size >= orig_max_size ? new_max_size : orig_max_size);
    }
}
