package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(BeaconBlockEntityRenderer.class)
public abstract class MixinBeaconBlockEntityRenderer
{
    @Inject(method = "renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/util/Identifier;FFJIIIFF)V",
            at = @At("HEAD"), cancellable = true)
    private static void tweakeroo_disableBeamRendering(MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue,
													   Identifier textureId, float tickProgress, float heightScale,
													   long worldTime, int yOffset, int maxY, int color,
													   float innerRadius, float outerRadius, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_BEACON_BEAM_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
