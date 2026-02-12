package fi.dy.masa.tweakeroo.mixin.world;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;

@Mixin(LevelLightEngine.class)
public abstract class MixinLevelLightEngine
{
    @Shadow @Final @Nullable private LightEngine<?, ?> blockEngine;

    @Inject(method = "checkBlock", at = @At("HEAD"), cancellable = true)
    private void disableLightUpdates(BlockPos pos, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_CLIENT_LIGHT_UPDATES.getBooleanValue() &&
            this.blockEngine != null &&
            ((IMixinChunkLightProvider) this.blockEngine).tweakeroo_getChunkProvider().getLevel() == Minecraft.getInstance().level)
        {
            ci.cancel();
        }
    }
}
