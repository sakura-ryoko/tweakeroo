package fi.dy.masa.tweakeroo.mixin.block;

import javax.annotation.Nonnull;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import fi.dy.masa.tweakeroo.config.Configs;


@Mixin(ScaffoldingBlock.class)
public abstract class MixinScaffoldingBlock extends Block
{
    private MixinScaffoldingBlock(Properties settings)
    {
        super(settings);
    }

    @Deprecated
    @Override
    public @Nonnull RenderShape getRenderShape(@Nonnull BlockState state)
    {
        if (Configs.Disable.DISABLE_RENDERING_SCAFFOLDING.getBooleanValue() &&
            state.getBlock() == Blocks.SCAFFOLDING)
        {
            return RenderShape.INVISIBLE;
        }

        return super.getRenderShape(state);
    }
}
