package fi.dy.masa.tweakeroo.util;

import javax.annotation.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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
        ClientPlayerEntity player = mc.player;

        if (player == null)
        {
            throw new RuntimeException("Cannot create CameraEntity from null!");
        }

//        Vec3d eyePos = player.getEyePos();
        Vec3d entityPos = player.getPos();
//        BlockPos blockPos = player.getBlockPos();
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        mc.player.setVelocity(Vec3d.ZERO);

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

    @Nullable
    public static CameraEntity getCamera()
    {
        return camera;
    }

    public static void setCameraState(boolean enabled)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.world != null && mc.player != null)
        {
            if (enabled)
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
}
