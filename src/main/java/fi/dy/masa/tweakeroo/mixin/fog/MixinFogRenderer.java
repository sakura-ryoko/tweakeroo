package fi.dy.masa.tweakeroo.mixin.fog;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import fi.dy.masa.malilib.util.MathUtils;
import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = FogRenderer.class, priority = 900)
public class MixinFogRenderer
{
    @WrapOperation(method = "computeFogColor",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;voidDarknessOnsetRange()F"))
    private float tweakeroo_disableSkyDarkness(ClientLevel.ClientLevelData instance, Operation<Float> original)
    {
        return Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue() ? 1.0F : original.call(instance);
    }

    @ModifyConstant(method = "setupFog",
                    constant = { @Constant(intValue = 16) })
    private int tweakeroo_tweakRenderDistanceFog_DistanceMultiplier(int constant)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            Minecraft mc = Minecraft.getInstance();

            final int viewDistance = mc.options.getEffectiveRenderDistance();
            final float blocksDistance = MathUtils.max(512.0F, mc.gameRenderer.getGameRenderState().optionsRenderState.renderDistance);

            // 42 is the answer :)
            return (int) (blocksDistance / viewDistance);
        }

        return constant;
    }

    @WrapOperation(method = "setupFog",
                   at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float tweakeroo_tweakRenderDistanceFog_StartDiff(float value, float min, float max, Operation<Float> original)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            return min;
        }

        return original.call(value, min, max);
    }
}
