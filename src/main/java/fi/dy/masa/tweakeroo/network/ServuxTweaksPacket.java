package fi.dy.masa.tweakeroo.network;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import fi.dy.masa.malilib.network.IClientPayloadData;
import fi.dy.masa.malilib.util.data.tag.BaseData;
import fi.dy.masa.malilib.util.data.tag.CompoundData;
import fi.dy.masa.malilib.util.data.tag.converter.DataConverterNbt;
import fi.dy.masa.malilib.util.data.tag.util.DataByteBufUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class ServuxTweaksPacket implements IClientPayloadData
{
	private Type packetType;
	private int entityId;
	private BlockPos pos;
	private CompoundData nbt;
	private FriendlyByteBuf buffer;
	public static final int PROTOCOL_VERSION = 2;

	private ServuxTweaksPacket(Type type)
	{
		this.packetType = type;
		this.entityId = -1;
		this.pos = BlockPos.ZERO;
		this.nbt = new CompoundData();
		this.clearPacket();
	}

	public static ServuxTweaksPacket MetadataRequest(@Nullable CompoundData nbt)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_C2S_METADATA_REQUEST);
		if (nbt != null)
		{
			packet.nbt.combine(nbt);
		}
		return packet;
	}

	public static ServuxTweaksPacket MetadataResponse(@Nullable CompoundData nbt)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_S2C_METADATA);
		if (nbt != null)
		{
			packet.nbt.combine(nbt);
		}
		return packet;
	}

	public static ServuxTweaksPacket UnregisterReply(@Nullable CompoundData nbt)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_C2S_UNREGISTER_REPLY);
		if (nbt != null)
		{
			packet.nbt.combine(nbt);
		}
		return packet;
	}

	// Entity simple response
	public static ServuxTweaksPacket SimpleEntityResponse(int entityId, @Nullable CompoundData nbt)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE);
		if (nbt != null)
		{
			packet.nbt.combine(nbt);
		}
		packet.entityId = entityId;
		return packet;
	}

	public static ServuxTweaksPacket SimpleBlockResponse(BlockPos pos, @Nullable CompoundData nbt)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE);
		if (nbt != null)
		{
			packet.nbt.combine(nbt);
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
	public static ServuxTweaksPacket ResponseS2CStart(@Nonnull CompoundData nbt)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_S2C_NBT_RESPONSE_START);
		packet.nbt.combine(nbt);
		return packet;
	}

	public static ServuxTweaksPacket ResponseS2CData(@Nonnull FriendlyByteBuf buffer)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_S2C_NBT_RESPONSE_DATA);
		packet.buffer = new FriendlyByteBuf(buffer.copy());
		packet.nbt = new CompoundData();
		return packet;
	}

	public static ServuxTweaksPacket ResponseC2SStart(@Nonnull CompoundData nbt)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_C2S_NBT_RESPONSE_START);
		packet.nbt.combine(nbt);
		return packet;
	}

	public static ServuxTweaksPacket ResponseC2SData(@Nonnull FriendlyByteBuf buffer)
	{
		var packet = new ServuxTweaksPacket(Type.PACKET_C2S_NBT_RESPONSE_DATA);
		packet.buffer = new FriendlyByteBuf(buffer.copy());
		packet.nbt = new CompoundData();
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

	public int getEntityId() {return this.entityId;}

	public BlockPos getPos() {return this.pos;}

	public CompoundData getCompound()
	{
		return this.nbt;
	}

	@Deprecated
	private static CompoundData fromVanilla(CompoundTag nbt)
	{
		if (nbt != null && !nbt.isEmpty())
		{
			return DataConverterNbt.fromVanillaCompound(nbt);
		}

		return new CompoundData();
	}

	@Deprecated
	private CompoundTag toVanilla()
	{
		if (this.nbt != null && !this.nbt.isEmpty())
		{
			return DataConverterNbt.toVanillaCompound(this.nbt);
		}

		return new CompoundTag();
	}

	public FriendlyByteBuf getBuffer()
	{
		return this.buffer;
	}

	public boolean hasBuffer() {return this.buffer != null && this.buffer.isReadable();}

	public boolean hasNbt() {return this.nbt != null && !this.nbt.isEmpty();}

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
//                    output.writeNbt(this.nbt);
					DataByteBufUtils.toByteBuf(output, this.nbt, "");
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
//                    output.writeNbt(this.nbt);
					DataByteBufUtils.toByteBuf(output, this.nbt, "");
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
					output.writeNbt(this.toVanilla());
//					DataByteBufUtils.toByteBuf(output, this.nbt, "");
				}
				catch (Exception e)
				{
					Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: error writing NBT to packet: [{}]", e.getLocalizedMessage());
				}
			}
			case PACKET_C2S_UNREGISTER_REPLY ->
			{
				// Write NBT
				try
				{
//                    output.writeNbt(this.nbt);
					DataByteBufUtils.toByteBuf(output, this.nbt, "");
				}
				catch (Exception e)
				{
					Tweakeroo.LOGGER.error("ServuxTweaksPacket#toPacket: error writing Data to packet: [{}]", e.getLocalizedMessage());
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
					BlockPos pos = input.readBlockPos();
					Optional<BaseData> opt = DataByteBufUtils.fromByteBuf(input);
//                    return ServuxTweaksPacket.SimpleBlockResponse(input.readBlockPos(), (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap()));
					if (opt.isPresent())
					{
						return ServuxTweaksPacket.SimpleBlockResponse(pos, (CompoundData) opt.get());
					}
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
					int entityId = input.readVarInt();
					Optional<BaseData> opt = DataByteBufUtils.fromByteBuf(input);
//                    return ServuxTweaksPacket.SimpleEntityResponse(input.readVarInt(), (CompoundTag) input.readNbt(NbtAccounter.unlimitedHeap()));
					if (opt.isPresent())
					{
						return ServuxTweaksPacket.SimpleEntityResponse(entityId, (CompoundData) opt.get());
					}
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
//					Optional<BaseData> opt = DataByteBufUtils.fromByteBuf(input);
					return ServuxTweaksPacket.MetadataRequest(fromVanilla(input.readNbt()));
//					if (opt.isPresent())
//					{
//						return ServuxTweaksPacket.MetadataRequest((CompoundData) opt.get());
//					}
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
//					Optional<BaseData> opt = DataByteBufUtils.fromByteBuf(input);
					return ServuxTweaksPacket.MetadataResponse(fromVanilla(input.readNbt()));
//					if (opt.isPresent())
//					{
//						return ServuxTweaksPacket.MetadataResponse((CompoundData) opt.get());
//					}
				}
				catch (Exception e)
				{
					Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading Metadata Response from packet: [{}]", e.getLocalizedMessage());
				}
			}
			case PACKET_C2S_UNREGISTER_REPLY ->
			{
				// Read Nbt
				try
				{
					Optional<BaseData> opt = DataByteBufUtils.fromByteBuf(input);
//                    return ServuxTweaksPacket.UnregisterReply(input.readNbt());
					if (opt.isPresent())
					{
						return ServuxTweaksPacket.UnregisterReply((CompoundData) opt.get());
					}
				}
				catch (Exception e)
				{
					Tweakeroo.LOGGER.error("ServuxTweaksPacket#fromPacket: error reading Unregister Reply from packet: [{}]", e.getLocalizedMessage());
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
			this.nbt = new CompoundData();
		}
		this.clearPacket();
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
		PACKET_C2S_UNREGISTER_REPLY(7),
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

		int get() {return this.type;}
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
