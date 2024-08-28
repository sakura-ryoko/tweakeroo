package fi.dy.masa.tweakeroo.mixin;

import org.joml.Vector4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
                            from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;FIRE_RESISTANCE:Lnet/minecraft/registry/entry/RegistryEntry;"),
                            to   = @At(value = "FIELD", target = "Lnet/minecraft/block/enums/CameraSubmersionType;POWDER_SNOW:Lnet/minecraft/block/enums/CameraSubmersionType;")),
            constant = @Constant(floatValue = 0.25f),
            require = 0)
    private static float reduceLavaFogStart(float original)
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
                    from = @At(value = "FIELD", target = "Lnet/minecraft/entity/effect/StatusEffects;FIRE_RESISTANCE:Lnet/minecraft/registry/entry/RegistryEntry;"),
                    to   = @At(value = "FIELD", target = "Lnet/minecraft/block/enums/CameraSubmersionType;POWDER_SNOW:Lnet/minecraft/block/enums/CameraSubmersionType;")),
            constant = { @Constant(floatValue = 1.0f), @Constant(floatValue = 5.0f)},
            require = 0)
    private static float reduceLavaFogEnd(float original)
    {
        wasLava = true;

        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            return RenderUtils.getLavaFogDistance(MinecraftClient.getInstance().getCameraEntity(), original);
        }

        return original;
    }

    /*
    @ModifyVariable(
            method = "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZ)V",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BackgroundRenderer$FogType;FOG_SKY:Lnet/minecraft/client/render/BackgroundRenderer$FogType;")),
            at = @At(value = "STORE", opcode = Opcodes.FSTORE, ordinal = 2), ordinal = 1)
    private static float overrideFogStart(float original)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            return Math.max(512, MinecraftClient.getInstance().gameRenderer.getViewDistance()) * 1.6f;
        }

        return original;
    }

    @ModifyVariable(
            method = "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZ)V",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BackgroundRenderer$FogType;FOG_SKY:Lnet/minecraft/client/render/BackgroundRenderer$FogType;")),
            at = @At(value = "STORE", opcode = Opcodes.FSTORE, ordinal = 3), ordinal = 2)
    private static float overrideFogEnd(float original)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            return Math.max(512, MinecraftClient.getInstance().gameRenderer.getViewDistance()) * 2.0f;
        }

        return original;
    }
    */

    @Redirect(method = "getFogColor",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/world/ClientWorld$Properties;getHorizonShadingRatio()F"))
    private static float tweakeroo_disableSkyDarkness(ClientWorld.Properties props)
    {
        return Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue() ? 1.0F : props.getHorizonShadingRatio();
    }

    @Inject(method = "applyFog",
            require = 0,
            at = @At(value = "RETURN", remap = false), cancellable = true)
    private static void disableRenderDistanceFog(
            Camera camera, BackgroundRenderer.FogType fogType, Vector4f v, float viewDistance, boolean thickFog, float tickDelta, CallbackInfoReturnable<Fog> cir)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            if (thickFog == false && wasLava == false)
            {
                float distance = Math.max(512, MinecraftClient.getInstance().gameRenderer.getViewDistance());
                cir.setReturnValue(
                        new Fog(distance * 1.6F, distance * 2.0F, FogShape.CYLINDER,
                        v.x, v.y, v.z, v.w));

                //RenderSystem.setShaderFogStart(distance * 1.6F);
                //RenderSystem.setShaderFogEnd(distance * 2.0F);
            }

            wasLava = false;
        }
    }
}
