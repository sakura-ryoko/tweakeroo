package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import fi.dy.masa.tweakeroo.data.DataManager;

@Mixin(ClientCommonNetworkHandler.class)
public class MixinClientCommonNetworkHandler
{
    @Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V", at = @At("HEAD"))
    private void tweakeroo_onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci)
    {
        if (packet.payload().getId().id().equals(DataManager.CARPET_HELLO))
        {
            DataManager.getInstance().setHasCarpetServer(true);
        }
        else if (packet.payload().getId().id().equals(DataManager.SERVUX_ENTITY_DATA))
        {
            DataManager.getInstance().setHasServuxServer(true);
        }
    }
}
