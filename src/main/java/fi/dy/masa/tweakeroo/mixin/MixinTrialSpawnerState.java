package fi.dy.masa.tweakeroo.mixin;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrialSpawnerState.class)
public class MixinTrialSpawnerState
{
    @Inject(method = "emitParticles", at = @At("HEAD"), cancellable = true)
    private void cancelParticleRendering(World world, BlockPos pos, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_TRIAL_SPAWNER_PARTICLE.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
