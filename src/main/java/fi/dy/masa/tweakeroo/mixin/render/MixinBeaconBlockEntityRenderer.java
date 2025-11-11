package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Mixin(BeaconBlockEntityRenderer.class)
public abstract class MixinBeaconBlockEntityRenderer
{
    @Inject(method = "renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/util/Identifier;FFIIIFF)V",
            at = @At("HEAD"), cancellable = true)
    private static void tweakeroo_disableBeamRendering(MatrixStack matrices, OrderedRenderCommandQueue queue, Identifier textureId,
													   float beamHeight, float beamRotationDegrees, int minHeight, int maxHeight,
													   int color, float innerScale, float outerScale, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_BEACON_BEAM_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
