package fi.dy.masa.tweakeroo.mixin.world;

import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightEngine.class)
public interface IMixinChunkLightProvider
{
    @Accessor("chunkSource")
    LightChunkGetter tweakeroo_getChunkProvider();
}
