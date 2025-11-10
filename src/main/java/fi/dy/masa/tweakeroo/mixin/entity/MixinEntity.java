package fi.dy.masa.tweakeroo.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import fi.dy.masa.tweakeroo.util.SnapAimMode;
import fi.dy.masa.tweakeroo.util.SnapAimUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class MixinEntity
{
    @Shadow public abstract Vec3 getDeltaMovement();
    @Shadow public abstract void setDeltaMovement(Vec3 velocity);
    @Shadow private float yRot;
    @Shadow private float xRot;
    @Shadow public float yRotO;
    @Shadow public float xRotO;

    @Unique private double lastFreePitch;
    @Unique private double lastFreeYaw;
    @Unique private double cameraPitch;
    @Unique private double cameraYaw;

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void overrideIsInvisibleToPlayer(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_RENDER_INVISIBLE_ENTITIES.getBooleanValue())
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
    private void moreAccurateMoveRelative(float speedIn, net.minecraft.world.phys.Vec3 motion, CallbackInfo ci)
    {
        if ((Object) this instanceof LocalPlayer &&
            (FeatureToggle.TWEAK_SNAP_AIM.getBooleanValue() ||
             FeatureToggle.TWEAK_AIM_LOCK.getBooleanValue()))
        {
            SnapAimUtils.onUpdateVelocity((Entity) (Object) this, this.yRot, speedIn, motion, ci);
        }
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void overrideYaw(double yawChange, double pitchChange, CallbackInfo ci)
    {
        if ((Object) this instanceof LocalPlayer)
        {
            if (CameraUtils.shouldPreventPlayerMovement())
            {
                CameraUtils.updateCameraRotations((float) yawChange, (float) pitchChange);
            }

            if (FeatureToggle.TWEAK_ELYTRA_CAMERA.getBooleanValue() && Hotkeys.ELYTRA_CAMERA.getKeybind().isKeybindHeld())
            {
                int pitchLimit = Configs.Generic.SNAP_AIM_PITCH_OVERSHOOT.getBooleanValue() ? 180 : 90;

                this.cameraYaw += yawChange * 0.15D;
                this.cameraPitch = net.minecraft.util.Mth.clamp(this.cameraPitch + pitchChange * 0.15D, -pitchLimit, pitchLimit);

                CameraUtils.setCameraYaw((float) this.cameraYaw);
                CameraUtils.setCameraPitch((float) this.cameraPitch);

                this.yRot = this.yRotO;
                this.xRot = this.xRotO;
                ci.cancel();

                return;
            }

            if (FeatureToggle.TWEAK_AIM_LOCK.getBooleanValue())
            {
                if (FeatureToggle.TWEAK_SNAP_AIM.getBooleanValue())
                {
                    this.yRot = SnapAimUtils.getSnappedYaw(this.lastFreeYaw);
                    this.xRot = SnapAimUtils.getSnappedPitch(this.lastFreePitch);
                }
                else
                {
                    this.yRot = (float) this.lastFreeYaw;
                    this.xRot = (float) this.lastFreePitch;
                }

                this.yRotO = this.yRot;
                this.xRotO = this.xRot;
                ci.cancel();

                return;
            }
            
            if (FeatureToggle.TWEAK_SNAP_AIM.getBooleanValue())
            {
                int pitchLimit = Configs.Generic.SNAP_AIM_PITCH_OVERSHOOT.getBooleanValue() ? 180 : 90;
                SnapAimMode mode = (SnapAimMode) Configs.Generic.SNAP_AIM_MODE.getOptionListValue();
                boolean snapAimLock = FeatureToggle.TWEAK_SNAP_AIM_LOCK.getBooleanValue();

                // Not locked, or not snapping the yaw (ie. not in Yaw or Both modes)
                boolean updateYaw = snapAimLock == false || mode == SnapAimMode.PITCH;
                // Not locked, or not snapping the pitch (ie. not in Pitch or Both modes)
                boolean updatePitch = snapAimLock == false || mode == SnapAimMode.YAW;

                this.updateCustomPlayerRotations(yawChange, pitchChange, updateYaw, updatePitch, pitchLimit);

                this.yRot = SnapAimUtils.getSnappedYaw(this.lastFreeYaw);
                this.xRot = SnapAimUtils.getSnappedPitch(this.lastFreePitch);
                this.yRotO = this.yRot;
                this.xRotO = this.xRot;
                ci.cancel();

                return;
            }

            if (CameraUtils.shouldPreventPlayerMovement())
            {
                ci.cancel();
                return;
            }

            // Update the internal rotations while no locking features are enabled
            // They will then be used as the forced rotations when some of the locking features are activated.
            this.lastFreeYaw = this.yRot;
            this.lastFreePitch = this.xRot;
            this.cameraYaw = this.yRot;
            this.cameraPitch = this.xRot;
        }
    }

    @Unique
    private void updateCustomPlayerRotations(double yawChange, double pitchChange, boolean updateYaw, boolean updatePitch, float pitchLimit)
    {
        if (updateYaw)
        {
            this.lastFreeYaw += yawChange * 0.15D;
        }

        if (updatePitch)
        {
            this.lastFreePitch = net.minecraft.util.Mth.clamp(this.lastFreePitch + pitchChange * 0.15D, -pitchLimit, pitchLimit);
        }
    }
}
