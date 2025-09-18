package fi.dy.masa.tweakeroo.mixin.render;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderManager.class)
public abstract class MixinBlockEntityRenderManager
{
    @Inject(method = "getRenderState(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)Lnet/minecraft/client/render/block/entity/state/BlockEntityRenderState;",
			at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity, S extends BlockEntityRenderState> void tweakeroo_preventTileEntityRendering(
            E blockEntity, float tickProgress, ModelCommandRenderer.@Nullable CrumblingOverlayCommand crumblingOverlay,
            CallbackInfoReturnable<S> cir)
    {
        if (Configs.Disable.DISABLE_TILE_ENTITY_RENDERING.getBooleanValue())
        {
            cir.setReturnValue(null);
        }
    }
}
