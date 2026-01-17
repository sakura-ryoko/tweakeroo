package fi.dy.masa.tweakeroo.event;

import fi.dy.masa.tweakeroo.util.MiscUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;

// there is no Malilib interface for this so I have to just use it the way I did.
public class LoginDisconnectListener implements ClientLoginConnectionEvents.Disconnect {
    @Override
    public void onLoginDisconnect(ClientHandshakePacketListenerImpl clientHandshakePacketListener, Minecraft mc) {
        MiscUtils.setTickRate(MiscUtils.DEFAULT_TICK_RATE);
    }
}
