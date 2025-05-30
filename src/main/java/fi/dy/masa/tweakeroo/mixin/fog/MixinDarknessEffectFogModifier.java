package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.DarknessEffectFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(DarknessEffectFogModifier.class)
public class MixinDarknessEffectFogModifier
{
    @Inject(method = "applyStartEndModifier", at = @At("RETURN"))
    private void tweakeroo_redirectDarknessFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_DARKNESS_VISIBILITY.getBooleanValue())
        {
            // Trying not to make this too overpowered.
            // To me, no more than around 3.0f should suffice; and
            // I want to make this bound to some enchantment.
            // Could remove it by changing the adj value higher, but
            // that would be no fun; now would it?
            final float adj = data.skyEnd * 2.7F;

            data.environmentalStart = adj * 0.75F;
            data.environmentalEnd = adj;
            data.skyEnd = adj;
            data.cloudEnd = adj;
        }
    }
}
