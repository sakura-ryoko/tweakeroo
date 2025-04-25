package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(Camera.class)
public class MixinCamera
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
