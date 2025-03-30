package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(BeaconBlockEntityRenderer.class)
public abstract class MixinBeaconBlockEntityRenderer
{
    @Inject(method = "renderBeam(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/Identifier;FFJIIIFF)V",
            at = @At("HEAD"), cancellable = true)
    private static void tweakeroo_disableBeamRendering(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId,
                                                       float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, int color,
                                                       float innerRadius, float outerRadius, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_BEACON_BEAM_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
