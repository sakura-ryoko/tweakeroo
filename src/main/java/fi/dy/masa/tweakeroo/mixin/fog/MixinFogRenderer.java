package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import fi.dy.masa.tweakeroo.tweaks.FogTweaks;

@Mixin(FogRenderer.class)
public class MixinFogRenderer
{
    @Redirect(method = "getFogColor",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/world/ClientWorld$Properties;getVoidDarknessRange()F"))
    private float tweakeroo_disableSkyDarkness(ClientWorld.Properties instance)
    {
        return FogTweaks.INSTANCE.tweakSkyDarkness(instance);
    }

    @ModifyConstant(method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
                    constant = { @Constant(intValue = 16) })
    private int tweakeroo_tweakRenderDistanceFog(int constant)
    {
        return FogTweaks.INSTANCE.tweakRenderDistanceFog_Distance(constant);
    }

    @Redirect(method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"))
    private float tweakeroo_tweakRenderDistanceFog(float value, float min, float max)
    {
        return FogTweaks.INSTANCE.tweakRenderDistanceFog_Clamp(value, min, max);
    }
}
