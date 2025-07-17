package fi.dy.masa.tweakeroo.mixin.fog;

import org.joml.Vector4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(FogRenderer.class)
public class MixinFogRenderer
{
    @Redirect(method = "getFogColor",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/world/ClientWorld$Properties;getVoidDarknessRange()F"))
    private float tweakeroo_disableSkyDarkness(ClientWorld.Properties instance)
    {
        return Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue() ? 1.0F : instance.getVoidDarknessRange();
    }

    @Inject(method = "getFogColor", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_adjustFogColor(Camera camera, float tickProgress, ClientWorld world, int viewDistance, float skyDarkness, boolean thick, CallbackInfoReturnable<Vector4f> cir)
    {
        if (FeatureToggle.TWEAK_MATCHING_SKY_FOG.getBooleanValue())
        {
            if (world.getDimension().hasSkyLight())
            {
                int color = world.getSkyColor(camera.getPos(), skyDarkness);

                cir.setReturnValue(new Vector4f(
                        ColorHelper.getRedFloat(color),
                        ColorHelper.getGreenFloat(color),
                        ColorHelper.getBlueFloat(color),
                        ColorHelper.getAlphaFloat(color))
                );
            }
        }
    }

    @ModifyConstant(method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
                    constant = { @Constant(intValue = 16) })
    private int tweakeroo_tweakRenderDistanceFog_DistanceMultiplier(int constant)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            MinecraftClient mc = MinecraftClient.getInstance();

            final int viewDistance = mc.options.getClampedViewDistance();
            final float blocksDistance = Math.max(512.0F, mc.gameRenderer.getViewDistanceBlocks());

            // 42 is the answer :)
            return (int) (blocksDistance / viewDistance);
        }

        return constant;
    }

    @Redirect(method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"))
    private float tweakeroo_tweakRenderDistanceFog_StartDiff(float value, float min, float max)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            return min;
        }

        return MathHelper.clamp(value, min, max);
    }
}
