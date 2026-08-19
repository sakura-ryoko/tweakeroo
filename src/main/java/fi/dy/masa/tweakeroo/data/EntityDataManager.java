package fi.dy.masa.tweakeroo.data;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.malilib.interfaces.IDataSyncer;
import fi.dy.masa.malilib.mixin.network.IMixinDebugQueryHandler;
import fi.dy.masa.malilib.network.ClientPlayHandler;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.MathUtils;
import fi.dy.masa.malilib.util.WorldUtils;
import fi.dy.masa.malilib.util.data.Constants;
import fi.dy.masa.malilib.util.data.tag.CompoundData;
import fi.dy.masa.malilib.util.data.tag.converter.DataConverterNbt;
import fi.dy.masa.malilib.util.data_syncer.EntityDataCache;
import fi.dy.masa.malilib.util.data_syncer.EntityDataRequestTracker;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.network.ServuxTweaksHandler;
import fi.dy.masa.tweakeroo.network.ServuxTweaksPacket;

public class EntityDataManager implements IClientTickHandler, IDataSyncer
{
    private static final EntityDataManager INSTANCE = new EntityDataManager();
    public static EntityDataManager getInstance()
    {
        return INSTANCE;
    }
    
    private final static ServuxTweaksHandler<ServuxTweaksPacket.Payload> HANDLER = ServuxTweaksHandler.getInstance();
    private final Minecraft mc;
    private ClientLevel clientWorld;
    private boolean servuxServer = false;
    private boolean hasInValidServux = false;
    private String servuxVersion;
    private boolean checkOpStatus = true;
    private boolean hasOpStatus = false;
    private long lastOpCheck;
    private boolean sentBackupPackets = false;
    private boolean receivedBackupPackets = false;

    private final EntityDataCache cache;
    private final EntityDataRequestTracker requestTracker;
    private final Map<Integer, Either<BlockPos, Integer>> transactionToBlockPosOrEntityId = new HashMap<>();
    private long lastTickTime;

    @Override
    @Nullable
    public Level getBestWorld()
    {
        return WorldUtils.getBestWorld(this.mc);
    }

    @Override
    public ClientLevel getClientWorld()
    {
        if (this.clientWorld == null)
        {
            this.clientWorld = this.mc.level;
        }

        return this.clientWorld;
    }

    public EntityDataManager()
    {
        this.mc = Minecraft.getInstance();
        this.cache = new EntityDataCache(Reference.MOD_ID, this.getCacheTimeout());
        this.requestTracker = new EntityDataRequestTracker();
        this.lastTickTime = System.currentTimeMillis();
        this.lastOpCheck = 0L;
        Registry.ENTITY_DATA_REGISTRY.registerEntityDataCache(this.cache);
    }

    @Override
    public void onClientTick(Minecraft mc)
    {
        long now = System.currentTimeMillis();

        if ((now - this.lastTickTime) > 50L)
        {
            if (this.mc.level == null || this.mc.player == null)
            {
                this.getCache().clearAll();
                this.getRequestTracker().clearAll();
                this.lastTickTime = now;
                return;
            }

            // In this block, we do something every server tick
            if (!Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue())
            {
                this.lastTickTime = now;
                if (!DataManager.getInstance().hasIntegratedServer() && this.hasServuxServer())
                {
                    this.servuxServer = false;
                    HANDLER.encodeClientData(ServuxTweaksPacket.UnregisterReply(new CompoundData()));
                    HANDLER.unregisterPlayReceiver();
                    HANDLER.reset(this.getNetworkChannel());
                }

                if (!Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue())
                {
                    this.requestTracker.clearAll();
//                    this.lastTickTime = now;
//                    return;
                }
            }
            else if (Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue() &&
                    !DataManager.getInstance().hasIntegratedServer() &&
                    !this.hasServuxServer() &&
                    !this.hasInValidServux &&
                    this.getBestWorld() != null)
            {
                // Make sure we're Play Registered, and request Metadata
                HANDLER.registerPlayReceiver(ServuxTweaksPacket.Payload.ID, HANDLER::receivePlayPayload);
                this.requestMetadata();
            }

            if (this.cache.getTimeout() != this.getCacheTimeout())
            {
                this.cache.setTimeout(this.getCacheTimeout());
            }

            // Expire cached NBT
            this.cache.tickCache(now);

            // 5 queries / server tick
            final int limit = Configs.Generic.SERVER_NBT_REQUEST_RATE.getIntegerValue();

            for (int i = 0; i < limit; i++)
            {
                BlockPos nextPos = this.requestTracker.pollNextBlockEntity();
                Integer nextId = this.requestTracker.pollNextEntity();

                if (nextPos == null && nextId == null) { break; }

                if (nextPos != null)
                {
                    if (this.hasServuxServer())
                    {
                        this.requestServuxBlockEntityData(nextPos);
                    }
                    else if (this.shouldUseQuery())
                    {
                        // Only check once if we have OP
                        this.requestQueryBlockEntity(nextPos);
                    }
                }

                if (nextId != null)
                {
                    if (this.hasServuxServer())
                    {
                        this.requestServuxEntityData(nextId);
                    }
                    else if (this.shouldUseQuery())
                    {
                        // Only check once if we have OP
                        this.requestQueryEntityData(nextId);
                    }
                }
            }

            this.lastTickTime = now;
        }
    }

    public Identifier getNetworkChannel()
    {
        return ServuxTweaksHandler.CHANNEL_ID;
    }

    private ClientPacketListener getVanillaHandler()
    {
        if (mc.player != null)
        {
            return mc.player.connection;
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
            this.sentBackupPackets = false;
            this.receivedBackupPackets = false;
            this.checkOpStatus = false;
            this.hasOpStatus = false;
            this.lastOpCheck = 0L;
        }
        else
        {
            Tweakeroo.debugLog("ServerDataSyncer#reset() - dimension change or log-in");
            long now = System.currentTimeMillis();
            this.lastTickTime = now - (this.getCacheTimeout() + 5000L);
            this.cache.tickCache(now);
            this.lastTickTime = now;
            this.clientWorld = this.mc.level;
            this.checkOpStatus = true;
            this.lastOpCheck = now;
        }

        // Clear data
        this.clearAll();
    }

    private boolean shouldUseQuery()
    {
        if (this.hasOpStatus) { return true; }
        if (this.checkOpStatus)
        {
            // Check for 15 minutes after login, or changing dimensions
            if ((System.currentTimeMillis() - this.lastOpCheck) < 900000L) { return true; }
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

    @Override
    public EntityDataCache getCache()
    {
        return this.cache;
    }

    @Override
    public EntityDataRequestTracker getRequestTracker()
    {
        return this.requestTracker;
    }

    @Override
    public boolean isEnabled()
    {
        return Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue();
    }

    @Override
    public boolean isBackupEnabled()
    {
        return Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue();
    }

    @Override
    public boolean loadContainerBlockEntities()
    {
        return true;
    }

    @Override
    public long getRefreshTime()
    {
        long result = (long) (Mth.clamp(Configs.Generic.ENTITY_DATA_SYNC_CACHE_REFRESH.getFloatValue(), 0.05f, 1.0f) * 1000L);
        long clamp = (this.getCacheTimeout() / 2);
        return MathUtils.min(result, clamp);
    }

    @Override
    public long getCacheTimeout()
    {
        // Increase cache timeout when in Backup Mode.
        int modifier = 1;

        if (!this.hasServuxServer())
        {
            if (!this.hasBackupStatus())
            {
                modifier = 10;
            }
            else
            {
                modifier = 5;
            }
        }

        return (long) (MathUtils.clamp((Configs.Generic.ENTITY_DATA_SYNC_CACHE_TIMEOUT.getFloatValue() * modifier), 1.0f, 500.0f) * 1000L);
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
        return Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue() && this.hasOpStatus;
    }

    public boolean hasOperatorStatus()
    {
        return this.hasOpStatus;
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
        return this.requestTracker.getPendingBlockEntityCount();
    }

    public int getPendingEntitiesCount()
    {
        return this.requestTracker.getPendingEntityCount();
    }

    public int getBlockEntityCacheCount()
    {
        return this.cache.blockEntityCount();
    }

    public int getEntityCacheCount()
    {
        return this.cache.entityCount();
    }

    public boolean getIfReceivedBackupPackets()
    {
        if (Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue())
        {
            return this.sentBackupPackets & this.receivedBackupPackets;
        }

        return false;
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
            Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue())
        {
            CompoundData nbt = new CompoundData();
            nbt.putInt("version", ServuxTweaksPacket.PROTOCOL_VERSION);
            HANDLER.encodeClientData(ServuxTweaksPacket.MetadataRequest(nbt));
        }
    }

    @Deprecated(forRemoval = true)
    public boolean receiveServuxMetadata(CompoundTag data)
    {
        return this.receiveServuxMetadata(DataConverterNbt.fromVanillaCompound(data));
    }

    public boolean receiveServuxMetadata(CompoundData data)
    {
        if (!DataManager.getInstance().hasIntegratedServer())
        {
            Tweakeroo.debugLog("tweaksDataChannel: received METADATA from Servux");
            this.checkTweaksConfigs(data);

            if (Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue())
            {
                final int version = data.getIntOrDefault("version", -1);
                final String servux = data.getStringOrDefault("servux", "?");

                if (version != ServuxTweaksPacket.PROTOCOL_VERSION || !servux.startsWith("servux-"+Reference.MOD_TYPE+"-"+Reference.MC_VERSION))
                {
                    Tweakeroo.LOGGER.warn("tweaksDataChannel: Mis-matched protocol version! (Expected: {} but got {} running on: {})", ServuxTweaksPacket.PROTOCOL_VERSION, version, servux);

                    if (version >= ServuxTweaksPacket.PROTOCOL_VERSION)
                    {
                        HANDLER.encodeClientData(ServuxTweaksPacket.UnregisterReply(new CompoundData()));
                    }

                    HANDLER.unregisterPlayReceiver();
                    HANDLER.reset(this.getNetworkChannel());
                    Configs.Generic.ENTITY_DATA_SYNC.setBooleanValue(false);
                    return false;
                }

                Tweakeroo.debugLog("tweaksDataChannel: Connected to: {}", servux);
                DataManager.getInstance().setHasServuxServer(true);
                this.setServuxVersion(servux);
                this.setIsServuxServer();

                return true;
            }
        }

        return false;
    }

    @Deprecated(forRemoval = true)
    private void checkTweaksConfigs(CompoundTag nbt)
    {
        this.checkTweaksConfigs(DataConverterNbt.fromVanillaCompound(nbt));
    }

    // This is only meant to keep some Tweaks in sync with the Server, such as Stackable Shulkers.
    private void checkTweaksConfigs(CompoundData nbt)
    {
        if (nbt.contains("stackingShulkers", Constants.NBT.TAG_BYTE))
        {
            boolean newValue = nbt.getBooleanOrDefault("stackingShulkers", FeatureToggle.TWEAK_SHULKERBOX_STACKING.getBooleanValue());
            Tweakeroo.debugLog("checkTweaksConfigs: stackingShulkers: [{}]", newValue);
            FeatureToggle.TWEAK_SHULKERBOX_STACKING.setBooleanValue(newValue);
        }
        if (nbt.contains("stackingShulkersMax", Constants.NBT.TAG_INT))
        {
            int newValue = Math.clamp(nbt.getIntOrDefault("stackingShulkersMax", 64), 1, 99);
            Tweakeroo.debugLog("checkTweaksConfigs: stackingShulkersMax: [{}]", newValue);
            Configs.Internal.SHULKER_MAX_STACK_SIZE.setIntegerValue(newValue);
        }
    }

    public void onPacketFailure()
    {
        Configs.Generic.ENTITY_DATA_SYNC.setBooleanValue(false);
        DataManager.getInstance().setHasServuxServer(false);
        this.servuxServer = false;
        this.hasInValidServux = true;
    }

    public void onEntityDataSyncToggled(ConfigBoolean config)
    {
        if (this.hasInValidServux)
        {
            this.reset(true);
        }

        // Do something?
    }

    private void requestQueryBlockEntity(BlockPos pos)
    {
        if (!Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue())
        {
            return;
        }

        ClientPacketListener handler = this.getVanillaHandler();

        if (handler != null)
        {
            this.sentBackupPackets = true;
            handler.getDebugQueryHandler().queryBlockEntityTag(pos, nbtCompound -> this.handleBlockEntityData(pos, nbtCompound));
            this.transactionToBlockPosOrEntityId.put(((IMixinDebugQueryHandler) handler.getDebugQueryHandler()).malilib_currentTransactionId(), Either.left(pos));
        }
    }

    private void requestQueryEntityData(int entityId)
    {
        if (!Configs.Generic.ENTITY_DATA_SYNC_BACKUP.getBooleanValue())
        {
            return;
        }

        ClientPacketListener handler = this.getVanillaHandler();

        if (handler != null)
        {
            this.sentBackupPackets = true;
            handler.getDebugQueryHandler().queryEntityTag(entityId, nbtCompound -> this.handleEntityData(entityId, nbtCompound));
            this.transactionToBlockPosOrEntityId.put(((IMixinDebugQueryHandler) handler.getDebugQueryHandler()).malilib_currentTransactionId(), Either.right(entityId));
        }
    }

    private void requestServuxBlockEntityData(BlockPos pos)
    {
        if (Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue())
        {
            HANDLER.encodeClientData(ServuxTweaksPacket.BlockEntityRequest(pos));
        }
    }

    private void requestServuxEntityData(int entityId)
    {
        if (Configs.Generic.ENTITY_DATA_SYNC.getBooleanValue())
        {
            HANDLER.encodeClientData(ServuxTweaksPacket.EntityRequest(entityId));
        }
    }

    @Override
    public void handleVanillaQueryNbt(int transactionId, CompoundTag nbt)
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
            this.receivedBackupPackets = true;
            either.ifLeft(pos -> this.handleBlockEntityData(pos, nbt))
                  .ifRight(entityId -> this.handleEntityData(entityId, nbt));
        }
    }
}
