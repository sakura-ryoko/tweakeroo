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

    @Inject(method = "setBlocksDirty", at = @At("HEAD"), cancellable = true)
    private void disableChunkReRenders(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CHUNK_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"), cancellable = true)
    private void disableChunkReRenders(int chunkX, int chunkY, int chunkZ, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CHUNK_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "sendBlockUpdated", at = @At("HEAD"), cancellable = true)
    private void disableChunkReRenders(BlockPos pos, BlockState old, BlockState current, int updateFlags, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CHUNK_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

	@Inject(method = "addDestroyBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
			at = @At("HEAD"), cancellable = true)
	private void tweakeroo_onAddBlockBreakParticles(BlockPos pos, BlockState blockState, CallbackInfo ci)
	{
		if (Configs.Disable.DISABLE_BLOCK_BREAK_PARTICLES.getBooleanValue())
		{
			ci.cancel();
		}
	}
}
