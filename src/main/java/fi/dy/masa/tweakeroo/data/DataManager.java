package fi.dy.masa.tweakeroo.data;

import net.minecraft.util.Identifier;

public class DataManager
{
    private static final DataManager INSTANCE = new DataManager();
    private boolean hasCarpetServer;
    public static final Identifier CARPET_HELLO = Identifier.of("carpet", "hello");

    private DataManager()
    {
    }

    public static DataManager getInstance() { return INSTANCE; }

    public void reset(boolean isLogout)
    {
        if (isLogout)
        {
            //Tweakeroo.logger.info("DataManager#reset() - log-out");
        }
        else
        {
            //Tweakeroo.logger.info("DataManager#reset() - dimension change or log-in");
        }
    }

    public void setHasCarpetServer(boolean toggle)
    {
        this.hasCarpetServer = toggle;
    }

    public boolean hasCarpetServer()
    {
        return this.hasCarpetServer;
    }
}
