package fi.dy.masa.tweakeroo.mixin;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.block.Block;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.ObserverBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ObserverBlock.class, priority = 1001)
public abstract class MixinObserverBlock extends FacingBlock
{
    public MixinObserverBlock(Block.Settings builder)
    {
        super(builder);
    }

    @Inject(method = "scheduleTick", at = @At("HEAD"), cancellable = true)
    private void preventTrigger(WorldView world, TickView tickView, BlockPos pos, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_OBSERVER.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
