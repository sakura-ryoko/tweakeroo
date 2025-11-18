package fi.dy.masa.tweakeroo.mixin.fog;

//@Mixin(StandardFogModifier.class)
// todo -- removed from game; now Sky Rendering uses a single constant value supplier.
@Deprecated
public class MixinStandardFogModifier
{
//    @Inject(method = "getFogColor", at = @At("HEAD"), cancellable = true)
//    private void tweakeroo_adjustFogColor(ClientWorld world, Camera camera, int viewDistance, float skyDarkness, CallbackInfoReturnable<Integer> cir)
//    {
//        if (FeatureToggle.TWEAK_MATCHING_SKY_FOG.getBooleanValue())
//        {
//            if (world.getDimension().hasSkyLight())
//            {
//                int color = world.getSkyColor(camera.getCameraPos(), skyDarkness);
//                cir.setReturnValue(color);
//            }
//        }
//    }
}
