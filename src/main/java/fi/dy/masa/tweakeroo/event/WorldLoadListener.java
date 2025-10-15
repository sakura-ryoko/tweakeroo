package fi.dy.masa.tweakeroo.event;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;

import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.CachedTagManager;
import fi.dy.masa.tweakeroo.data.CameraPresetCache;
import fi.dy.masa.tweakeroo.data.DataManager;
import fi.dy.masa.tweakeroo.data.ServerDataSyncer;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import fi.dy.masa.tweakeroo.util.InventoryUtils;

public class WorldLoadListener implements IWorldLoadListener
{
    @Override
    public void onWorldLoadImmutable(DynamicRegistryManager.Immutable immutable)
    {
        RenderTweaks.setDynamicRegistryManager(immutable);
    }

    @Override
    public void onWorldLoadPre(@Nullable ClientWorld worldBefore, @Nullable ClientWorld worldAfter, MinecraftClient mc)
    {
        // Always disable the Free Camera mode when leaving the world or switching dimensions
        FeatureToggle.TWEAK_FREE_CAMERA.setBooleanValue(false);

		if (worldBefore != null)
		{
//			this.writeDataPerDimension();

			// Quitting to main menu
			if (worldAfter == null)
			{
				this.writeDataGlobal();
			}
		}

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

            // Prevents option value de-sync
            if (FeatureToggle.TWEAK_DARKNESS_VISIBILITY.getBooleanValue() &&
                mc.options.getDarknessEffectScale().getValue() != Configs.Generic.DARKNESS_SCALE_OVERRIDE_VALUE.getDoubleValue())
            {
                Configs.Internal.DARKNESS_SCALE_VALUE_ORIGINAL.setDoubleValue(mc.options.getDarknessEffectScale().getValue());
                mc.options.getDarknessEffectScale().setValue(Configs.Generic.DARKNESS_SCALE_OVERRIDE_VALUE.getDoubleValue());
            }

            InventoryUtils.clearCache();
        }

        // Logging in to a world or changing dimensions or respawning
        if (worldAfter != null)
        {
			if (worldBefore == null)
			{
				this.readStoredDataGlobal();
			}

//	        this.readStoredDataPerDimension();
            ServerDataSyncer.getInstance().onWorldJoin();
            InventoryUtils.startCache();
        }
        else
        {
            Configs.Internal.SHULKER_MAX_STACK_SIZE.resetToDefault();
        }
    }

//	private void writeDataPerDimension()
//	{
//		Path file = getCurrentStorageFile(false);
//		JsonObject root = new JsonObject();
//
//		root.add("camera_presets", CameraPresetCache.getInstance().toJson());
//
//		JsonUtils.writeJsonToFileAsPath(root, file);
//	}

	private void writeDataGlobal()
	{
		Path file = getCurrentStorageFile(true);
		JsonObject root = new JsonObject();
		boolean shouldSave = false;

		if (!CameraPresetCache.getInstance().isEmpty())
		{
			root.add("camera_presets", CameraPresetCache.getInstance().toJson());
			shouldSave = true;
		}

		if (shouldSave)
		{
			JsonUtils.writeJsonToFileAsPath(root, file);
		}
	}

//	private void readStoredDataPerDimension()
//	{
//		// Per-dimension file
//		Path file = getCurrentStorageFile(false);
//		JsonElement element = JsonUtils.parseJsonFileAsPath(file);
//
//		if (element != null && element.isJsonObject())
//		{
//			JsonObject root = element.getAsJsonObject();
//
//			if (JsonUtils.hasObject(root, "camera_presets"))
//			{
//				CameraPresetCache.getInstance().fromJson(JsonUtils.getNestedObject(root, "camera_presets", false));
//			}
//		}
//	}

	private void readStoredDataGlobal()
	{
		// Global file
		Path file = getCurrentStorageFile(true);
		JsonElement element = JsonUtils.parseJsonFileAsPath(file);

		if (element != null && element.isJsonObject())
		{
			JsonObject root = element.getAsJsonObject();

			if (JsonUtils.hasObject(root, "camera_presets"))
			{
				CameraPresetCache.getInstance().fromJson(JsonUtils.getNestedObject(root, "camera_presets", false));
			}
		}
	}

	public static Path getCurrentConfigDirectory()
	{
		return FileUtils.getConfigDirectoryAsPath().resolve(Reference.MOD_ID);
	}

	private static Path getCurrentStorageFile(boolean globalData)
	{
		Path saveDir = getCurrentConfigDirectory();

		if (!Files.exists(saveDir))
		{
			FileUtils.createDirectoriesIfMissing(saveDir);
			//Tweakeroo.debugLog("getCurrentStorageFile(): Creating directory '{}'.", saveDir.toAbsolutePath());
		}

		if (!Files.isDirectory(saveDir))
		{
			Tweakeroo.LOGGER.warn("getCurrentStorageFile(): Failed to create the config directory '{}'", saveDir.toAbsolutePath());
		}

		return saveDir.resolve(StringUtils.getStorageFileName(globalData, "", ".json", Reference.MOD_ID + "_default"));
	}
}
