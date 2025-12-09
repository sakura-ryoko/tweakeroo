package fi.dy.masa.tweakeroo.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

public class CameraEntity extends LocalPlayer
{
    @Nullable private static CameraEntity camera;
    @Nullable private static Entity originalCameraEntity;
    private static Vec3 cameraMotion = new Vec3(0.0, 0.0, 0.0);
    private static boolean cullChunksOriginal;
    private static boolean sprinting;
    private static boolean originalCameraWasPlayer;

    private CameraEntity(Minecraft mc, ClientLevel world,
                         ClientPacketListener netHandler, StatsCounter stats,
                         ClientRecipeBook recipeBook, Input input, boolean sprinting)
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
            Options options = Minecraft.getInstance().options;

            camera.updateLastTickPosition();

            if (options.keySprint.isDown())
            {
                sprinting = true;
            }
            else if (options.keyUp.isDown() == false && options.keyDown.isDown() == false)
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
        float yaw = this.getYRot();
        double scale = getMoveSpeed();
        double xFactor = Math.sin(yaw * Math.PI / 180.0);
        double zFactor = Math.cos(yaw * Math.PI / 180.0);

        double x = (strafe * zFactor - forward * xFactor) * scale;
        double y = up * scale;
        double z = (forward * zFactor + strafe * xFactor) * scale;

        this.setDeltaMovement(new Vec3(x, y, z));
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private void updateLastTickPosition()
    {
//        this.setLastPositionAndAngles(new Vec3d(this.getX(), this.getY(), this.getZ()), this.getYaw(), this.getPitch());
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();

        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        this.yHeadRotO = this.yHeadRot;
    }

    public void setCameraRotations(float yaw, float pitch)
    {
        this.setYRot(yaw);
        this.setXRot(pitch);

        this.yHeadRot = yaw;

        //this.lastRotationYaw = this.rotationYaw;
        //this.lastRotationPitch = this.rotationPitch;

        //this.lastRotationYawHead = this.rotationYaw;
        //this.setRenderYawOffset(this.rotationYaw);
    }

    public void updateCameraRotations(float yawChange, float pitchChange)
    {
        float yaw = this.getYRot() + yawChange * 0.15F;
        float pitch = Mth.clamp(this.getXRot() + pitchChange * 0.15F, -90F, 90F);

        this.setYRot(yaw);
        this.setXRot(pitch);

        this.setCameraRotations(yaw, pitch);
    }

    private static CameraEntity createCameraEntity(Minecraft mc)
    {
	    if (mc.player == null || mc.level == null)
	    {
		    throw new RuntimeException("Cannot create CameraEntity from null!");
	    }

	    LocalPlayer player = mc.player;

//        Vec3d eyePos = player.getEyePos();
        Vec3 entityPos = player.position();
//        BlockPos blockPos = player.getBlockPos();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        // Don't reset velocity when flying / swimming.
        if (mc.player.onGround())
        {
            mc.player.setDeltaMovement(Vec3.ZERO);
        }

        CameraEntity camera = new CameraEntity(mc, mc.level, player.connection, player.getStats(), player.getRecipeBook(), Input.EMPTY, false);
        camera.noPhysics = true;
//
//        camera.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), yaw, pitch);
//        camera.setRotation(yaw, pitch);

//        Tweakeroo.LOGGER.error("CameraEntity::new() [PLAYER] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", eyePos.toString(), entityPos.toString(), blockPos.toShortString(), player.getVelocity().toString());

        camera.setPosRaw(entityPos.x(), entityPos.y() + 0.125f, entityPos.z());
        camera.setYRot(yaw);
        camera.setXRot(pitch);
        camera.setDeltaMovement(Vec3.ZERO);

//        Tweakeroo.LOGGER.error("CameraEntity::new() [CAM] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", camera.getEyePos().toString(), camera.getPos().toString(), camera.getBlockPos().toShortString(), camera.getVelocity().toString());
//        Tweakeroo.LOGGER.error("CameraEntity::new() [AFTER] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", mc.player.getEyePos().toString(), mc.player.getPos().toString(), mc.player.getBlockPos().toShortString(), mc.player.getVelocity().toString());

        return camera;
    }

	public static void updatePositionAtPreset(@Nonnull CameraPreset preset)
	{
		if (camera != null && isValidDim(camera.level(), preset.getDim()))
		{
//			Tweakeroo.LOGGER.error("CameraEntity#updatePositionAtPreset(): oldPos [{}], newPos [{}] // yaw [{}], pitch [{}]", camera.getEyePos().toString(), preset.pos().toString(), preset.yaw(), preset.pitch());
			camera.setPosRaw(preset.getPos().x(), preset.getPos().y(), preset.getPos().z());
			camera.setYRot(preset.getYaw());
			camera.setXRot(preset.getPitch());
			camera.setDeltaMovement(Vec3.ZERO);
		}
	}

	@ApiStatus.Experimental
	private static CameraEntity createCameraAtPreset(Minecraft mc, @Nonnull CameraPreset preset)
	{
		if (mc.player == null || mc.level == null)
		{
			throw new RuntimeException("Cannot create CameraEntity from null!");
		}

		LocalPlayer player = mc.player;

		// Don't reset velocity when flying / swimming.
		if (mc.player.onGround())
		{
			mc.player.setDeltaMovement(Vec3.ZERO);
		}

		CameraEntity camera = new CameraEntity(mc, mc.level, player.connection, player.getStats(), player.getRecipeBook(), Input.EMPTY, false);
		camera.noPhysics = true;

//        Tweakeroo.LOGGER.error("CameraEntity::new() [PLAYER] eyePos [{}], pos [{}], blockPos [{}] // Velocity [{}]", eyePos.toString(), entityPos.toString(), blockPos.toShortString(), player.getVelocity().toString());

		camera.setPosRaw(preset.getPos().x(), preset.getPos().y(), preset.getPos().z());
		camera.setYRot(preset.getYaw());
		camera.setXRot(preset.getPitch());
		camera.setDeltaMovement(Vec3.ZERO);

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
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.player != null)
        {
	        ResourceKey<Level> dim = mc.level.dimension();

            if (enabled && preset != null &&
	            preset.getId() > -1)
            {
				if (preset.getDim().equals(dim.identifier()))
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

    private static void createAndSetCamera(Minecraft mc)
    {
        camera = createCameraEntity(mc);
	    setCamera(mc);
    }

	@ApiStatus.Experimental
	private static void createAndSetCameraAtPreset(Minecraft mc, CameraPreset preset)
	{
		if (isValidDim(mc.level, preset.getDim()))
		{
			camera = createCameraAtPreset(mc, preset);
			setCamera(mc);
		}
	}

	private static void setCamera(Minecraft mc)
	{
		originalCameraEntity = mc.getCameraEntity();
		originalCameraWasPlayer = originalCameraEntity == mc.player;
		cullChunksOriginal = mc.smartCull;

		mc.setCameraEntity(camera);
		mc.smartCull = false; // Disable chunk culling

		// Disable the motion option when entering camera mode
		Configs.Generic.FREE_CAMERA_PLAYER_MOVEMENT.setBooleanValue(false);
	}

	private static void removeCamera(Minecraft mc)
    {
        if (mc.level != null && camera != null)
        {
            // Re-fetch the player entity, in case the player died while in Free Camera mode and the instance changed
            mc.setCameraEntity(originalCameraWasPlayer ? mc.player : originalCameraEntity);
            mc.smartCull = cullChunksOriginal;

            final int chunkX = Mth.floor(camera.getX() / 16.0) >> 4;
            final int chunkZ = Mth.floor(camera.getZ() / 16.0) >> 4;
            CameraUtils.markChunksForRebuildOnDeactivation(chunkX, chunkZ);
        }

        originalCameraEntity = null;
        camera = null;
    }

	private static boolean isValidDim(Level world, Identifier dim)
	{
		if (world == null)
		{
			return false;
		}

		return world.dimension().identifier().equals(dim);
	}
}
