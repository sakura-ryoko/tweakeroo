package fi.dy.masa.tweakeroo.mixin.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelHeightAccessor;

@Mixin(ClientLevel.ClientLevelData.class)
public class MixinClientLevel_ClientLevelData
{
    @Inject(method = "getHorizonHeight", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_overrideSkyDarknessHeight(LevelHeightAccessor world, CallbackInfoReturnable<Double> cir)
    {
        // Disable the dark sky effect in normal situations
        // by moving the y threshold below the bottom of the world
        if (Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue())
        {
            cir.setReturnValue(world.getMinY() - 2.0);
        }
    }
}
