package fi.dy.masa.tweakeroo.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Mixin(value = ObserverBlock.class, priority = 1001)
public abstract class MixinObserverBlock extends DirectionalBlock
{
    public MixinObserverBlock(BlockBehaviour.Properties builder)
    {
        super(builder);
    }

    @Inject(method = "startSignal", at = @At("HEAD"), cancellable = true)
    private void preventTrigger(LevelReader world, ScheduledTickAccess tickView, BlockPos pos, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_OBSERVER.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
