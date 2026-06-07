package fi.dy.masa.tweakeroo.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(SlimeBlock.class)
public abstract class MixinSlimeBlock extends HalfTransparentBlock
{
    public MixinSlimeBlock(BlockBehaviour.Properties settings)
    {
        super(settings);
    }

    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void onEntityWalkOnSlime(Level level, BlockPos pos, BlockState onState, Entity entity, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_SLIME_BLOCK_SLOWDOWN.getBooleanValue() && entity instanceof Player)
        {
            super.stepOn(level, pos, onState, entity);
            ci.cancel();
        }
    }
}
