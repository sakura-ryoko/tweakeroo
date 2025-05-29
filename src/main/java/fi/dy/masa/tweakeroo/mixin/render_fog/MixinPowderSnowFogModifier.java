package fi.dy.masa.tweakeroo.mixin.render_fog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.fog.PowderSnowFogModifier;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.tweaks.FogTweaks;

@Mixin(PowderSnowFogModifier.class)
public class MixinPowderSnowFogModifier
{
    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_shouldBlockPoweredSnowFog(CameraSubmersionType submersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir)
    {
        if (FogTweaks.INSTANCE.shouldBlockPoweredSnowFog())
        {
            cir.setReturnValue(false);
        }
    }
}
