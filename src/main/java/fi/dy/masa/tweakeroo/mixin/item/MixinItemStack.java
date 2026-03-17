package fi.dy.masa.tweakeroo.mixin.item;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(ItemStack.class)
public abstract class MixinItemStack
{
    @Shadow public abstract Item getItem();

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private <T> void tweakeroo_removeVanillaTooltip(DataComponentType<T> componentType, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type, CallbackInfo ci)
    {
        if (this.getItem() instanceof BlockItem block &&
            block.getBlock() instanceof ShulkerBoxBlock &&
            componentType == DataComponents.CONTAINER &&
            Configs.Disable.DISABLE_SHULKER_BOX_TOOLTIP.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
