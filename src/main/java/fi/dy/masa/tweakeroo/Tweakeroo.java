package fi.dy.masa.tweakeroo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.util.log.AnsiLogger;
import fi.dy.masa.tweakeroo.config.Configs;

public class Tweakeroo implements ModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    private static final AnsiLogger ANSI_LOGGER = new AnsiLogger(Tweakeroo.class, true, true);

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

            // Is this for the "Test" module??
            if (Boolean.getBoolean("tweakeroo.debug.stdout"))
            {
                ANSI_LOGGER.info("[TweakerooDebug] {} | args={}", msg, args);
            }
        }
    }
}
