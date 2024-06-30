package fi.dy.masa.tweakeroo.data;

import net.minecraft.util.Identifier;

public class DataManager
{
    private static final DataManager INSTANCE = new DataManager();
    private boolean hasCarpetServer;
    private boolean hasServuxServer;
    public static final Identifier CARPET_HELLO = Identifier.of("carpet", "hello");
    public static final Identifier SERVUX_ENTITY_DATA = Identifier.of("servux", "entity_data");

    private DataManager()
    {
    }

    public static DataManager getInstance() { return INSTANCE; }

    public void reset(boolean isLogout)
    {
        if (isLogout)
        {
            //Tweakeroo.logger.info("DataManager#reset() - log-out");
            this.hasCarpetServer = false;
            this.hasServuxServer = false;
        }
        //else
        //{
            //Tweakeroo.logger.info("DataManager#reset() - dimension change or log-in");
        //}
    }

    public void setHasCarpetServer(boolean toggle)
    {
        this.hasCarpetServer = toggle;
    }

    public boolean hasCarpetServer()
    {
        return this.hasCarpetServer;
    }

    public void setHasServuxServer(boolean toggle)
    {
        this.hasServuxServer = toggle;
    }

    public boolean hasServuxServer()
    {
        return this.hasServuxServer;
    }
}
