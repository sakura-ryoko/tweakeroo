package fi.dy.masa.tweakeroo.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import fi.dy.masa.malilib.util.EntityUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.CameraPresetManager;

public class CameraUtils
{
    private static float cameraYaw;
    private static float cameraPitch;
    private static boolean freeCameraSpectator;

    public static void setFreeCameraSpectator(boolean isSpectator)
    {
        freeCameraSpectator = isSpectator;
    }

    public static boolean getFreeCameraSpectator()
    {
        return freeCameraSpectator;
    }

    public static boolean shouldPreventPlayerInputs()
    {
        return FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
               Configs.Generic.FREE_CAMERA_PLAYER_INPUTS.getBooleanValue() == false;
    }

    public static boolean shouldPreventPlayerMovement()
    {
        return FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
               Configs.Generic.FREE_CAMERA_PLAYER_MOVEMENT.getBooleanValue() == false;
    }

    public static float getCameraYaw()
    {
        return net.minecraft.util.Mth.wrapDegrees(cameraYaw);
    }

    public static float getCameraPitch()
    {
        return net.minecraft.util.Mth.wrapDegrees(cameraPitch);
    }

    public static void setCameraYaw(float yaw)
    {
        cameraYaw = yaw;
    }

    public static void setCameraPitch(float pitch)
    {
        cameraPitch = pitch;
    }

    public static void setCameraRotations(float yaw, float pitch)
    {
        CameraEntity camera = CameraEntity.getCamera();

        if (camera != null)
        {
            camera.setCameraRotations(yaw, pitch);
        }
    }

    public static void updateCameraRotations(float yawChange, float pitchChange)
    {
        CameraEntity camera = CameraEntity.getCamera();

        if (camera != null)
        {
            camera.updateCameraRotations(yawChange, pitchChange);
        }
    }

    public static void markChunksForRebuild(int chunkX, int chunkZ, int lastChunkX, int lastChunkZ)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || (chunkX == lastChunkX && chunkZ == lastChunkZ))
        {
            return;
        }

        final int viewDistance = mc.options.renderDistance().get();

        if (chunkX != lastChunkX)
        {
            final int minCX = chunkX > lastChunkX ? lastChunkX + viewDistance : chunkX     - viewDistance;
            final int maxCX = chunkX > lastChunkX ? chunkX     + viewDistance : lastChunkX - viewDistance;

            for (int cx = minCX; cx <= maxCX; ++cx)
            {
                for (int cz = chunkZ - viewDistance; cz <= chunkZ + viewDistance; ++cz)
                {
                    if (isClientChunkLoaded(mc.level, cx, cz))
                    {
                        markChunkForReRender(mc.levelRenderer, cx, cz);
                    }
                }
            }
        }

        if (chunkZ != lastChunkZ)
        {
            final int minCZ = chunkZ > lastChunkZ ? lastChunkZ + viewDistance : chunkZ     - viewDistance;
            final int maxCZ = chunkZ > lastChunkZ ? chunkZ     + viewDistance : lastChunkZ - viewDistance;

            for (int cz = minCZ; cz <= maxCZ; ++cz)
            {
                for (int cx = chunkX - viewDistance; cx <= chunkX + viewDistance; ++cx)
                {
                    if (isClientChunkLoaded(mc.level, cx, cz))
                    {
                        markChunkForReRender(mc.levelRenderer, cx, cz);
                    }
                }
            }
        }
    }

    public static void markChunksForRebuildOnDeactivation(int lastChunkX, int lastChunkZ)
    {
        Minecraft mc = Minecraft.getInstance();
        final int viewDistance = mc.options.renderDistance().get();
        Entity entity = EntityUtils.getCameraEntity();

        if (mc.level == null || entity == null)
        {
            return;
        }

        final int chunkX = Mth.floor(entity.getX() / 16.0) >> 4;
        final int chunkZ = Mth.floor(entity.getZ() / 16.0) >> 4;

        final int minCameraCX = lastChunkX - viewDistance;
        final int maxCameraCX = lastChunkX + viewDistance;
        final int minCameraCZ = lastChunkZ - viewDistance;
        final int maxCameraCZ = lastChunkZ + viewDistance;
        final int minCX = chunkX - viewDistance;
        final int maxCX = chunkX + viewDistance;
        final int minCZ = chunkZ - viewDistance;
        final int maxCZ = chunkZ + viewDistance;

        for (int cz = minCZ; cz <= maxCZ; ++cz)
        {
            for (int cx = minCX; cx <= maxCX; ++cx)
            {
                // Mark all chunks that were not in free camera range
                if ((cx < minCameraCX || cx > maxCameraCX || cz < minCameraCZ || cz > maxCameraCZ) &&
                    isClientChunkLoaded(mc.level, cx, cz))
                {
                    markChunkForReRender(mc.levelRenderer, cx, cz);
                }
            }
        }
    }

    public static void markChunkForReRender(LevelRenderer renderer, int chunkX, int chunkZ)
    {
        for (int cy = 0; cy < 16; ++cy)
        {
            renderer.setSectionDirty(chunkX, cy, chunkZ);
        }
    }

    public static boolean isClientChunkLoaded(ClientLevel world, int chunkX, int chunkZ)
    {
        return world.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) != null;
    }

	public static String fixPresetName(String in)
	{
		return in.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "");
	}

	public static boolean addPreset(@Nonnull CameraPreset preset)
	{
		if (!CameraPresetManager.getInstance().hasPosition(preset))
		{
			CameraPresetManager.getInstance().add(preset);
			Tweakeroo.debugLog("CameraUtils#addPreset(): Added new preset: {}", preset.toShortString());
			return true;
		}

		return false;
	}

	public static boolean updatePreset(@Nonnull CameraPreset preset)
	{
		CameraPresetManager.getInstance().update(preset, false);
		Tweakeroo.debugLog("CameraUtils#updatePreset(): Updated preset: {}", preset.toShortString());
		return true;
	}

	public static boolean deletePreset(@Nullable CameraPreset oldPreset)
	{
		if (oldPreset != null)
		{
			CameraPresetManager.getInstance().remove(oldPreset.getId(), false);
			Tweakeroo.debugLog("CameraUtils#deletePreset(): Deleted preset: {}", oldPreset.toShortString());
			return true;
		}

		return false;
	}

	public static boolean deletePresetAtPosition(Minecraft mc)
	{
		if (mc.getCameraEntity() != null)
		{
			CameraPreset preset = CameraPresetManager.getInstance().getAtPosition(mc.getCameraEntity());

			if (preset != null)
			{
				CameraPresetManager.getInstance().remove(preset.getId(), false);
				Tweakeroo.debugLog("CameraUtils#deletePresetAtPosition(): Deleted preset: {}", preset.toShortString());
				return true;
			}
		}

		return false;
	}

	public static boolean deleteAllPresets(ResourceKey<Level> dimKey)
	{
		if (dimKey != null)
		{
			CameraPresetManager.getInstance().clear(dimKey, false);
			Tweakeroo.debugLog("CameraUtils#deletePresetAtPosition(): Deleted all presets for dimension '{}'", dimKey.identifier().toString());
			return true;
		}

		return false;
	}

	public static boolean renamePreset(@Nullable CameraPreset preset, final String newName)
	{
		if (preset != null)
		{
			String oldName = preset.getName();
			preset.setName(CameraUtils.fixPresetName(newName));
			CameraPresetManager.getInstance().update(preset, false);
			Tweakeroo.debugLog("CameraUtils#renamePreset(): Renamed preset: [{}] / '{}' -> '{}'", preset.getId(), oldName, preset.getName());
			return true;
		}

		return false;
	}

	public static boolean recallPreset(@Nonnull CameraPreset preset, Minecraft mc)
	{
		if (!preset.equals(mc.getCameraEntity()) && mc.level != null)
		{
			if (mc.level.dimension().identifier().equals(preset.getDim()))
			{
				CameraPresetManager.getInstance().setLastPreset(preset.getId());

				if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
				{
					CameraEntity.updatePositionAtPreset(preset);
				}
				else
				{
					FeatureToggle.TWEAK_FREE_CAMERA.setEnabledNoCallback();
					CameraEntity.setCameraState(true, preset);
				}

				Tweakeroo.debugLog("CameraUtils#recallPreset(): Recall preset: {}", preset.toShortString());
				return true;
			}
		}

		return false;
	}

	public static boolean cyclePreset(Minecraft mc)
	{
		if (mc.level != null)
		{
			ResourceKey<Level> dimKey = mc.level.dimension();
			CameraPreset preset = CameraPresetManager.getInstance().cycle(dimKey);

			if (preset != null)
			{
				return recallPreset(preset, mc);
			}
		}

		return false;
	}
}
