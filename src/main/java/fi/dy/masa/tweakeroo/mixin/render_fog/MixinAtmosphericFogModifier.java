package fi.dy.masa.tweakeroo.mixin.render_fog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.tweaks.FogTweaks;

@Mixin(AtmosphericFogModifier.class)
public class MixinAtmosphericFogModifier
{
    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_shouldBlockAtmosphericFog(CameraSubmersionType submersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir)
    {
        if (FogTweaks.INSTANCE.shouldBlockAtmosphericFog())
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectAtmosphericFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        FogTweaks.INSTANCE.tweakAtmosphericFog(data, cameraEntity, viewDistance, tickCounter);
    }

    @Redirect(method = "applyStartEndModifier",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float tweakeroo_redirectRainFog(ClientWorld instance, float v)
    {
        return FogTweaks.INSTANCE.tweakAtmosphericRainFog(instance, v);
    }

    @Redirect(method = "applyStartEndModifier",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;hasPrecipitation()Z"))
    private boolean tweakeroo_redirectBiomePrecipitation(Biome instance)
    {
        return FogTweaks.INSTANCE.tweakAtmosphericBiomePrecipitation(instance);
    }
}
