package fi.dy.masa.tweakeroo.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.renderer.DimensionSpecialEffects;

@Mixin(DimensionSpecialEffects.NetherEffects.class)
public abstract class MixinDimensionEffects_Nether extends DimensionSpecialEffects
{
    private MixinDimensionEffects_Nether(SkyType skyType, boolean shouldRenderSky, boolean darkened)
    {
        super(skyType, shouldRenderSky, darkened);
    }

    @Inject(method = "isFoggyAt", at = @At("HEAD"), cancellable = true)
    private void disableNetherFog(int x, int z, CallbackInfoReturnable<Boolean> cir)
    {
        if (Configs.Disable.DISABLE_NETHER_FOG.getBooleanValue())
        {
            cir.setReturnValue(false);
        }
    }
}
