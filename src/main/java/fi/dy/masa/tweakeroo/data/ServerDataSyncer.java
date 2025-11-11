package fi.dy.masa.tweakeroo.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.interfaces.IDataSyncer;
import fi.dy.masa.malilib.mixin.entity.IMixinAbstractHorseEntity;
import fi.dy.masa.malilib.mixin.entity.IMixinPiglinEntity;
import fi.dy.masa.malilib.mixin.network.IMixinDataQueryHandler;
import fi.dy.masa.malilib.network.ClientPlayHandler;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.malilib.util.nbt.NbtEntityUtils;
import fi.dy.masa.malilib.util.nbt.NbtKeys;
import fi.dy.masa.malilib.util.nbt.NbtView;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.network.ServuxTweaksHandler;
import fi.dy.masa.tweakeroo.network.ServuxTweaksPacket;

@SuppressWarnings({"deprecation"})
public class ServerDataSyncer implements IClientTickHandler, IDataSyncer
{
    private static final ServerDataSyncer INSTANCE = new ServerDataSyncer();
    public static ServerDataSyncer getInstance()
    {
        return INSTANCE;
    }
    
    private final static ServuxTweaksHandler<ServuxTweaksPacket.Payload> HANDLER = ServuxTweaksHandler.getInstance();
    private final MinecraftClient mc;
    //private int uptimeTicks = 0;
    private boolean servuxServer = false;
    private boolean hasInValidServux = false;
    private String servuxVersion;
    private boolean checkOpStatus = true;
    private boolean hasOpStatus = false;
    private long lastOpCheck = 0L;

    // Data Cache
    private final ConcurrentHashMap<BlockPos, Pair<Long, Pair<BlockEntity, NbtCompound>>> blockEntityCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer,  Pair<Long, Pair<Entity,      NbtCompound>>> entityCache      = new ConcurrentHashMap<>();
    private long serverTickTime = 0;
    // Requests to be executed
    private final Set<BlockPos> pendingBlockEntitiesQueue = new LinkedHashSet<>();
    private final Set<Integer> pendingEntitiesQueue = new LinkedHashSet<>();
    // To save vanilla query packet transaction
    private final Map<Integer, Either<BlockPos, Integer>> transactionToBlockPosOrEntityId = new HashMap<>();
    private ClientWorld clientWorld;

    @Override
    @Nullable
    public World getWorld()
    {
        return WorldUtils.getBestWorld(mc);
    }

    @Override
    public ClientWorld getClientWorld()
    {
        if (this.clientWorld == null)
        {
            this.clientWorld = this.mc.world;
        }

        return this.clientWorld;
    }

    public ServerDataSyncer()
    {
        this.mc = MinecraftClient.getInstance();
    }

    @Override
    public void onClientTick(MinecraftClient mc)
    {
        long now = System.currentTimeMillis();

        //this.uptimeTicks++;
        if (now - this.serverTickTime > 50)
        {
            // In this block, we do something every server tick
            if (!FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
            {
                this.serverTickTime = now;
                if (!DataManager.getInstance().hasIntegratedServer() && this.hasServuxServer())
                {
                    this.servuxServer = false;
                    HANDLER.unregisterPlayReceiver();
                }

                if (!FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue())
                {
                    // Expire cached NBT and clear pending Queue if both are disabled
                    if (!this.pendingBlockEntitiesQueue.isEmpty())
                    {
                        this.pendingBlockEntitiesQueue.clear();
                    }

                    if (!this.pendingEntitiesQueue.isEmpty())
                    {
                        this.pendingEntitiesQueue.clear();
                    }

//                    this.tickCache(now);

                    return;
                }
            }
            else if (!DataManager.getInstance().hasIntegratedServer() &&
                    !this.hasServuxServer() &&
                    !this.hasInValidServux &&
                    this.getWorld() != null)
            {
                // Make sure we're Play Registered, and request Metadata
                HANDLER.registerPlayReceiver(ServuxTweaksPacket.Payload.ID, HANDLER::receivePlayPayload);
                this.requestMetadata();
            }

            // Expire cached NBT
            this.tickCache(now);

            // 5 queries / server tick
            for (int i = 0; i < Configs.Generic.SERVER_NBT_REQUEST_RATE.getIntegerValue(); i++)
            {
                if (!this.pendingBlockEntitiesQueue.isEmpty())
                {
                    var iter = this.pendingBlockEntitiesQueue.iterator();
                    BlockPos pos = iter.next();
                    iter.remove();

                    if (this.hasServuxServer())
                    {
                        requestServuxBlockEntityData(pos);
                    }
                    else if (this.shouldUseQuery())
                    {
                        // Only check once if we have OP
                        requestQueryBlockEntity(pos);
                    }
                }
                if (!this.pendingEntitiesQueue.isEmpty())
                {
                    var iter = this.pendingEntitiesQueue.iterator();
                    int entityId = iter.next();
                    iter.remove();
                    if (this.hasServuxServer())
                    {
                        requestServuxEntityData(entityId);
                    }
                    else if (this.shouldUseQuery())
                    {
                        // Only check once if we have OP
                        requestQueryEntityData(entityId);
                    }
                }
            }

            this.serverTickTime = now;
        }
    }

    public Identifier getNetworkChannel()
    {
        return ServuxTweaksHandler.CHANNEL_ID;
    }

    private ClientPlayNetworkHandler getVanillaHandler()
    {
        if (mc.player != null)
        {
            return mc.player.networkHandler;
        }

        return null;
    }

    public IPluginClientPlayHandler<ServuxTweaksPacket.Payload> getNetworkHandler()
    {
        return HANDLER;
    }

    @Override
    public void reset(boolean isLogout)
    {
        if (isLogout)
        {
            Tweakeroo.debugLog("ServerDataSyncer#reset() - log-out");
            HANDLER.reset(this.getNetworkChannel());
            HANDLER.resetFailures(this.getNetworkChannel());
            this.servuxServer = false;
            this.hasInValidServux = false;
            this.checkOpStatus = false;
            this.hasOpStatus = false;
            this.lastOpCheck = 0L;
        }
        else
        {
            Tweakeroo.debugLog("ServerDataSyncer#reset() - dimension change or log-in");
            long now = System.currentTimeMillis();
            this.serverTickTime = now - (this.getCacheTimeout() + 5000L);
            this.tickCache(now);
            this.serverTickTime = now;
            this.clientWorld = mc.world;
            this.checkOpStatus = true;
            this.lastOpCheck = now;
        }

        // Clear data
        this.blockEntityCache.clear();
        this.entityCache.clear();
        this.pendingBlockEntitiesQueue.clear();
        this.pendingEntitiesQueue.clear();
    }

    private boolean shouldUseQuery()
    {
        if (this.hasOpStatus) return true;
        if (this.checkOpStatus)
        {
            // Check for 15 minutes after login, or changing dimensions
            if ((System.currentTimeMillis() - this.lastOpCheck) < 900000L) return true;
            this.checkOpStatus = false;
        }

        return false;
    }

    public void resetOpCheck()
    {
        this.hasOpStatus = false;
        this.checkOpStatus = true;
        this.lastOpCheck = System.currentTimeMillis();
    }

    public long getCacheRefresh()
    {
        long result = (long) (MathHelper.clamp(Configs.Generic.SERVER_DATA_SYNC_CACHE_REFRESH.getFloatValue(), 0.05f, 1.0f) * 1000L);
        long clamp = (this.getCacheTimeout() / 2);

        return Math.min(result, clamp);
    }

    private long getCacheTimeout()
    {
        // Increase cache timeout when in Backup Mode.
        int modifier = FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue() ? 5 : 1;
        return (long) (MathHelper.clamp((Configs.Generic.SERVER_DATA_SYNC_CACHE_TIMEOUT.getFloatValue() * modifier), 0.25f, 15.0f) * 1000L);
    }

    private void tickCache(long nowTime)
    {
        long timeout = this.getCacheTimeout();

        synchronized (this.blockEntityCache)
        {
            for (BlockPos pos : this.blockEntityCache.keySet())
            {
                Pair<Long, Pair<BlockEntity, NbtCompound>> pair = this.blockEntityCache.get(pos);

                if (nowTime - pair.getLeft() > timeout || pair.getLeft() > nowTime)
                {
                    //Tweakeroo.printDebug("entityCache: be at pos [{}] has timed out", pos.toShortString());
                    this.blockEntityCache.remove(pos);
                }
            }
        }

        synchronized (this.entityCache)
        {
            for (Integer entityId : this.entityCache.keySet())
            {
                Pair<Long, Pair<Entity, NbtCompound>> pair = this.entityCache.get(entityId);

                if (nowTime - pair.getLeft() > timeout || pair.getLeft() > nowTime)
                {
                    //Tweakeroo.printDebug("entityCache: entity Id [{}] has timed out", entityId);
                    this.entityCache.remove(entityId);
                }
            }
        }
    }

    @Override
    public @Nullable NbtCompound getFromBlockEntityCacheNbt(BlockPos pos)
    {
        if (this.blockEntityCache.containsKey(pos))
        {
            return this.blockEntityCache.get(pos).getRight().getRight();
        }

        return null;
    }

    @Override
    public @Nullable BlockEntity getFromBlockEntityCache(BlockPos pos)
    {
        if (this.blockEntityCache.containsKey(pos))
        {
            return this.blockEntityCache.get(pos).getRight().getLeft();
        }

        return null;
    }

    @Override
    public @Nullable NbtCompound getFromEntityCacheNbt(int entityId)
    {
        if (this.entityCache.containsKey(entityId))
        {
            return this.entityCache.get(entityId).getRight().getRight();
        }

        return null;
    }

    @Override
    public @Nullable Entity getFromEntityCache(int entityId)
    {
        if (this.entityCache.containsKey(entityId))
        {
            return this.entityCache.get(entityId).getRight().getLeft();
        }

        return null;
    }

    public void setIsServuxServer()
    {
        this.servuxServer = true;
        this.hasInValidServux = false;
    }

    public boolean hasServuxServer()
    {
        return this.servuxServer;
    }

    public boolean hasBackupStatus()
    {
        return FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue() && this.hasOpStatus;
    }

    public void setServuxVersion(String ver)
    {
        if (ver != null && !ver.isEmpty())
        {
            this.servuxVersion = ver;
            Tweakeroo.debugLog("tweaksDataChannel: joining Servux version {}", ver);
        }
        else
        {
            this.servuxVersion = "unknown";
        }
    }

    public String getServuxVersion()
    {
        return this.servuxVersion;
    }

    public int getPendingBlockEntitiesCount()
    {
        return this.pendingBlockEntitiesQueue.size();
    }

    public int getPendingEntitiesCount()
    {
        return this.pendingEntitiesQueue.size();
    }

    public int getBlockEntityCacheCount()
    {
        return this.blockEntityCache.size();
    }

    public int getEntityCacheCount()
    {
        return this.entityCache.size();
    }

    @Override
    public void onGameInit()
    {
        ClientPlayHandler.getInstance().registerClientPlayHandler(HANDLER);
        HANDLER.registerPlayPayload(ServuxTweaksPacket.Payload.ID, ServuxTweaksPacket.Payload.CODEC, IPluginClientPlayHandler.BOTH_CLIENT);
    }

    @Override
    public void onWorldPre()
    {
        if (!DataManager.getInstance().hasIntegratedServer())
        {
            HANDLER.registerPlayReceiver(ServuxTweaksPacket.Payload.ID, HANDLER::receivePlayPayload);
        }
    }

    @Override
    public void onWorldJoin()
    {
        // NO-OP
    }

    public void requestMetadata()
    {
        if (!DataManager.getInstance().hasIntegratedServer() &&
            FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
        {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("version", Reference.MOD_STRING);

            HANDLER.encodeClientData(ServuxTweaksPacket.MetadataRequest(nbt));
        }
    }

    public boolean receiveServuxMetadata(NbtCompound nbt)
    {
        if (!DataManager.getInstance().hasIntegratedServer())
        {
            Tweakeroo.debugLog("tweaksDataChannel: received METADATA from Servux");
            this.checkTweaksConfigs(nbt);

            if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
            {
                if (nbt.getInt("version", -1) != ServuxTweaksPacket.PROTOCOL_VERSION)
                {
                    Tweakeroo.LOGGER.warn("tweaksDataChannel: Mis-matched protocol version!");
                }

                DataManager.getInstance().setHasServuxServer(true);
                this.setServuxVersion(nbt.getString("servux", ""));
                this.setIsServuxServer();
                
                return true;
            }
        }

        return false;
    }
    
    // This is only meant to keep some Tweaks in sync with the Server, such as Stackable Shulkers.
    private void checkTweaksConfigs(NbtCompound nbt)
    {
        if (nbt.contains("stackingShulkers"))
        {
            boolean newValue =nbt.getBoolean("stackingShulkers", FeatureToggle.TWEAK_SHULKERBOX_STACKING.getBooleanValue());
            Tweakeroo.debugLog("checkTweaksConfigs: stackingShulkers: [{}]", newValue);
            FeatureToggle.TWEAK_SHULKERBOX_STACKING.setBooleanValue(newValue);
        }
        if (nbt.contains("stackingShulkersMax"))
        {
            int newValue = Math.clamp(nbt.getInt("stackingShulkersMax", 64), 1, 99);
            Tweakeroo.debugLog("checkTweaksConfigs: stackingShulkersMax: [{}]", newValue);
            Configs.Internal.SHULKER_MAX_STACK_SIZE.setIntegerValue(newValue);
        }
    }

    public void onPacketFailure()
    {
        DataManager.getInstance().setHasServuxServer(false);
        this.servuxServer = false;
        this.hasInValidServux = true;
    }

    @Override
    public @Nullable Pair<BlockEntity, NbtCompound> requestBlockEntity(World world, BlockPos pos)
    {
        if (this.blockEntityCache.containsKey(pos))
        {
            // Refresh at 25%
            if (!DataManager.getInstance().hasIntegratedServer() &&
                (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue() || FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue()))
            {
                if (System.currentTimeMillis() - this.blockEntityCache.get(pos).getLeft() > this.getCacheRefresh())
                {
                    //Tweakeroo.debugLog("requestBlockEntity: be at pos [{}] requeue at [{}] ms", pos.toShortString(), this.getCacheRefresh());
                    this.pendingBlockEntitiesQueue.add(pos);
                }
            }

            if (world instanceof ServerWorld)
            {
                return this.refreshBlockEntityFromWorld(world, pos);
            }

            return this.blockEntityCache.get(pos).getRight();
        }
        else if (world.getBlockState(pos).getBlock() instanceof BlockEntityProvider)
        {
            if (!DataManager.getInstance().hasIntegratedServer() &&
                (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue() || FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue()))
            {
                this.pendingBlockEntitiesQueue.add(pos);
            }

            return this.refreshBlockEntityFromWorld(this.getClientWorld(), pos);
        }

        return null;
    }

    private @Nullable Pair<BlockEntity, NbtCompound> refreshBlockEntityFromWorld(World world, BlockPos pos)
    {
        if (world != null && world.getBlockState(pos).hasBlockEntity())
        {
            BlockEntity be = world.getWorldChunk(pos).getBlockEntity(pos);

            if (be != null)
            {
                NbtCompound nbt = be.createNbtWithIdentifyingData(world.getRegistryManager());
                Pair<BlockEntity, NbtCompound> pair = Pair.of(be, nbt);

                synchronized (this.blockEntityCache)
                {
                    this.blockEntityCache.put(pos, Pair.of(System.currentTimeMillis(), pair));
                }

                return pair;
            }
        }

        return null;
    }

    @Override
    public @Nullable Pair<Entity, NbtCompound> requestEntity(World world, int entityId)
    {
        if (this.entityCache.containsKey(entityId))
        {
            // Refresh at 25%
            if (!DataManager.getInstance().hasIntegratedServer() &&
                (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue() || FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue()))
            {
                if (System.currentTimeMillis() - this.entityCache.get(entityId).getLeft() > this.getCacheRefresh())
                {
                    //Tweakeroo.debugLog("requestEntity: entity Id [{}] requeue at [{}] ms", entityId, this.getCacheRefresh());
                    this.pendingEntitiesQueue.add(entityId);
                }
            }

            // Refresh from Server World
            if (world instanceof ServerWorld)
            {
                return this.refreshEntityFromWorld(world, entityId);
            }

            return this.entityCache.get(entityId).getRight();
        }
        if (!DataManager.getInstance().hasIntegratedServer() &&
            (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue() || FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue()))
        {
            this.pendingEntitiesQueue.add(entityId);
        }

        return this.refreshEntityFromWorld(this.getClientWorld(), entityId);
    }

    private @Nullable Pair<Entity, NbtCompound> refreshEntityFromWorld(World world, int entityId)
    {
        if (world != null)
        {
            Entity entity = world.getEntityById(entityId);

            if (entity != null)
            {
//                Identifier id = EntityType.getId(entity.getType());
//                NbtView view = NbtView.getWriter(world.getRegistryManager());
//                NbtCompound nbt;
//
//                entity.writeData(view.getWriter());
//                nbt = view.readNbt();
                NbtCompound nbt = NbtEntityUtils.invokeEntityNbtDataNoPassengers(entity, entityId);

//                if (nbt != null && id != null)
                if (!nbt.isEmpty())
                {
//                    nbt.putString("id", id.toString());
                    Pair<Entity, NbtCompound> pair = Pair.of(entity, nbt);

                    synchronized (this.entityCache)
                    {
                        this.entityCache.put(entityId, Pair.of(System.currentTimeMillis(), pair));
                    }

                    return pair;
                }
            }
        }

        return null;
    }

    @Override
    @Nullable
    public Inventory getBlockInventory(World world, BlockPos pos, boolean useNbt)
    {
        if (this.blockEntityCache.containsKey(pos))
        {
            Inventory inv = null;

            if (useNbt)
            {
                inv = InventoryUtils.getNbtInventory(this.blockEntityCache.get(pos).getRight().getRight(), -1, world.getRegistryManager());
            }
            else
            {
                BlockEntity be = this.blockEntityCache.get(pos).getRight().getLeft();
                BlockState state = world.getBlockState(pos);

                if (state.isIn(BlockTags.AIR) || !state.hasBlockEntity())
                {
                    synchronized (this.blockEntityCache)
                    {
                        this.blockEntityCache.remove(pos);
                    }

                    // Don't keep requesting if we're tick warping or something.
                    return null;
                }

                if (be instanceof Inventory inv1)
                {
                    if (be instanceof ChestBlockEntity && state.contains(ChestBlock.CHEST_TYPE))
                    {
                        ChestType type = state.get(ChestBlock.CHEST_TYPE);

                        if (type != ChestType.SINGLE)
                        {
                            BlockPos posAdj = pos.offset(ChestBlock.getFacing(state));
                            if (!world.isChunkLoaded(posAdj)) return null;
                            BlockState stateAdj = world.getBlockState(posAdj);

                            var dataAdj = this.getFromBlockEntityCache(posAdj);

                            if (dataAdj == null)
                            {
                                this.requestBlockEntity(world, posAdj);
                            }

                            if (stateAdj.getBlock() == state.getBlock() &&
                                dataAdj instanceof ChestBlockEntity inv2 &&
                                stateAdj.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE &&
                                stateAdj.get(ChestBlock.FACING) == state.get(ChestBlock.FACING))
                            {
                                Inventory invRight = type == ChestType.RIGHT ? inv1 : inv2;
                                Inventory invLeft = type == ChestType.RIGHT ? inv2 : inv1;

                                inv = new DoubleInventory(invRight, invLeft);
                            }
                        }
                        else
                        {
                            inv = inv1;
                        }
                    }
                    else
                    {
                        inv = inv1;
                    }
                }
            }

            if (inv != null)
            {
                return inv;
            }
        }

        if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue() || FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue())
        {
            this.requestBlockEntity(world, pos);
        }

        return null;
    }

    @Override
    @Nullable
    public Inventory getEntityInventory(World world, int entityId, boolean useNbt)
    {
        if (this.entityCache.containsKey(entityId) && this.getWorld() != null)
        {
            Inventory inv = null;

            if (useNbt)
            {
                inv = InventoryUtils.getNbtInventory(this.entityCache.get(entityId).getRight().getRight(), -1, this.getWorld().getRegistryManager());
            }
            else
            {
                Entity entity = this.entityCache.get(entityId).getRight().getLeft();

                if (entity instanceof Inventory)
                {
                    inv = (Inventory) entity;
                }
                else if (entity instanceof PlayerEntity player)
                {
                    inv = new SimpleInventory(player.getInventory().getMainStacks().toArray(new ItemStack[36]));
                }
                else if (entity instanceof VillagerEntity)
                {
                    inv = ((VillagerEntity) entity).getInventory();
                }
                else if (entity instanceof AbstractHorseEntity)
                {
                    inv = ((IMixinAbstractHorseEntity) entity).malilib_getHorseInventory();
                }
                else if (entity instanceof PiglinEntity)
                {
                    inv = ((IMixinPiglinEntity) entity).malilib_getInventory();
                }
            }

            if (inv != null)
            {
                return inv;
            }
        }

        if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue() || FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue())
        {
            this.requestEntity(world, entityId);
        }

        return null;
    }

    private void requestQueryBlockEntity(BlockPos pos)
    {
        if (!FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue())
        {
            return;
        }

        ClientPlayNetworkHandler handler = this.getVanillaHandler();

        if (handler != null)
        {
            handler.getDataQueryHandler().queryBlockNbt(pos, nbtCompound -> handleBlockEntityData(pos, nbtCompound, null));
            this.transactionToBlockPosOrEntityId.put(((IMixinDataQueryHandler) handler.getDataQueryHandler()).malilib_currentTransactionId(), Either.left(pos));
        }
    }

    private void requestQueryEntityData(int entityId)
    {
        if (!FeatureToggle.TWEAK_SERVER_DATA_SYNC_BACKUP.getBooleanValue())
        {
            return;
        }

        ClientPlayNetworkHandler handler = this.getVanillaHandler();

        if (handler != null)
        {
            handler.getDataQueryHandler().queryEntityNbt(entityId, nbtCompound -> handleEntityData(entityId, nbtCompound));
            this.transactionToBlockPosOrEntityId.put(((IMixinDataQueryHandler) handler.getDataQueryHandler()).malilib_currentTransactionId(), Either.right(entityId));
        }
    }

    private void requestServuxBlockEntityData(BlockPos pos)
    {
        if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
        {
            HANDLER.encodeClientData(ServuxTweaksPacket.BlockEntityRequest(pos));
        }
    }

    private void requestServuxEntityData(int entityId)
    {
        if (FeatureToggle.TWEAK_SERVER_DATA_SYNC.getBooleanValue())
        {
            HANDLER.encodeClientData(ServuxTweaksPacket.EntityRequest(entityId));
        }
    }

    @Override
    @Nullable
    public BlockEntity handleBlockEntityData(BlockPos pos, NbtCompound nbt, @Nullable Identifier type)
    {
        this.pendingBlockEntitiesQueue.remove(pos);
        if (nbt == null || this.getClientWorld() == null) return null;

        BlockEntity blockEntity = this.getClientWorld().getBlockEntity(pos);

        if (blockEntity != null && (type == null || type.equals(BlockEntityType.getId(blockEntity.getType()))))
        {
            if (!nbt.contains(NbtKeys.ID))
            {
                Identifier id = BlockEntityType.getId(blockEntity.getType());

                if (id != null)
                {
                    nbt.putString(NbtKeys.ID, id.toString());
                }
            }
            synchronized (this.blockEntityCache)
            {
                this.blockEntityCache.put(pos, Pair.of(System.currentTimeMillis(), Pair.of(blockEntity, nbt)));
            }

            NbtView view = NbtView.getReader(nbt, this.getClientWorld().getRegistryManager());
            blockEntity.read(view.getReader());
            return blockEntity;
        }

        Optional<RegistryEntry.Reference<BlockEntityType<?>>> opt = Registries.BLOCK_ENTITY_TYPE.getEntry(type);

        if (opt.isPresent())
        {
            BlockEntityType<?> beType = opt.get().value();

            if (beType.supports(this.getClientWorld().getBlockState(pos)))
            {
                BlockEntity blockEntity2 = beType.instantiate(pos, this.getClientWorld().getBlockState(pos));

                if (blockEntity2 != null)
                {
                    if (!nbt.contains(NbtKeys.ID))
                    {
                        Identifier id = BlockEntityType.getId(beType);

                        if (id != null)
                        {
                            nbt.putString(NbtKeys.ID, id.toString());
                        }
                    }
                    synchronized (this.blockEntityCache)
                    {
                        this.blockEntityCache.put(pos, Pair.of(System.currentTimeMillis(), Pair.of(blockEntity2, nbt)));
                    }

                    return blockEntity2;
                }
            }
        }

        return null;
    }

    @Override
    @Nullable
    public Entity handleEntityData(int entityId, NbtCompound nbt)
    {
        this.pendingEntitiesQueue.remove(entityId);
        if (nbt == null || this.getClientWorld() == null) return null;
        Entity entity = this.getClientWorld().getEntityById(entityId);

        if (entity != null)
        {
            if (!nbt.contains(NbtKeys.ID))
            {
                Identifier id = EntityType.getId(entity.getType());

                if (id != null)
                {
                    nbt.putString(NbtKeys.ID, id.toString());
                }
            }
            synchronized (this.entityCache)
            {
                this.entityCache.put(entityId, Pair.of(System.currentTimeMillis(), Pair.of(entity, nbt)));
            }
        }
        return entity;
    }

    @Override
    public void handleBulkEntityData(int transactionId, NbtCompound nbt)
    {
        // todo
    }

    @Override
    public void handleVanillaQueryNbt(int transactionId, NbtCompound nbt)
    {
        if (this.checkOpStatus)
        {
            this.hasOpStatus = true;
            this.checkOpStatus = false;
            this.lastOpCheck = System.currentTimeMillis();
        }

        Either<BlockPos, Integer> either = this.transactionToBlockPosOrEntityId.remove(transactionId);

        if (either != null)
        {
            either.ifLeft(pos -> handleBlockEntityData(pos, nbt, null))
                  .ifRight(entityId -> handleEntityData(entityId, nbt));
        }
    }
}
