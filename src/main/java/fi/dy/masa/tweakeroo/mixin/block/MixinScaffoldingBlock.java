package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.block.*;
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
    public BlockRenderType getRenderType(BlockState state)
    {
        if (Configs.Disable.DISABLE_RENDERING_SCAFFOLDING.getBooleanValue() &&
            state.getBlock() == Blocks.SCAFFOLDING)
        {
            return BlockRenderType.INVISIBLE;
        }

        return super.getRenderType(state);
    }
}
