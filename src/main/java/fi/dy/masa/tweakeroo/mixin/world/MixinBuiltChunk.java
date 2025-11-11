package fi.dy.masa.tweakeroo.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.render.chunk.ChunkBuilder;

@Mixin(ChunkBuilder.BuiltChunk.class)
public abstract class MixinBuiltChunk
{
    @Inject(method = "scheduleRebuild(Z)V", at = @At("HEAD"), cancellable = true)
    private void disableChunkReRenders(boolean important, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CHUNK_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
