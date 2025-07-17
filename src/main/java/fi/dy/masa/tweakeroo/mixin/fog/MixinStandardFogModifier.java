package fi.dy.masa.tweakeroo.mixin.fog;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.StandardFogModifier;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StandardFogModifier.class)
public class MixinStandardFogModifier {

    @Inject(method = "getFogColor", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_adjustFogColor(ClientWorld world, Camera camera, int viewDistance, float skyDarkness, CallbackInfoReturnable<Integer> cir)
    {
        if (FeatureToggle.TWEAK_MATCHING_SKY_FOG.getBooleanValue())
        {
            if (world.getDimension().hasSkyLight())
            {
                int color = world.getSkyColor(camera.getPos(), skyDarkness);
                cir.setReturnValue(color);
            }
        }
    }
}
