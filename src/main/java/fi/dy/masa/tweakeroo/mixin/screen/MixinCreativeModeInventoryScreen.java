package fi.dy.masa.tweakeroo.mixin.screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.util.CreativeExtraItems;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu>
{
    private MixinCreativeModeInventoryScreen(CreativeModeInventoryScreen.ItemPickerMenu screenHandler, Inventory playerInventory, Component text)
    {
        super(screenHandler, playerInventory, text);
    }

    // This needs to happen before the `this.handler.scrollItems(0.0F);` call.
    @Inject(method = "refreshSearchResults", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$ItemPickerMenu;scrollTo(F)V"))
    private void tweakeroo_removeInfestedStoneFromCreativeSearchInventory(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CREATIVE_INFESTED_BLOCKS.getBooleanValue())
        {
            CreativeExtraItems.removeInfestedBlocks(this.menu.items);
        }
    }
}
