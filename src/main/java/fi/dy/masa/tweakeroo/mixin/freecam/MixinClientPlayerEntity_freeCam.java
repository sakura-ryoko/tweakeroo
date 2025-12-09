package fi.dy.masa.tweakeroo.mixin.freecam;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraEntity;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import fi.dy.masa.tweakeroo.util.DummyMovementInput;

import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

@Mixin(value = LocalPlayer.class, priority = 1005)
public abstract class MixinClientPlayerEntity_freeCam extends AbstractClientPlayer
{
    @Shadow public ClientInput input;

    @Unique private final DummyMovementInput dummyMovementInput = new DummyMovementInput(null);
    @Unique private ClientInput realInput;

    private MixinClientPlayerEntity_freeCam(ClientLevel world, GameProfile profile)
    {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tweakeroo_disableMovementInputsPre(CallbackInfo ci)
    {
        if (CameraUtils.shouldPreventPlayerMovement())
        {
            this.realInput = this.input;
            this.input = this.dummyMovementInput;
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tweakeroo_disableMovementInputsPost(CallbackInfo ci)
    {
        if (this.realInput != null)
        {
            this.input = this.realInput;
            this.realInput = null;
        }
    }

    @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_allowPlayerMovementInFreeCameraMode(CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() && CameraEntity.originalCameraWasPlayer())
        {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_preventHandSwing(InteractionHand hand, CallbackInfo ci)
    {
        if (CameraUtils.shouldPreventPlayerInputs())
        {
            ci.cancel();
        }
    }
}
