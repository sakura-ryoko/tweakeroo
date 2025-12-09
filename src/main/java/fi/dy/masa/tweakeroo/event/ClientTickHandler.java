package fi.dy.masa.tweakeroo.event;

import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import net.minecraft.client.Minecraft;

public class ClientTickHandler implements IClientTickHandler
{
    @Override
    public void onClientTick(Minecraft mc)
    {
        if (mc.level != null && mc.player != null)
        {
            MiscTweaks.onTick(mc);
            RenderTweaks.onTick();
        }
    }
}
