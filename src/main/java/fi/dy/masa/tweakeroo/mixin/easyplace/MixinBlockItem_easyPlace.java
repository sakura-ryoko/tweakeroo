package fi.dy.masa.tweakeroo.mixin.easyplace;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.tweaks.PlacementHandler;
import fi.dy.masa.tweakeroo.tweaks.PlacementHandler.UseContext;

@Mixin(value = BlockItem.class, priority = 990)
//@Restriction(conflict = @Condition(value = "litematica"))
public abstract class MixinBlockItem_easyPlace extends Item
{
    private MixinBlockItem_easyPlace(Item.Properties builder)
    {
        super(builder);
    }

    @Shadow protected abstract boolean canPlace(BlockPlaceContext context, BlockState state);
    @Shadow public abstract Block getBlock();

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_modifyPlacementState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir)
    {
        if (Configs.Generic.CLIENT_PLACEMENT_ROTATION.getBooleanValue())
        {
            BlockState stateOrig = this.getBlock().getStateForPlacement(ctx);

			if (stateOrig != null)
			{
				if (!Configs.Generic.CLIENT_PLACEMENT_VALIDATION.getBooleanValue() || this.canPlace(ctx, stateOrig))
				{
					UseContext context = UseContext.from(ctx, ctx.getHand());
					cir.setReturnValue(PlacementHandler.applyPlacementProtocolToPlacementState(stateOrig, context));
				}
			}
        }
    }
}
