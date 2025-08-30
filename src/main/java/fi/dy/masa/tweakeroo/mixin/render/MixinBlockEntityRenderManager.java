package fi.dy.masa.tweakeroo.mixin.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(BlockEntityRenderManager.class)
public abstract class MixinBlockEntityRenderManager
{
    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V",
			at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void tweakeroo_preventTileEntityRendering(
			E blockEntity, float tickProgress, MatrixStack matrices,
			@Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand,
			OrderedRenderCommandQueue queue, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TILE_ENTITY_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }
//
//    @Inject(method = "render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/math/Vec3d;)V",
//            at = @At("HEAD"),
//			cancellable = true)
//    private static <T extends BlockEntity> void preventTileEntityRendering(
//            BlockEntityRenderer<T> renderer, T blockEntity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d vec3d, CallbackInfo ci)
//    {
//        if (Configs.Disable.DISABLE_TILE_ENTITY_RENDERING.getBooleanValue())
//        {
//            ci.cancel();
//        }
//    }
}
