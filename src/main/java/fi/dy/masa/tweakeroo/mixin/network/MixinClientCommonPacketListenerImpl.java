package fi.dy.masa.tweakeroo.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.data.DataManager;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

@Mixin(ClientCommonPacketListenerImpl.class)
public class MixinClientCommonPacketListenerImpl
{
    @Inject(method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V", at = @At("HEAD"))
    private void tweakeroo_onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci)
    {
        if (packet.payload().type().id().equals(DataManager.CARPET_HELLO))
        {
            DataManager.getInstance().setHasCarpetServer(true);
        }
        else if (packet.payload().type().id().getNamespace().equals("servux"))
        {
            DataManager.getInstance().setHasServuxServer(true);
        }
    }
}
