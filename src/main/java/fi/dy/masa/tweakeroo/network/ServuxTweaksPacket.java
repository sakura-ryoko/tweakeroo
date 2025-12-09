package fi.dy.masa.tweakeroo.network;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import io.netty.buffer.Unpooled;
import fi.dy.masa.malilib.network.IClientPayloadData;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class ServuxTweaksPacket implements IClientPayloadData
{
    private Type packetType;
    private int transactionId;
    private int entityId;
    private BlockPos pos;
    private CompoundTag nbt;
    private FriendlyByteBuf buffer;
    private List<ChunkPos> requestingChunks;
    public static final int PROTOCOL_VERSION = 1;

    private ServuxTweaksPacket(Type type)
    {
        this.packetType = type;
        this.transactionId = -1;
        this.entityId = -1;
        this.pos = BlockPos.ZERO;
        this.nbt = new CompoundTag();
        this.clearPacket();
    }

    public static ServuxTweaksPacket MetadataRequest(@Nullable CompoundTag nbt)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_C2S_METADATA_REQUEST);
        if (nbt != null)
        {
            packet.nbt.merge(nbt);
        }
        return packet;
    }

    public static ServuxTweaksPacket MetadataResponse(@Nullable CompoundTag nbt)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_S2C_METADATA);
        if (nbt != null)
        {
            packet.nbt.merge(nbt);
        }
        return packet;
    }

    // Entity simple response
    public static ServuxTweaksPacket SimpleEntityResponse(int entityId, @Nullable CompoundTag nbt)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE);
        if (nbt != null)
        {
            packet.nbt.merge(nbt);
        }
        packet.entityId = entityId;
        return packet;
    }

    public static ServuxTweaksPacket SimpleBlockResponse(BlockPos pos, @Nullable CompoundTag nbt)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE);
        if (nbt != null)
        {
            packet.nbt.merge(nbt);
        }
        packet.pos = pos.immutable();
        return packet;
    }

    public static ServuxTweaksPacket BlockEntityRequest(BlockPos pos)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_C2S_BLOCK_ENTITY_REQUEST);
        packet.pos = pos.immutable();
        return packet;
    }

    public static ServuxTweaksPacket EntityRequest(int entityId)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_C2S_ENTITY_REQUEST);
        packet.entityId = entityId;
        return packet;
    }

    // Nbt Packet, using Packet Splitter
    public static ServuxTweaksPacket ResponseS2CStart(@Nonnull CompoundTag nbt)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_S2C_NBT_RESPONSE_START);
        packet.nbt.merge(nbt);
        return packet;
    }

    public static ServuxTweaksPacket ResponseS2CData(@Nonnull FriendlyByteBuf buffer)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_S2C_NBT_RESPONSE_DATA);
        packet.buffer = new FriendlyByteBuf(buffer.copy());
        packet.nbt = new CompoundTag();
        return packet;
    }

    public static ServuxTweaksPacket ResponseC2SStart(@Nonnull CompoundTag nbt)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_C2S_NBT_RESPONSE_START);
        packet.nbt.merge(nbt);
        return packet;
    }

    public static ServuxTweaksPacket ResponseC2SData(@Nonnull FriendlyByteBuf buffer)
    {
        var packet = new ServuxTweaksPacket(Type.PACKET_C2S_NBT_RESPONSE_DATA);
        packet.buffer = new FriendlyByteBuf(buffer.copy());
        packet.nbt = new CompoundTag();
        return packet;
    }

    private void clearPacket()
    {
        if (this.buffer != null)
        {
            this.buffer.clear();
            this.buffer = new FriendlyByteBuf(Unpooled.buffer());
        }
    }

    @Override
    public int getVersion()
    {
        return PROTOCOL_VERSION;
    }

    @Override
    public int getPacketType()
    {
        return this.packetType.get();
    }

    @Override
    public int getTotalSize()
    {
        int total = 2;

        if (this.nbt != null && !this.nbt.isEmpty())
        {
            total += this.nbt.sizeInBytes();
        }
        if (this.buffer != null)
        {
            total += this.buffer.readableBytes();
        }

        return total;
    }

    public Type getType()
    {
        return this.packetType;
    }

    public void setTransactionId(int id)
    {
        this.transactionId = id;
    }

    public int getTransactionId()
    {
        return this.transactionId;
    }

    public int getEntityId() { return this.entityId; }

    public BlockPos getPos() { return this.pos; }

    public CompoundTag getCompound()
    {
        return this.nbt;
    }

    public FriendlyByteBuf getBuffer()
    {
        return this.buffer;
    }

    public boolean hasBuffer() { return this.buffer != null && this.buffer.isReadable(); }

    public boolean hasNbt() { return this.nbt != null && !this.nbt.isEmpty(); }

    @Override
    public boolean isEmpty()
    {
        return !this.hasBuffer() && !this.hasNbt();
    }

    @Override
    public void toPacket(FriendlyByteBuf output)
    {
        output.writeVarInt(this.packetType.get());

        switch (this.packetType)
        {
            case PACKET_C2S_BLOCK_ENTITY_REQUEST ->
            {
                // Write BE Request
                try
                {
                    output.writeVarInt(this.transactionId);
                    output.writeBlockPos(this.pos);
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: error writing Block Entity Request to packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_C2S_ENTITY_REQUEST ->
            {
                // Write Entity Request
                try
                {
                    output.writeVarInt(this.transactionId);
                    output.writeVarInt(this.entityId);
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: error writing Entity Request to packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE ->
            {
                try
                {
                    output.writeBlockPos(this.pos);
                    output.writeNbt(this.nbt);
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: error writing Block Entity Response to packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE ->
            {
                try
                {
                    output.writeVarInt(this.entityId);
                    output.writeNbt(this.nbt);
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: error writing Entity Response to packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_S2C_NBT_RESPONSE_DATA, PACKET_C2S_NBT_RESPONSE_DATA ->
            {
                // Write Packet Buffer (Slice)
                try
                {
                    /*
                    PacketByteBuf serverReplay = new PacketByteBuf(this.buffer.copy());
                    output.writeBytes(serverReplay.readBytes(serverReplay.readableBytes()));
                     */

                    output.writeBytes(this.buffer.copy());
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: error writing buffer data to packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_C2S_METADATA_REQUEST, PACKET_S2C_METADATA ->
            {
                // Write NBT
                try
                {
                    output.writeNbt(this.nbt);
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: error writing NBT to packet: [{}]", e.getLocalizedMessage());
                }
            }
            default -> Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: Unknown packet type!");
        }
    }

    @Nullable
    public static ServuxTweaksPacket fromPacket(FriendlyByteBuf input)
    {
        int i = input.readVarInt();
        Type type = getType(i);

        if (type == null)
        {
            // Invalid Type
            Tweakeroo.LOGGER.warn("ServuxTweaksPacket#fromPacket: invalid packet type received");
            return null;
        }
        switch (type)
        {
            case PACKET_C2S_BLOCK_ENTITY_REQUEST ->
            {
                // Read Packet Buffer
                try
                {
                    input.readVarInt(); // todo: old code compat
                    return ServuxTweaksPacket.BlockEntityRequest(input.readBlockPos());
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading Block Entity Request from packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_C2S_ENTITY_REQUEST ->
            {
                // Read Packet Buffer
                try
                {
                    input.readVarInt(); // todo: old code compat
                    return ServuxTweaksPacket.EntityRequest(input.readVarInt());
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading Entity Request from packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE ->
            {
                try
                {
                    return ServuxTweaksPacket.SimpleBlockResponse(input.readBlockPos(), (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap()));
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading Block Entity Response from packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE ->
            {
                try
                {
                    return ServuxTweaksPacket.SimpleEntityResponse(input.readVarInt(), (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap()));
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading Entity Response from packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_S2C_NBT_RESPONSE_DATA ->
            {
                // Read Packet Buffer Slice
                try
                {
                    return ServuxTweaksPacket.ResponseS2CData(new FriendlyByteBuf(input.readBytes(input.readableBytes())));
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading S2C Bulk Response Buffer from packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_C2S_NBT_RESPONSE_DATA ->
            {
                // Read Packet Buffer Slice
                try
                {
                    return ServuxTweaksPacket.ResponseC2SData(new FriendlyByteBuf(input.readBytes(input.readableBytes())));
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading C2S Bulk Response Buffer from packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_C2S_METADATA_REQUEST ->
            {
                // Read Nbt
                try
                {
                    return ServuxTweaksPacket.MetadataRequest(input.readNbt());
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading Metadata Request from packet: [{}]", e.getLocalizedMessage());
                }
            }
            case PACKET_S2C_METADATA ->
            {
                // Read Nbt
                try
                {
                    return ServuxTweaksPacket.MetadataResponse(input.readNbt());
                }
                catch (Exception e)
                {
                    Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading Metadata Response from packet: [{}]", e.getLocalizedMessage());
                }
            }
            default -> Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: Unknown packet type!");
        }

        return null;
    }

    @Override
    public void clear()
    {
        if (this.nbt != null && !this.nbt.isEmpty())
        {
            this.nbt = new CompoundTag();
        }
        this.clearPacket();
        this.transactionId = -1;
        this.entityId = -1;
        this.pos = BlockPos.ZERO;
        this.packetType = null;
    }

    @Nullable
    public static Type getType(int input)
    {
        for (Type type : Type.values())
        {
            if (type.get() == input)
            {
                return type;
            }
        }

        return null;
    }

    public enum Type
    {
        PACKET_S2C_METADATA(1),
        PACKET_C2S_METADATA_REQUEST(2),
        PACKET_C2S_BLOCK_ENTITY_REQUEST(3),
        PACKET_C2S_ENTITY_REQUEST(4),
        PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE(5),
        PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE(6),
        // For Packet Splitter (Oversize Packets, S2C)
        PACKET_S2C_NBT_RESPONSE_START(10),
        PACKET_S2C_NBT_RESPONSE_DATA(11),
        // For Packet Splitter (Oversize Packets, C2S)
        PACKET_C2S_NBT_RESPONSE_START(12),
        PACKET_C2S_NBT_RESPONSE_DATA(13);

        private final int type;

        Type(int type)
        {
            this.type = type;
        }

        int get() { return this.type; }
    }

    public record Payload(ServuxTweaksPacket data) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Payload> ID = new CustomPacketPayload.Type<>(ServuxTweaksHandler.CHANNEL_ID);
        public static final StreamCodec<FriendlyByteBuf, Payload> CODEC = CustomPacketPayload.codec(Payload::write, Payload::new);

        public Payload(FriendlyByteBuf input)
        {
            this(fromPacket(input));
        }

        private void write(FriendlyByteBuf output)
        {
            data.toPacket(output);
        }

        @Override
        public @Nonnull CustomPacketPayload.Type<? extends CustomPacketPayload> type()
        {
            return ID;
        }
    }
}
