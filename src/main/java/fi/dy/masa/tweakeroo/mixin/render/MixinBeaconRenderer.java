package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.resources.Identifier;

@Mixin(BeaconRenderer.class)
public abstract class MixinBeaconRenderer
{
    @Inject(method = "submitBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/resources/Identifier;FFIIIFF)V",
            at = @At("HEAD"), cancellable = true)
    private static void tweakeroo_disableBeamRendering(PoseStack matrices, SubmitNodeCollector queue, Identifier textureId,
													   float beamHeight, float beamRotationDegrees, int minHeight, int maxHeight,
													   int color, float innerScale, float outerScale, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_BEACON_BEAM_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
