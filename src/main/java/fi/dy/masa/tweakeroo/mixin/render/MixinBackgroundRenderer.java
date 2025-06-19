package fi.dy.masa.tweakeroo.mixin.render;

import org.objectweb.asm.Opcodes;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;

@Mixin(BackgroundRenderer.class)
public abstract class MixinBackgroundRenderer
{
    @Unique private static boolean wasLava;

    @ModifyConstant(
            method = "applyFog",
            slice = @Slice(
                            from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;FIRE_RESISTANCE:Lnet/minecraft/registry/entry/RegistryEntry;",
                                       opcode = Opcodes.GETSTATIC),
                            to   = @At(value = "FIELD", target = "Lnet/minecraft/block/enums/CameraSubmersionType;POWDER_SNOW:Lnet/minecraft/block/enums/CameraSubmersionType;",
                                       opcode = Opcodes.GETSTATIC)),
            constant = @Constant(floatValue = 0.25f),
            require = 0)
    private static float tweakeroo_reduceLavaFogStart(float original)
    {
        wasLava = true;

        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            return 0.0f;
        }

        return original;
    }

    @ModifyConstant(
            method = "applyFog",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;FIRE_RESISTANCE:Lnet/minecraft/registry/entry/RegistryEntry;",
                               opcode = Opcodes.GETSTATIC),
                    to   = @At(value = "FIELD", target = "Lnet/minecraft/block/enums/CameraSubmersionType;POWDER_SNOW:Lnet/minecraft/block/enums/CameraSubmersionType;",
                               opcode = Opcodes.GETSTATIC)),
            constant = { @Constant(floatValue = 1.0f), @Constant(floatValue = 5.0f)},
            require = 0)
    private static float tweakeroo_reduceLavaFogEnd(float original)
    {
        wasLava = true;

        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            return RenderUtils.getLavaFogDistance(MinecraftClient.getInstance().getCameraEntity(), original);
        }

        return original;
    }

    // todo 1.21.3+
//    @Inject(method = "getFogColor", at = @At("HEAD"), cancellable = true)
//    private static void tweakeroo_adjustFogColor(Camera camera, float tickProgress, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir)
//    {
//        if (FeatureToggle.TWEAK_MATCHING_SKY_FOG.getBooleanValue())
//        {
//            if (world.getDimension().hasSkyLight())
//            {
//                int color = world.getSkyColor(camera.getPos(), skyDarkness);
//
//                cir.setReturnValue(new Vector4f(
//                        ColorHelper.getRed(color),
//                        ColorHelper.getGreen(color),
//                        ColorHelper.getBlue(color),
//                        ColorHelper.getAlpha(color))
//                );
//            }
//        }
//    }

    @Redirect(method = "render",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/world/ClientWorld$Properties;getHorizonShadingRatio()F"))
    private static float tweakeroo_disableSkyDarkness(ClientWorld.Properties props)
    {
        return Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue() ? 1.0F : props.getHorizonShadingRatio();
    }

    @Inject(method = "applyFog",
            require = 0,
            at = @At(value = "INVOKE", remap = false,
                     target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V",
                     shift = At.Shift.AFTER))
    private static void tweakeroo_disableRenderDistanceFog(
            Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            if (thickFog == false && wasLava == false)
            {
                float distance = Math.max(512, MinecraftClient.getInstance().gameRenderer.getViewDistance());
                RenderSystem.setShaderFogStart(distance * 1.6F);
                RenderSystem.setShaderFogEnd(distance * 2.0F);
            }

            wasLava = false;
        }
    }
}
