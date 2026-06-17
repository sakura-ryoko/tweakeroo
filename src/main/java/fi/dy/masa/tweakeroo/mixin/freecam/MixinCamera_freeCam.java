package fi.dy.masa.tweakeroo.mixin.freecam;

import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = Camera.class, priority = 850)
public class MixinCamera_freeCam
{
    @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableFluidFog(CallbackInfoReturnable<FogType> cir)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            cir.setReturnValue(FogType.NONE);
        }
    }

    // TODO ???
//    @ModifyExpressionValue(method = "calculateFov",
//                           at = @At(value = "CONSTANT",
//                                    args = "floatValue=70.0"))
//    private float tweakeroo_applyFreeCameraFov(float original)
//    {
//        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
//        {
//            return ((float) this.minecraft.options.fov().get());
//        }
//
//        return original;
//    }

//    @ModifyVariable(method = "calculateFov",
//                    at = @At(value = "LOAD", ordinal = 0),
//                    argsOnly = true)
//    private boolean tweakeroo_freezeFovOnFreeCamera(boolean value)
//    {
//        return !FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() && value;
//    }
}
