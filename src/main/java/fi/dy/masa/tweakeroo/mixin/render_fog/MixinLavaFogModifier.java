package fi.dy.masa.tweakeroo.mixin.render_fog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.tweaks.FogTweaks;

@Mixin(LavaFogModifier.class)
public class MixinLavaFogModifier
{
    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_shouldBlockLavaFog(CameraSubmersionType submersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir)
    {
        if (FogTweaks.INSTANCE.shouldBlockLavaFog())
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectLavaFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        FogTweaks.INSTANCE.tweakLavaFog(data, cameraEntity, viewDistance, tickCounter);
    }
}
