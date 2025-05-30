package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.WaterFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.tweaks.FogTweaks;

@Mixin(WaterFogModifier.class)
public class MixinWaterFogModifier
{
    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_shouldBlockWaterFog(CameraSubmersionType submersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir)
    {
        if (FogTweaks.INSTANCE.shouldBlockWaterFog())
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectWaterFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        FogTweaks.INSTANCE.tweakWaterFog(data, cameraEntity, viewDistance, tickCounter);
    }
}
