package fi.dy.masa.tweakeroo.mixin.item;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AxeItem.class)
public interface IMixinAxeItem
{
    @Accessor("STRIPPED_BLOCKS")
    static Map<Block, Block> tweakeroo_getStrippedBlocks() { return null; }
}
