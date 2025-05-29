package fi.dy.masa.tweakeroo.mixin.render_fog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.fog.DimensionOrBossFogModifier;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.tweaks.FogTweaks;

@Mixin(DimensionOrBossFogModifier.class)
public class MixinDimensionOrBossFogModifier
{
    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_shouldBlockDimensionOrBossFog(CameraSubmersionType submersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir)
    {
        if (FogTweaks.INSTANCE.shouldBlockDimensionOrBossFog())
        {
            cir.setReturnValue(false);
        }
    }
}
