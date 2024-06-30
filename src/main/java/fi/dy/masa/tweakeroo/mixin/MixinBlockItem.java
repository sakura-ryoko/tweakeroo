package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.PlacementHandler;
import fi.dy.masa.tweakeroo.tweaks.PlacementHandler.UseContext;
import fi.dy.masa.tweakeroo.util.IItemStackLimit;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem extends Item implements IItemStackLimit
{
    private MixinBlockItem(Item.Settings builder)
    {
        super(builder);
    }

    @Shadow protected abstract boolean canPlace(ItemPlacementContext context, BlockState state);
    @Shadow public abstract Block getBlock();

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void modifyPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir)
    {
        if (Configs.Generic.CLIENT_PLACEMENT_ROTATION.getBooleanValue())
        {
            BlockState stateOrig = this.getBlock().getPlacementState(ctx);

            if (stateOrig != null && this.canPlace(ctx, stateOrig))
            {
                UseContext context = UseContext.from(ctx, ctx.getHand());
                cir.setReturnValue(PlacementHandler.applyPlacementProtocolToPlacementState(stateOrig, context));
            }
        }
    }

    @Override
    public int getMaxStackSize(ItemStack stack)
    {
        if (FeatureToggle.TWEAK_SHULKERBOX_STACKING.getBooleanValue() &&
            ((BlockItem) (Object) this).getBlock() instanceof ShulkerBoxBlock &&
            InventoryUtils.shulkerBoxHasItems(stack) == false)
        {
            return 64;
        }

        // FIXME How to call the stack-sensitive version on the super class?
        return super.getMaxCount();
    }
}
