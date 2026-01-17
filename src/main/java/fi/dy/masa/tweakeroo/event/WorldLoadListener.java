package fi.dy.masa.tweakeroo.event;

import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;

import fi.dy.masa.malilib.interfaces.IWorldLoadListener;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.CachedTagManager;
import fi.dy.masa.tweakeroo.data.CameraPresetManager;
import fi.dy.masa.tweakeroo.data.DataManager;
import fi.dy.masa.tweakeroo.data.EntityDataManager;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import fi.dy.masa.tweakeroo.util.MiscUtils;

public class WorldLoadListener implements IWorldLoadListener
{
	@Override
    public void onWorldLoadImmutable(RegistryAccess.Frozen immutable)
    {
        RenderTweaks.setDynamicRegistryManager(immutable);
    }

    @Override
    public void onWorldLoadPre(@Nullable ClientLevel worldBefore, @Nullable ClientLevel worldAfter, Minecraft mc)
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
            EntityDataManager.getInstance().onWorldPre();
        }
    }

    @Override
    public void onWorldLoadPost(@Nullable ClientLevel worldBefore, @Nullable ClientLevel worldAfter, Minecraft mc)
    {
        DataManager.getInstance().reset(worldAfter == null);
        EntityDataManager.getInstance().reset(worldAfter == null);

        if (worldBefore == null)
        {
	        MiscTweaks.setKeybindStates();

            if (FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue())
            {
                FeatureToggle.TWEAK_GAMMA_OVERRIDE.setBooleanValue(false);
	            MiscUtils.toggleGammaOverrideWithMessage();
            }

            // Prevents option value de-sync
            if (FeatureToggle.TWEAK_DARKNESS_VISIBILITY.getBooleanValue() &&
                mc.options.darknessEffectScale().get() != Configs.Generic.DARKNESS_SCALE_OVERRIDE_VALUE.getDoubleValue())
            {
                Configs.Internal.DARKNESS_SCALE_VALUE_ORIGINAL.setDoubleValue(mc.options.darknessEffectScale().get());
                mc.options.darknessEffectScale().set(Configs.Generic.DARKNESS_SCALE_OVERRIDE_VALUE.getDoubleValue());
            }
        }

        // Logging in to a world or changing dimensions or respawning
        if (worldAfter != null)
        {
			if (worldBefore == null)
			{
				this.readStoredDataGlobal();
			}

//	        this.readStoredDataPerDimension();
            EntityDataManager.getInstance().onWorldJoin();
			CachedTagManager.startCache();
			MiscUtils.setRealTickRate();
        }
        else
        {
            Configs.Internal.SHULKER_MAX_STACK_SIZE.resetToDefault();
			MiscUtils.setTickRate(MiscUtils.DEFAULT_TICK_RATE);
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

		if (Files.exists(file))
		{
			FileUtils.delete(file);
		}

		if (!CameraPresetManager.getInstance().isEmpty())
		{
			root.add("camera_presets", CameraPresetManager.getInstance().toJson());
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
				CameraPresetManager.getInstance().fromJson(JsonUtils.getNestedObject(root, "camera_presets", false));
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