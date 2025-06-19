package fi.dy.masa.tweakeroo.mixin.world;

import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkLightProvider.class)
public interface IMixinChunkLightProvider
{
    @Accessor("chunkProvider")
    ChunkProvider tweakeroo_getChunkProvider();
}
