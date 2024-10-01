package fi.dy.masa.tweakeroo.event;

import javax.annotation.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.DataManager;
import fi.dy.masa.tweakeroo.data.ServerDataSyncer;

public class WorldLoadListener implements IWorldLoadListener
{
    @Override
    public void onWorldLoadPre(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc)
    {
        // Always disable the Free Camera mode when leaving the world or switching dimensions
        FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(false);

        if (worldAfter != null)
        {
            ServerDataSyncer.getInstance().onWorldPre();
        }
    }

    @Override
    public void onWorldLoadPost(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc)
    {
        DataManager.getInstance().reset(worldAfter == null);
        ServerDataSyncer.getInstance().reset(worldAfter == null);

        if (worldBefore == null)
        {
            if (FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue())
            {
                FeatureToggle.TWEAK_GAMMA_OVERRIDE.setBooleanValue(false);
                FeatureToggle.TWEAK_GAMMA_OVERRIDE.setBooleanValue(true);
            }
        }

        // Logging in to a world or changing dimensions or respawning
        if (worldAfter != null)
        {
            ServerDataSyncer.getInstance().onWorldJoin();
        }
    }
}