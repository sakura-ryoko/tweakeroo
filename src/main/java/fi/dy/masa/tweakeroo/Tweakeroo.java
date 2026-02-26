package fi.dy.masa.tweakeroo;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.tweakeroo.config.Configs;

import java.util.Arrays;

public class Tweakeroo implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    public static int renderCountItems;
    public static int renderCountXPOrbs;

    @Override
    public void onInitialize()
    {
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
    }

    public static void debugLog(String msg, Object... args)
    {
        if (Configs.Generic.DEBUG_LOGGING.getBooleanValue())
        {
            Tweakeroo.LOGGER.info(msg, args);

            if (Boolean.getBoolean("tweakeroo.debug.stdout"))
            {
                System.out.println("[TweakerooDebug] " + msg + " | args=" + Arrays.toString(args));
            }
        }
    }
}
