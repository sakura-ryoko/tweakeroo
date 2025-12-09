package fi.dy.masa.tweakeroo.mixin.item;

import java.util.Map;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AxeItem.class)
public interface IMixinAxeItem
{
    @Accessor("STRIPPABLES")
    static Map<Block, Block> tweakeroo_getStrippedBlocks() { return null; }
}
