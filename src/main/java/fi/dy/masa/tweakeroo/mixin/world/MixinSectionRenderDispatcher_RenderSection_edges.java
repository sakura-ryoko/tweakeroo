package fi.dy.masa.tweakeroo.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public abstract class MixinSectionRenderDispatcher_RenderSection_edges
{
    @Inject(method = "doesChunkExistAt", at = @At("HEAD"), cancellable = true)
    private void allowEdgeChunksToRender(long l, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_RENDER_EDGE_CHUNKS.getBooleanValue())
        {
            cir.setReturnValue(true);
        }
    }
}
