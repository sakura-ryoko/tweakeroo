package fi.dy.masa.tweakeroo.mixin.world;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends Level
{
    private MixinClientLevel(WritableLevelData properties,
                             ResourceKey<Level> registryRef,
                             RegistryAccess registryManager,
                             Holder<DimensionType> dimension,
                             boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates)
    {
        super(properties, registryRef, registryManager, dimension, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tickNonPassenger(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void disableClientEntityTicking(Entity entity, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CLIENT_ENTITY_UPDATES.getBooleanValue() &&
            (entity instanceof Player) == false)
        {
            ci.cancel();
        }
    }

    /* TODO 1.17 is this still needed?
    @Inject(method = "addEntitiesToChunk", at = @At("HEAD"), cancellable = true)
    private void fixChunkEntityLeak(WorldChunk chunk, CallbackInfo ci)
    {
        if (Configs.Fixes.CLIENT_CHUNK_ENTITY_DUPE.getBooleanValue())
        {
            for (int y = 0; y < 16; ++y)
            {
                // The chunk already has entities, which means it's a re-used existing chunk,
                // in such a case we don't want to add the from the world entities again, otherwise
                // they are basically duped within the Chunk.
                if (chunk.getEntitySectionArray()[y].size() > 0)
                {
                    ci.cancel();
                    return;
                }
            }
        }
    }
    */

    @Inject(method = "setBlocksDirty", at = @At("HEAD"), cancellable = true)
    private void disableChunkReRenders(BlockPos pos, BlockState old, BlockState updated, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CHUNK_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"), cancellable = true)
    private void disableChunkReRenders(int x, int y, int z, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CHUNK_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"), cancellable = true)
    private void disableChunkReRenders(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CHUNK_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

	@Inject(method = "addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
			at = @At("HEAD"), cancellable = true)
	private void tweakeroo_onAddBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci)
	{
		if (Configs.Disable.DISABLE_BLOCK_BREAK_PARTICLES.getBooleanValue())
		{
			ci.cancel();
		}
	}
}
