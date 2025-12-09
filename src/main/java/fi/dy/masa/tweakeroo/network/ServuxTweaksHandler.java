package fi.dy.masa.tweakeroo.network;

import io.netty.buffer.Unpooled;
import org.jspecify.annotations.NonNull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import fi.dy.masa.malilib.network.IClientPayloadData;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import fi.dy.masa.malilib.network.PacketSplitter;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.data.EntityDataManager;

@Environment(EnvType.CLIENT)
public abstract class ServuxTweaksHandler<T extends CustomPacketPayload> implements IPluginClientPlayHandler<T>
{
    private static final ServuxTweaksHandler<ServuxTweaksPacket.Payload> INSTANCE = new ServuxTweaksHandler<>()
    {
        @Override
        public void receive(ServuxTweaksPacket.Payload payload, ClientPlayNetworking.@NonNull Context context)
        {
            ServuxTweaksHandler.INSTANCE.receivePlayPayload(payload, context);
        }
    };
    public static ServuxTweaksHandler<ServuxTweaksPacket.Payload> getInstance() { return INSTANCE; }

    public static final Identifier CHANNEL_ID = Identifier.fromNamespaceAndPath("servux", "tweaks");

    private boolean servuxRegistered;
    private boolean payloadRegistered = false;
    private int failures = 0;
    private static final int MAX_FAILURES = 4;
    private long readingSessionKey = -1;

    @Override
    public Identifier getPayloadChannel() { return CHANNEL_ID; }

    @Override
    public boolean isPlayRegistered(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID))
        {
            return payloadRegistered;
        }

        return false;
    }

    @Override
    public void setPlayRegistered(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID))
        {
            this.payloadRegistered = true;
        }
    }

    @Override
    public <P extends IClientPayloadData> void decodeClientData(Identifier channel, P data)
    {
        ServuxTweaksPacket packet = (ServuxTweaksPacket) data;

        if (!channel.equals(CHANNEL_ID))
        {
            return;
        }
        switch (packet.getType())
        {
            case PACKET_S2C_METADATA ->
            {
                if (EntityDataManager.getInstance().receiveServuxMetadata(packet.getCompound()))
                {
                    this.servuxRegistered = true;
                }
            }
            case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE -> EntityDataManager.getInstance().handleBlockEntityData(packet.getPos(), packet.getCompound(), null);
            case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE -> EntityDataManager.getInstance().handleEntityData(packet.getEntityId(), packet.getCompound());
            case PACKET_S2C_NBT_RESPONSE_DATA ->
            {
                if (this.readingSessionKey == -1)
                {
                    this.readingSessionKey = RandomSource.create(Util.getMillis()).nextLong();
                }

                //Tweakeroo.printDebug("ServuxTweaksHandler#decodeClientData(): received Tweaks Data Packet Slice of size {} (in bytes) // reading session key [{}]", packet.getTotalSize(), this.readingSessionKey);
                FriendlyByteBuf fullPacket = PacketSplitter.receive(this, this.readingSessionKey, packet.getBuffer());

                if (fullPacket != null)
                {
                    try
                    {
                        this.readingSessionKey = -1;
                        EntityDataManager.getInstance().handleBulkEntityData(fullPacket.readVarInt(), (CompoundTag) fullPacket.readNbt(NbtAccounter.unlimitedHeap()));
                    }
                    catch (Exception e)
                    {
                        Tweakeroo.LOGGER.error("ServuxTweaksHandler#decodeClientData(): Tweaks Data: error reading fullBuffer [{}]", e.getLocalizedMessage());
                    }
                }
            }
            default -> Tweakeroo.LOGGER.warn("ServuxTweaksHandler#decodeClientData(): received unhandled packetType {} of size {} bytes.", packet.getPacketType(), packet.getTotalSize());
        }
    }

    @Override
    public void reset(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID) && this.servuxRegistered)
        {
            this.servuxRegistered = false;
            this.failures = 0;
            this.readingSessionKey = -1;
        }
    }

    public void resetFailures(Identifier channel)
    {
        if (channel.equals(CHANNEL_ID) && this.failures > 0)
        {
            this.failures = 0;
        }
    }

    @Override
    public void receivePlayPayload(T payload, ClientPlayNetworking.Context ctx)
    {
        if (payload.type().id().equals(CHANNEL_ID))
        {
            ServuxTweaksHandler.INSTANCE.decodeClientData(CHANNEL_ID, ((ServuxTweaksPacket.Payload) payload).data());
        }
    }

    @Override
    public void encodeWithSplitter(FriendlyByteBuf buffer, ClientPacketListener handler)
    {
        // Send each PacketSplitter buffer slice
        ServuxTweaksHandler.INSTANCE.sendPlayPayload(new ServuxTweaksPacket.Payload(ServuxTweaksPacket.ResponseS2CData(buffer)));
    }

    @Override
    public <P extends IClientPayloadData> void encodeClientData(P data)
    {
        ServuxTweaksPacket packet = (ServuxTweaksPacket) data;

        if (packet.getType().equals(ServuxTweaksPacket.Type.PACKET_C2S_NBT_RESPONSE_START))
        {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeVarInt(packet.getTransactionId());
            buffer.writeNbt(packet.getCompound());
            PacketSplitter.send(this, buffer, Minecraft.getInstance().getConnection());
        }
        else if (!ServuxTweaksHandler.INSTANCE.sendPlayPayload(new ServuxTweaksPacket.Payload(packet)))
        {
            if (this.failures > MAX_FAILURES)
            {
                Tweakeroo.debugLog("ServuxTweaksHandler#encodeClientData(): encountered [{}] sendPayload failures, cancelling any Servux join attempt(s)", MAX_FAILURES);
                this.servuxRegistered = false;
                ServuxTweaksHandler.INSTANCE.unregisterPlayReceiver();
                EntityDataManager.getInstance().onPacketFailure();
            }
            else
            {
                this.failures++;
            }
        }
    }
}
