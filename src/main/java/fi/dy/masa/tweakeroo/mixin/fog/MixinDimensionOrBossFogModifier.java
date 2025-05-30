package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.DimensionOrBossFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionOrBossFogModifier.class)
public class MixinDimensionOrBossFogModifier
{
    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_tweakDimensionOrBossFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        // todo - DISABLE_NETHER_FOG and DISABLE_BOSS_FOG has the same effect
//        FogTweaks.INSTANCE.tweakDimensionOrBossFog(data, cameraEntity, cameraPos, world, viewDistance, tickCounter);
    }
}
