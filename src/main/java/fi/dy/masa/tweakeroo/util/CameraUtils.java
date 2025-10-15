package fi.dy.masa.tweakeroo.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;

import fi.dy.masa.malilib.util.EntityUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.CameraPresetCache;

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
        return net.minecraft.util.math.MathHelper.wrapDegrees(cameraYaw);
    }

    public static float getCameraPitch()
    {
        return net.minecraft.util.math.MathHelper.wrapDegrees(cameraPitch);
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
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world == null || (chunkX == lastChunkX && chunkZ == lastChunkZ))
        {
            return;
        }

        final int viewDistance = mc.options.getViewDistance().getValue();

        if (chunkX != lastChunkX)
        {
            final int minCX = chunkX > lastChunkX ? lastChunkX + viewDistance : chunkX     - viewDistance;
            final int maxCX = chunkX > lastChunkX ? chunkX     + viewDistance : lastChunkX - viewDistance;

            for (int cx = minCX; cx <= maxCX; ++cx)
            {
                for (int cz = chunkZ - viewDistance; cz <= chunkZ + viewDistance; ++cz)
                {
                    if (isClientChunkLoaded(mc.world, cx, cz))
                    {
                        markChunkForReRender(mc.worldRenderer, cx, cz);
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
                    if (isClientChunkLoaded(mc.world, cx, cz))
                    {
                        markChunkForReRender(mc.worldRenderer, cx, cz);
                    }
                }
            }
        }
    }

    public static void markChunksForRebuildOnDeactivation(int lastChunkX, int lastChunkZ)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        final int viewDistance = mc.options.getViewDistance().getValue();
        Entity entity = EntityUtils.getCameraEntity();

        if (mc.world == null || entity == null)
        {
            return;
        }

        final int chunkX = MathHelper.floor(entity.getX() / 16.0) >> 4;
        final int chunkZ = MathHelper.floor(entity.getZ() / 16.0) >> 4;

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
                    isClientChunkLoaded(mc.world, cx, cz))
                {
                    markChunkForReRender(mc.worldRenderer, cx, cz);
                }
            }
        }
    }

    public static void markChunkForReRender(WorldRenderer renderer, int chunkX, int chunkZ)
    {
        for (int cy = 0; cy < 16; ++cy)
        {
            renderer.scheduleChunkRender(chunkX, cy, chunkZ);
        }
    }

    public static boolean isClientChunkLoaded(ClientWorld world, int chunkX, int chunkZ)
    {
        return world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) != null;
    }

	public static String fixPresetName(String in)
	{
		return in.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", "");
	}

	public static boolean addPreset(@Nonnull CameraPreset preset)
	{
		if (!CameraPresetCache.getInstance().hasPosition(preset))
		{
			CameraPresetCache.getInstance().add(preset);
			Tweakeroo.debugLog("CameraUtils#addPreset(): Added new preset: {}", preset.toShortString());
			return true;
		}

		return false;
	}

	public static boolean updatePreset(@Nonnull CameraPreset preset)
	{
		CameraPresetCache.getInstance().update(preset, false);
		Tweakeroo.debugLog("CameraUtils#updatePreset(): Updated preset: {}", preset.toShortString());
		return true;
	}

	public static boolean deletePreset(@Nullable CameraPreset oldPreset)
	{
		if (oldPreset != null)
		{
			CameraPresetCache.getInstance().remove(oldPreset.id(), false);
			Tweakeroo.debugLog("CameraUtils#deletePreset(): Deleted preset: {}", oldPreset.toShortString());
			return true;
		}

		return false;
	}

	public static boolean deletePresetAtPosition(MinecraftClient mc)
	{
		if (mc.getCameraEntity() != null)
		{
			CameraPreset preset = CameraPresetCache.getInstance().getAtPosition(mc.getCameraEntity());

			if (preset != null)
			{
				CameraPresetCache.getInstance().remove(preset.id(), false);
				Tweakeroo.debugLog("CameraUtils#deletePresetAtPosition(): Deleted preset: {}", preset.toShortString());
				return true;
			}
		}

		return false;
	}

	public static boolean deleteAllPresets(RegistryKey<World> dimKey)
	{
		if (dimKey != null)
		{
			CameraPresetCache.getInstance().clear(dimKey, false);
			Tweakeroo.debugLog("CameraUtils#deletePresetAtPosition(): Deleted all presets for dimension '{}'", dimKey.getValue().toString());
			return true;
		}

		return false;
	}

	public static boolean renamePreset(@Nullable CameraPreset oldPreset, final String newName)
	{
		if (oldPreset != null)
		{
			CameraPreset newPreset = new CameraPreset(oldPreset.id(), CameraUtils.fixPresetName(newName), oldPreset.dim(), oldPreset.pos(), oldPreset.yaw(), oldPreset.pitch());
			CameraPresetCache.getInstance().update(newPreset, false);
			Tweakeroo.debugLog("CameraUtils#renamePreset(): Renamed preset: [{}] / '{}' -> '{}'", oldPreset.id(), oldPreset.name(), newPreset.name());
			return true;
		}

		return false;
	}

	public static boolean recallPreset(@Nonnull CameraPreset preset, MinecraftClient mc)
	{
		if (!preset.equals(mc.getCameraEntity()))
		{
			CameraPresetCache.getInstance().setLastPreset(preset.id());

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

		return false;
	}

	public static boolean cyclePreset(MinecraftClient mc)
	{
		if (mc.world != null)
		{
			RegistryKey<World> dimKey = mc.world.getRegistryKey();
			CameraPreset preset = CameraPresetCache.getInstance().cycle(dimKey);

			if (preset != null)
			{
				return recallPreset(preset, mc);
			}
		}

		return false;
	}
}
