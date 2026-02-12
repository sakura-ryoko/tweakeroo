package fi.dy.masa.tweakeroo.mixin.fog;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.Mth;

@Mixin(FogRenderer.class)
public class MixinFogRenderer
{
    @WrapOperation(method = "computeFogColor",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;voidDarknessOnsetRange()F"))
    private float tweakeroo_disableSkyDarkness(ClientLevel.ClientLevelData instance, Operation<Float> original)
    {
        return Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue() ? 1.0F : instance.voidDarknessOnsetRange();
    }

    @ModifyConstant(method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;",
                    constant = { @Constant(intValue = 16) })
    private int tweakeroo_tweakRenderDistanceFog_DistanceMultiplier(int constant)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            Minecraft mc = Minecraft.getInstance();

            final int viewDistance = mc.options.getEffectiveRenderDistance();
            final float blocksDistance = Math.max(512.0F, mc.gameRenderer.getRenderDistance());

            // 42 is the answer :)
            return (int) (blocksDistance / viewDistance);
        }

        return constant;
    }

    @WrapOperation(method = "setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;",
                   at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float tweakeroo_tweakRenderDistanceFog_StartDiff(float value, float min, float max, Operation<Float> original)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            return min;
        }

        return Mth.clamp(value, min, max);
    }
}
