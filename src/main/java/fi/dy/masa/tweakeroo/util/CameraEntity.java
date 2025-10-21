package fi.dy.masa.tweakeroo.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

public class CameraEntity extends ClientPlayerEntity
{
    @Nullable private static CameraEntity camera;
    @Nullable private static Entity originalCameraEntity;
    private static Vec3d cameraMotion = new Vec3d(0.0, 0.0, 0.0);
    private static boolean cullChunksOriginal;
    private static boolean sprinting;
    private static boolean originalCameraWasPlayer;

    private CameraEntity(MinecraftClient mc, ClientWorld world,
                         ClientPlayNetworkHandler netHandler, StatHandler stats,
                         ClientRecipeBook recipeBook, PlayerInput input, boolean sprinting)
    {
        super(mc, world, netHandler, stats, recipeBook, input, sprinting);
    }

    @Override
    public boolean isSpectator()
    {
        return true;
    }

    /**
     * Apparently, some mods complain about us not returning an entityId
     * @return (id)
     */
    @Override
    public int getId()
    {
        if (originalCameraEntity != null)
        {
            return originalCameraEntity.getId();
        }

        return super.getId();
    }

    public static void movementTick()
    {
        CameraEntity camera = getCamera();

        if (camera != null && Configs.Generic.FREE_CAMERA_PLAYER_MOVEMENT.getBooleanValue() == false)
        {
            GameOptions options = MinecraftClient.getInstance().options;

            camera.updateLastTickPosition();

            if (options.sprintKey.isPressed())
            {
                sprinting = true;
            }
            else if (options.forwardKey.isPressed() == false && options.backKey.isPressed() == false)
            {
                sprinting = false;
            }

            cameraMotion = MiscUtils.calculatePlayerMotionWithDeceleration(cameraMotion, 0.15, 0.4);
            double forward = sprinting ? cameraMotion.x * 3 : cameraMotion.x;

            camera.handleMotion(forward, cameraMotion.y, cameraMotion.z);
        }
    }

    private static double getMoveSpeed()
    {
        double base = 0.07;

        if (FeatureToggle.TWEAK_FLY_SPEED.getBooleanValue())
        {
            base = Configs.getActiveFlySpeedConfig().getDoubleValue();
        }

        return base * 10;
    }

    private void handleMotion(double forward, double up, double strafe)
    {
        float yaw = this.getYaw();
        double scale = getMoveSpeed();
        double xFactor = Math.sin(yaw * Math.PI / 180.0);
        double zFactor = Math.cos(yaw * Math.PI / 180.0);

        double x = (strafe * zFactor - forward * xFactor) * scale;
        double y = up * scale;
        double z = (forward * zFactor + strafe * xFactor) * scale;

        this.setVelocity(new Vec3d(x, y, z));
        this.move(MovementType.SELF, this.getVelocity());
    }

    private void updateLastTickPosition()
    {
//        this.setLastPositionAndAngles(new Vec3d(this.getX(), this.getY(), this.getZ()), this.getYaw(), this.getPitch());
        this.lastRenderX = this.getX();
        this.lastRenderY = this.getY();
        this.lastRenderZ = this.getZ();

        this.lastX = this.getX();
        this.lastY = this.getY();
        this.lastZ = this.getZ();

        this.lastYaw = this.getYaw();
        this.lastPitch = this.getPitch();

        this.lastHeadYaw = this.headYaw;
    }

    public void setCameraRotations(float yaw, float pitch)
    {
        this.setYaw(yaw);
        this.setPitch(pitch);

        this.headYaw = yaw;

        //this.lastRotationYaw = this.rotationYaw;
        //this.lastRotationPitch = this.rotationPitch;

        //this.lastRotationYawHead = this.rotationYaw;
        //this.setRenderYawOffset(this.rotationYaw);
    }

    public void updateCameraRotations(float yawChange, float pitchChange)
    {
        float yaw = this.getYaw() + yawChange * 0.15F;
        float pitch = MathHelper.clamp(this.getPitch() + pitchChange * 0.15F, -90F, 90F);

        this.setYaw(yaw);
        this.setPitch(pitch);

        this.setCameraRotations(yaw, pitch);
    }

    private static CameraEntity createCameraEntity(MinecraftClient mc)
    {
	    if (mc.player == null || mc.world == null)
	    {
		    throw new RuntimeException("Cannot create CameraEntity from null!");
	    }

	    ClientPlayerEntity player = mc.player;

//        Vec3d eyePos = player.getEyePos();
        Vec3d entityPos = player.getPos();
//        BlockPos blockPos = player.getBlockPos();
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        // Don't reset velocity when flying / swimming.
        if (mc.player.isOnGround())
        {
            mc.player.setVelocity(Vec3d.ZERO);
        }

        CameraEntity camera = new CameraEntity(mc, mc.world, player.networkHandler, player.getStatHandler(), player.getRecipeBook(), PlayerInput.DEFAULT, false);
        camera.noClip = true;
//
//        camera.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), yaw, pitch);
//        camera.setRotation(yaw, pitch);

//        Tweakeroo.LOGGER.error("CameraEntity::new() [PLAYER] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", eyePos.toString(), entityPos.toString(), blockPos.toShortString(), player.getVelocity().toString());

        camera.setPos(entityPos.getX(), entityPos.getY() + 0.125f, entityPos.getZ());
        camera.setYaw(yaw);
        camera.setPitch(pitch);
        camera.setVelocity(Vec3d.ZERO);

//        Tweakeroo.LOGGER.error("CameraEntity::new() [CAM] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", camera.getEyePos().toString(), camera.getPos().toString(), camera.getBlockPos().toShortString(), camera.getVelocity().toString());
//        Tweakeroo.LOGGER.error("CameraEntity::new() [AFTER] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", mc.player.getEyePos().toString(), mc.player.getPos().toString(), mc.player.getBlockPos().toShortString(), mc.player.getVelocity().toString());

        return camera;
    }

	public static void updatePositionAtPreset(@Nonnull CameraPreset preset)
	{
		if (camera != null && isValidDim(camera.getWorld(), preset.getDim()))
		{
//			Tweakeroo.LOGGER.error("CameraEntity#updatePositionAtPreset(): oldPos [{}], newPos [{}] // yaw [{}], pitch [{}]", camera.getEyePos().toString(), preset.pos().toString(), preset.yaw(), preset.pitch());
			camera.setPos(preset.getPos().getX(), preset.getPos().getY(), preset.getPos().getZ());
			camera.setYaw(preset.getYaw());
			camera.setPitch(preset.getPitch());
			camera.setVelocity(Vec3d.ZERO);
		}
	}

	@ApiStatus.Experimental
	private static CameraEntity createCameraAtPreset(MinecraftClient mc, @Nonnull CameraPreset preset)
	{
		if (mc.player == null || mc.world == null)
		{
			throw new RuntimeException("Cannot create CameraEntity from null!");
		}

		ClientPlayerEntity player = mc.player;

		// Don't reset velocity when flying / swimming.
		if (mc.player.isOnGround())
		{
			mc.player.setVelocity(Vec3d.ZERO);
		}

		CameraEntity camera = new CameraEntity(mc, mc.world, player.networkHandler, player.getStatHandler(), player.getRecipeBook(), PlayerInput.DEFAULT, false);
		camera.noClip = true;

//        Tweakeroo.LOGGER.error("CameraEntity::new() [PLAYER] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", eyePos.toString(), entityPos.toString(), blockPos.toShortString(), player.getVelocity().toString());

		camera.setPos(preset.getPos().getX(), preset.getPos().getY(), preset.getPos().getZ());
		camera.setYaw(preset.getYaw());
		camera.setPitch(preset.getPitch());
		camera.setVelocity(Vec3d.ZERO);

//        Tweakeroo.LOGGER.error("CameraEntity::new() [CAM] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", camera.getEyePos().toString(), camera.getPos().toString(), camera.getBlockPos().toShortString(), camera.getVelocity().toString());
//        Tweakeroo.LOGGER.error("CameraEntity::new() [AFTER] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", mc.player.getEyePos().toString(), mc.player.getPos().toString(), mc.player.getBlockPos().toShortString(), mc.player.getVelocity().toString());

		return camera;
	}

    @Nullable
    public static CameraEntity getCamera()
    {
        return camera;
    }

    public static void setCameraState(boolean enabled, @Nullable CameraPreset preset)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world != null && mc.player != null)
        {
	        RegistryKey<World> dim = mc.world.getRegistryKey();

            if (enabled && preset != null &&
	            preset.getId() > -1)
            {
				if (preset.getDim().equals(dim.getValue()))
				{
					createAndSetCameraAtPreset(mc, preset);
				}
				else
				{
					Tweakeroo.LOGGER.error("freeCam: Recalled preset is in a different Dimension: [{}]", preset.getDim().toString());
				}
            }
	        else if (enabled)
	        {
		        createAndSetCamera(mc);
	        }
            else
            {
                removeCamera(mc);
            }

//            mc.gameRenderer.setRenderHand(! enabled);
        }
    }

    public static boolean originalCameraWasPlayer()
    {
        return originalCameraWasPlayer;
    }

    private static void createAndSetCamera(MinecraftClient mc)
    {
        camera = createCameraEntity(mc);
	    setCamera(mc);
    }

	@ApiStatus.Experimental
	private static void createAndSetCameraAtPreset(MinecraftClient mc, CameraPreset preset)
	{
		if (isValidDim(mc.world, preset.getDim()))
		{
			camera = createCameraAtPreset(mc, preset);
			setCamera(mc);
		}
	}

	private static void setCamera(MinecraftClient mc)
	{
		originalCameraEntity = mc.getCameraEntity();
		originalCameraWasPlayer = originalCameraEntity == mc.player;
		cullChunksOriginal = mc.chunkCullingEnabled;

		mc.setCameraEntity(camera);
		mc.chunkCullingEnabled = false; // Disable chunk culling

		// Disable the motion option when entering camera mode
		Configs.Generic.FREE_CAMERA_PLAYER_MOVEMENT.setBooleanValue(false);
	}

	private static void removeCamera(MinecraftClient mc)
    {
        if (mc.world != null && camera != null)
        {
            // Re-fetch the player entity, in case the player died while in Free Camera mode and the instance changed
            mc.setCameraEntity(originalCameraWasPlayer ? mc.player : originalCameraEntity);
            mc.chunkCullingEnabled = cullChunksOriginal;

            final int chunkX = MathHelper.floor(camera.getX() / 16.0) >> 4;
            final int chunkZ = MathHelper.floor(camera.getZ() / 16.0) >> 4;
            CameraUtils.markChunksForRebuildOnDeactivation(chunkX, chunkZ);
        }

        originalCameraEntity = null;
        camera = null;
    }

	private static boolean isValidDim(World world, Identifier dim)
	{
		if (world == null)
		{
			return false;
		}

		return world.getRegistryKey().getValue().equals(dim);
	}
}
