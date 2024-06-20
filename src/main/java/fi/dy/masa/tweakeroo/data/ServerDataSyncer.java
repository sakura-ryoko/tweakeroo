package fi.dy.masa.tweakeroo.data;

import com.mojang.datafixers.util.Either;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.mixin.IMixinDataQueryHandler;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"deprecation"})
public class ServerDataSyncer {
    private static ServerDataSyncer INSTANCE;

    public static ServerDataSyncer getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new ServerDataSyncer(MinecraftClient.getInstance().world);
        }
        return INSTANCE;
    }

    public static void resetInstance()
    {
        INSTANCE = null;
    }

    /**
     * key: BlockPos
     * value: data, timestamp
     */
    private final Map<BlockPos, Pair<BlockEntity, Long>> blockCache = new HashMap<>();
    private final Map<Integer, Pair<Entity, Long>> entityCache = new HashMap<>();
    private final Map<Either<BlockPos, Integer>, CompletableFuture<@Nullable NbtCompound>> pendingQueries = new HashMap<>();
    private final Map<Integer, CompletableFuture<@Nullable NbtCompound>> pendingQueryFutures = new HashMap<>();
    private final ClientWorld clientWorld;
    /**
     * if the client can query the server for block/entity data? null if not yet known.
     */
    private Optional<Boolean> yesIAmOp = Optional.empty();

    public ServerDataSyncer(ClientWorld world)
    {
        this.clientWorld = Objects.requireNonNull(world);
    }

    private @Nullable BlockEntity getCache(BlockPos pos)
    {
        var data = blockCache.get(pos);

        if (yesIAmOp.isPresent() && !yesIAmOp.get()) return null;

        if (data != null && System.currentTimeMillis() - data.getRight() <= 1000)
        {
            if (System.currentTimeMillis() - data.getRight() > 500)
            {
                syncBlockEntity(clientWorld, pos);
            }
            return data.getLeft();
        }

        return null;
    }

    private @Nullable Entity getCache(int networkId)
    {
        var data = entityCache.get(networkId);

        if (yesIAmOp.isPresent() && !yesIAmOp.get()) return null;

        if (data != null && System.currentTimeMillis() - data.getRight() <= 1000)
        {
            if (System.currentTimeMillis() - data.getRight() > 500)
            {
                syncEntity(networkId);
            }
            return data.getLeft();
        }

        return null;
    }

    public void handleQueryResponse(int transactionId, NbtCompound nbt)
    {
        Tweakeroo.logger.debug("handleQueryResponse: id [{}] // nbt {}", transactionId, nbt);
        pendingQueryFutures.remove(transactionId).complete(nbt);

        if (nbt == null) return;
        if (pendingQueryFutures.containsKey(transactionId))
        {
            yesIAmOp = Optional.of(true);
        }

        if (blockCache.size() > 30)
        {
            blockCache.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().getRight() > 1000);
        }
        if (entityCache.size() > 30)
        {
            entityCache.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().getRight() > 1000);
        }
    }

    public Inventory getBlockInventory(World world, BlockPos pos)
    {
        if (yesIAmOp.isPresent() && !yesIAmOp.get()) return null;
        Tweakeroo.logger.debug("getBlockInventory: pos [{}], op status: {}", pos.toShortString(), yesIAmOp);

        if (!world.isChunkLoaded(pos)) return null;
        if (getCache(pos) instanceof Inventory inv)
        {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof ChestBlock && inv instanceof ChestBlockEntity)
            {
                ChestType type = state.get(ChestBlock.CHEST_TYPE);

                if (type != ChestType.SINGLE)
                {
                    BlockPos posAdj = pos.offset(ChestBlock.getFacing(state));
                    if (!world.isChunkLoaded(posAdj)) return null;
                    BlockState stateAdj = world.getBlockState(posAdj);

                    var dataAdj = getCache(posAdj);
                    if (dataAdj == null)
                    {
                        syncBlockEntity(world, posAdj);
                    }

                    if (stateAdj.getBlock() == state.getBlock() && dataAdj instanceof ChestBlockEntity inv2 && stateAdj.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE && stateAdj.get(ChestBlock.FACING) == state.get(ChestBlock.FACING))
                    {
                        Inventory invRight = type == ChestType.RIGHT ? inv : inv2;
                        Inventory invLeft = type == ChestType.RIGHT ? inv2 : inv;
                        inv = new DoubleInventory(invRight, invLeft);
                    }
                }
            }
            return inv;
        }

        syncBlockEntity(world, pos);

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock)
        {
            ChestType type = state.get(ChestBlock.CHEST_TYPE);

            if (type != ChestType.SINGLE)
            {
                BlockPos posAdj = pos.offset(ChestBlock.getFacing(state));
                if (world.isChunkLoaded(posAdj))
                {
                    BlockState stateAdj = world.getBlockState(posAdj);
                    if (stateAdj.getBlock() instanceof ChestBlock)
                    {
                        syncBlockEntity(world, posAdj);
                    }
                }
            }
        }
        return null;
    }

    public CompletableFuture<NbtCompound> syncBlockEntity(World world, BlockPos pos)
    {
        Tweakeroo.logger.debug("syncBlockEntity: pos [{}], op status: {}", pos.toShortString(), yesIAmOp);
        if (yesIAmOp.isPresent() && !yesIAmOp.get()) return CompletableFuture.completedFuture(null);

        if (MinecraftClient.getInstance().isIntegratedServerRunning())
        {
            BlockEntity blockEntity = MinecraftClient.getInstance().getServer().getWorld(world.getRegistryKey()).getWorldChunk(pos).getBlockEntity(pos, WorldChunk.CreationType.CHECK);
            if (blockEntity != null)
            {
                blockCache.put(pos, new Pair<>(blockEntity, System.currentTimeMillis()));
                return CompletableFuture.completedFuture(blockEntity.createNbt(world.getRegistryManager()));
            }
        }
        Either<BlockPos, Integer> posEither = Either.left(pos);
        if (MinecraftClient.getInstance().getNetworkHandler() != null)
        {
            if (pendingQueries.containsKey(posEither))
            {
                return pendingQueries.get(posEither);
            }

            CompletableFuture<NbtCompound> future = new CompletableFuture<>();
            DataQueryHandler handler = MinecraftClient.getInstance().getNetworkHandler().getDataQueryHandler();
            handler.queryBlockNbt(pos, it -> {
            });
            pendingQueries.put(posEither, future);
            pendingQueryFutures.put(((IMixinDataQueryHandler) handler).currentTransactionId(), future);
            future.thenAccept(nbt -> {
                if (!clientWorld.isChunkLoaded(pos) || nbt == null) return;
                BlockState state = clientWorld.getBlockState(pos);

                if (state.getBlock() instanceof BlockEntityProvider provider)
                {
                    var be = provider.createBlockEntity(pos, state);
                    if (be != null)
                    {
                        be.read(nbt, clientWorld.getRegistryManager());
                        blockCache.put(pos, new Pair<>(be, System.currentTimeMillis()));
                    }
                }
            });
            if (yesIAmOp.isEmpty())
            {
                yesIAmOp = Optional.of(false);
            }
            return future;
        }
        else
        {
            throw new IllegalStateException("Not connected to a server");
        }
    }

    public CompletableFuture<NbtCompound> syncEntity(int networkId)
    {
        Tweakeroo.logger.debug("syncEntity: pos [{}], op status: {}", networkId, yesIAmOp);
        if (yesIAmOp.isPresent() && !yesIAmOp.get()) return CompletableFuture.completedFuture(null);

        Either<BlockPos, Integer> idEither = Either.right(networkId);
        if (MinecraftClient.getInstance().getNetworkHandler() != null)
        {
            if (pendingQueries.containsKey(idEither))
            {
                return pendingQueries.get(idEither);
            }

            CompletableFuture<NbtCompound> future = new CompletableFuture<>();
            DataQueryHandler handler = MinecraftClient.getInstance().getNetworkHandler().getDataQueryHandler();
            handler.queryEntityNbt(networkId, it -> {
            });
            pendingQueries.put(idEither, future);
            pendingQueryFutures.put(((IMixinDataQueryHandler) handler).currentTransactionId(), future);
            future.thenAccept(nbt -> {
                if (nbt == null) return;
                if (clientWorld.getEntityById(networkId) != null)
                {
                    Entity entity = clientWorld.getEntityById(networkId).getType().create(clientWorld);
                    if (entity != null)
                    {
                        entity.readNbt(nbt);
                        entityCache.put(networkId, new Pair<>(entity, System.currentTimeMillis()));
                    }
                }
            });
            if (yesIAmOp.isEmpty())
            {
                yesIAmOp = Optional.of(false);
            }
            return future;
        }
        else
        {
            throw new IllegalStateException("Not connected to a server");
        }
    }

    public @Nullable Entity getServerEntity(Entity entity)
    {
        Entity serverEntity = getCache(entity.getId());
        if (serverEntity == null)
        {
            syncEntity(entity.getId());
            return null;
        }
        return serverEntity;
    }

    public void recheckOpStatus()
    {
        yesIAmOp = Optional.empty();
    }
}
