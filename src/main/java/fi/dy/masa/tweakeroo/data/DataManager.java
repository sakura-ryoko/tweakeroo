package fi.dy.masa.tweakeroo.data;

import javax.annotation.Nullable;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.interfaces.IServerListener;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class DataManager implements IServerListener
{
    private static final DataManager INSTANCE = new DataManager();
    public static DataManager getInstance() { return INSTANCE; }

    private boolean hasCarpetServer;
    private boolean hasServuxServer;
    public static final Identifier CARPET_HELLO = Identifier.fromNamespaceAndPath("carpet", "hello");
    public static final Identifier SERVUX_LITEMATIC_DATA = Identifier.fromNamespaceAndPath("servux", "litematics");
    //private IntegratedServer integratedServer;
    //private DynamicRegistryManager.Immutable registryManager = DynamicRegistryManager.EMPTY;
    private boolean hasIntegratedServer;

    private DataManager() { }

    public void reset(boolean isLogout)
    {
        if (isLogout)
        {
            Tweakeroo.debugLog("DataManager#reset() - log-out");
            this.hasCarpetServer = false;
            this.hasServuxServer = false;
            //this.registryManager = DynamicRegistryManager.EMPTY;
            this.setHasIntegratedServer(false, null);
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

    @Override
    public void onServerIntegratedSetup(IntegratedServer server)
    {
        this.setHasIntegratedServer(true, server);
    }

    public boolean hasIntegratedServer() { return this.hasIntegratedServer; }

    public void setHasIntegratedServer(boolean toggle, @Nullable IntegratedServer server)
    {
        this.hasIntegratedServer = toggle;
        //this.integratedServer = server;
    }

    /*
    public IntegratedServer getIntegratedServer()
    {
        return this.integratedServer;
    }
     */

    /*
    public void setRegistryManager(DynamicRegistryManager.Immutable immutable)
    {
        this.registryManager = immutable;
    }

    public DynamicRegistryManager.Immutable getRegistryManager()
    {
        return this.registryManager;
    }
     */

	public JsonElement toJson()
	{
		return new JsonObject();
	}

	public void fromJson(JsonElement ele)
	{
	}
}
