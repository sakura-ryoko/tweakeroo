package fi.dy.masa.tweakeroo.mixin.input;

import net.minecraft.client.Mouse;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(Mouse.class)
public abstract class MixinMouse
{
    @ModifyVariable(method = "onMouseScroll", ordinal = 1, at = @At("HEAD"), argsOnly = true)
    private double applyHorizontalScroll(double vertical, long argWindow, double argHorizontal, double argVertical)
    {
        if (Configs.Fixes.MAC_HORIZONTAL_SCROLL.getBooleanValue() &&
			Util.getOperatingSystem() == Util.OperatingSystem.OSX &&
			vertical == 0)
        {
            return argHorizontal;
        }

        return vertical;
    }
}
