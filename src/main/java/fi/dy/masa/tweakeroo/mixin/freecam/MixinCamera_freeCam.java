package fi.dy.masa.tweakeroo.mixin.freecam;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;

@Mixin(value = Camera.class, priority = 1005)
public class MixinCamera_freeCam
{
    @Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableFluidFog(CallbackInfoReturnable<CameraSubmersionType> cir)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue())
        {
            cir.setReturnValue(CameraSubmersionType.NONE);
        }
    }
}
