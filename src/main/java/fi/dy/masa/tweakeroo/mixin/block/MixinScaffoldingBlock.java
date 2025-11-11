package fi.dy.masa.tweakeroo.mixin.block;

import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ScaffoldingBlock;
import org.spongepowered.asm.mixin.Mixin;
import fi.dy.masa.tweakeroo.config.Configs;


@Mixin(ScaffoldingBlock.class)
public abstract class MixinScaffoldingBlock extends Block
{
    private MixinScaffoldingBlock(Settings settings)
    {
        super(settings);
    }

    @Deprecated
    @Override
    public @Nonnull BlockRenderType getRenderType(@Nonnull BlockState state)
    {
        if (Configs.Disable.DISABLE_RENDERING_SCAFFOLDING.getBooleanValue() &&
            state.getBlock() == Blocks.SCAFFOLDING)
        {
            return BlockRenderType.INVISIBLE;
        }

        return super.getRenderType(state);
    }
}
