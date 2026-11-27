package fi.dy.masa.tweakeroo.network;

import org.jspecify.annotations.NonNull;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import fi.dy.masa.malilib.network.IClientPayloadData;
import fi.dy.masa.malilib.network.IPluginClientPlayHandler;
import fi.dy.masa.malilib.network.status.NetworkState;
import fi.dy.masa.malilib.network.status.NetworkStateTracker;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.data.EntityDataManager;

@Environment(EnvType.CLIENT)
public abstract class ServuxTweaksHandler<T extends CustomPacketPayload> implements IPluginClientPlayHandler<T>
{
	private static final ServuxTweaksHandler<ServuxTweaksPacket.Payload> INSTANCE = new ServuxTweaksHandler<>()
	{
		@Override
		public void receive(ServuxTweaksPacket.@NonNull Payload payload, ClientPlayNetworking.@NonNull Context context)
		{
			ServuxTweaksHandler.INSTANCE.receivePlayPayload(payload, context);
		}
	};

	public static ServuxTweaksHandler<ServuxTweaksPacket.Payload> getInstance() {return INSTANCE;}

	public static final Identifier CHANNEL_ID = Identifier.fromNamespaceAndPath("servux", "tweaks");
	private final NetworkStateTracker tracker = new NetworkStateTracker("tweaks_data");

	private boolean servuxRegistered;
	private boolean payloadRegistered = false;

	@Override
	public Identifier getPayloadChannel() {return CHANNEL_ID;}

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
		if (!channel.equals(CHANNEL_ID))
		{
			return;
		}
		if (!EntityDataManager.getInstance().isEnabled() || !this.checkFailures())
		{
			return;
		}

		if (data instanceof ServuxTweaksPacket packet)
		{
			switch (packet.getType())
			{
				case PACKET_S2C_METADATA ->
				{
					if (EntityDataManager.getInstance().receiveServuxMetadata(packet.getCompound()))
					{
						this.servuxRegistered = true;
					}
				}
				case PACKET_S2C_BLOCK_NBT_RESPONSE_SIMPLE ->
				{
					if (this.servuxRegistered)
					{
						EntityDataManager.getInstance().handleBlockEntityData(packet.getPos(), packet.getCompound());
					}
				}
				case PACKET_S2C_ENTITY_NBT_RESPONSE_SIMPLE ->
				{
					if (this.servuxRegistered)
					{
						EntityDataManager.getInstance().handleEntityData(packet.getEntityId(), packet.getCompound());
					}
				}
				default ->
						Tweakeroo.LOGGER.warn("ServuxTweaksHandler#decodeClientData(): received unhandled packetType {} of size {} bytes.", packet.getPacketType(), packet.getTotalSize());
			}
		}
	}

	@Override
	public void reset(Identifier channel)
	{
		if (channel.equals(CHANNEL_ID))
		{
			this.servuxRegistered = false;
			this.tracker().reset();
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
		if (!EntityDataManager.getInstance().isEnabled() || !this.checkFailures())
		{
			return;
		}

		if (data instanceof ServuxTweaksPacket packet)
		{
			if (!ServuxTweaksHandler.INSTANCE.sendPlayPayload(new ServuxTweaksPacket.Payload(packet)))
			{
				this.tickFailures();
			}
		}
	}

	@Override
	public boolean checkFailures()
	{
		return !(this.tracker().failureCount() > this.maxFailures());
	}

	@Override
	public NetworkStateTracker tracker()
	{
		return this.tracker;
	}

	@Override
	public void tickFailures()
	{
		int count = this.tracker().failureCount();

		if (count > this.maxFailures())
		{
			Tweakeroo.debugLog("ServuxTweaksHandler#tickFailures(): encountered [{}] sendPayload failures, cancelling any Servux join attempt(s)", this.maxFailures());
			this.servuxRegistered = false;
			this.tracker().lock();
			ServuxTweaksHandler.INSTANCE.unregisterPlayReceiver();
			EntityDataManager.getInstance().onPacketFailure();
		}
		else
		{
			this.tracker().tickState(NetworkState.GENERAL_ERROR);
		}
	}
}
