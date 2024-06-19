package fi.dy.masa.tweakeroo.data;

import com.mojang.datafixers.util.Either;
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

@SuppressWarnings("deprecation")
public class ServerDataSyncer {
    public static ServerDataSyncer INSTANCE;

    /**
     * key: BlockPos
     * value: data, timestamp
     */
    private final Map<BlockPos, Pair<BlockEntity, Long>> blockCache = new HashMap<>();
    private final Map<Integer, Pair<Entity, Long>> entityCache = new HashMap<>();
    private final Map<Integer, Either<BlockPos, Integer>> pendingQueries = new HashMap<>();
    private final ClientWorld clientWorld;

    public ServerDataSyncer(ClientWorld world) {
        this.clientWorld = Objects.requireNonNull(world);
    }

    private @Nullable BlockEntity getCache(BlockPos pos) {
        var data = blockCache.get(pos);
        if (data != null && System.currentTimeMillis() - data.getRight() <= 1000) {
            if (System.currentTimeMillis() - data.getRight() > 500) {
                syncBlockEntity(clientWorld, pos);
            }
            return data.getLeft();
        }

        return null;
    }

    private @Nullable Entity getCache(int networkId) {
        var data = entityCache.get(networkId);
        if (data != null && System.currentTimeMillis() - data.getRight() <= 1000) {
            if (System.currentTimeMillis() - data.getRight() > 500) {
                syncEntity(networkId);
            }
            return data.getLeft();
        }

        return null;
    }

    public void handleQueryResponse(int transactionId, NbtCompound nbt) {
        if (nbt == null) return;
        if (pendingQueries.containsKey(transactionId)) {
            Either<BlockPos, Integer> either = pendingQueries.remove(transactionId);
            either.ifLeft(pos -> {
                if (!clientWorld.isChunkLoaded(pos)) return;
                BlockState state = clientWorld.getBlockState(pos);
                if (state.getBlock() instanceof BlockEntityProvider provider) {
                    var be = provider.createBlockEntity(pos, state);
                    if (be != null) {
                        be.read(nbt, clientWorld.getRegistryManager());
                        blockCache.put(pos, new Pair<>(be, System.currentTimeMillis()));
                    }
                }
            }).ifRight(id -> {
                Entity entity = clientWorld.getEntityById(id).getType().create(clientWorld);
                if (entity != null) {
                    entity.readNbt(nbt);
                    entityCache.put(id, new Pair<>(entity, System.currentTimeMillis()));
                }
            });
        }
        if (blockCache.size() > 30) {
            blockCache.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().getRight() > 1000);
        }
        if (entityCache.size() > 30) {
            entityCache.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().getRight() > 1000);
        }
    }

    public Inventory getBlockInventory(World world, BlockPos pos) {
        if (!world.isChunkLoaded(pos)) return null;
        var data = getCache(pos);
        if (data instanceof Inventory inv) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof ChestBlock && data instanceof ChestBlockEntity) {
                ChestType type = state.get(ChestBlock.CHEST_TYPE);

                if (type != ChestType.SINGLE) {
                    BlockPos posAdj = pos.offset(ChestBlock.getFacing(state));
                    if (!world.isChunkLoaded(posAdj)) return null;
                    BlockState stateAdj = world.getBlockState(posAdj);

                    var dataAdj = getCache(posAdj);
                    if (dataAdj == null) {
                        syncBlockEntity(world, posAdj);
                    }

                    if (stateAdj.getBlock() == state.getBlock() &&
                            dataAdj instanceof ChestBlockEntity inv2 &&
                            stateAdj.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE &&
                            stateAdj.get(ChestBlock.FACING) == state.get(ChestBlock.FACING)) {
                        Inventory invRight = type == ChestType.RIGHT ? inv : inv2;
                        Inventory invLeft = type == ChestType.RIGHT ? inv2 : inv;
                        inv = new DoubleInventory(invRight, invLeft);
                    }
                }
            }
            return inv;
        }

        syncBlockEntity(world, pos);
        return null;
    }

    public void syncBlockEntity(World world, BlockPos pos) {
        if (MinecraftClient.getInstance().isIntegratedServerRunning()) {
            BlockEntity blockEntity = MinecraftClient.getInstance().getServer().getWorld(world.getRegistryKey()).getWorldChunk(pos).getBlockEntity(pos, WorldChunk.CreationType.CHECK);
            if (blockEntity != null) {
                blockCache.put(pos, new Pair<>(blockEntity, System.currentTimeMillis()));
                return;
            }
        }
        Either<BlockPos, Integer> posEither = Either.left(pos);
        if (MinecraftClient.getInstance().getNetworkHandler() != null && !pendingQueries.containsValue(posEither)) {
            DataQueryHandler handler = MinecraftClient.getInstance().getNetworkHandler().getDataQueryHandler();
            handler.queryBlockNbt(pos, it -> {});
            pendingQueries.put(((IMixinDataQueryHandler) handler).currentTransactionId(), posEither);
        }
    }

    public void syncEntity(int networkId) {
        Either<BlockPos, Integer> idEither = Either.right(networkId);
        if (MinecraftClient.getInstance().getNetworkHandler() != null && !pendingQueries.containsValue(idEither)) {
            DataQueryHandler handler = MinecraftClient.getInstance().getNetworkHandler().getDataQueryHandler();
            handler.queryEntityNbt(networkId, it -> {});
            pendingQueries.put(((IMixinDataQueryHandler) handler).currentTransactionId(), idEither);
        }
    }

    public @Nullable Entity getServerEntity(Entity entity) {
        Entity serverEntity = getCache(entity.getId());
        if (serverEntity == null) {
            syncEntity(entity.getId());
            return null;
        }
        return serverEntity;
    }
}
