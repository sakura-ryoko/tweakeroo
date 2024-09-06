package fi.dy.masa.tweakeroo.mixin;

import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;

import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.event.InputHandler;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input
{
    @Shadow @Final private GameOptions settings;

    // FIXME
    @Inject(method = "tick(ZF)V", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/input/KeyboardInput;playerInput:Lnet/minecraft/util/PlayerInput;",
            ordinal = 0,
            shift = Shift.AFTER,
            opcode = Opcodes.PUTFIELD))
    private void customMovement(boolean val1, float f, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_MOVEMENT_KEYS.getBooleanValue())
        {
            InputHandler.getInstance().handleMovementKeys(this);
        }

        if (FeatureToggle.TWEAK_PERMANENT_SNEAK.getBooleanValue() &&
            (Configs.Generic.PERMANENT_SNEAK_ALLOW_IN_GUIS.getBooleanValue() || GuiUtils.getCurrentScreen() == null))
        {
            this.playerInput = new PlayerInput(this.settings.forwardKey.isPressed(), this.settings.backKey.isPressed(), this.settings.leftKey.isPressed(), this.settings.rightKey.isPressed(), this.settings.jumpKey.isPressed(), true, this.settings.sprintKey.isPressed());
            // this.sneaking = true;
        }
    }
}
