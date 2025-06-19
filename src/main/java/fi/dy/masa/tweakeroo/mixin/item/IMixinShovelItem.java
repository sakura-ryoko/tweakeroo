package fi.dy.masa.tweakeroo.mixin.item;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ShovelItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShovelItem.class)
public interface IMixinShovelItem
{
    @Accessor("PATH_STATES")
    static Map<Block, BlockState> tweakeroo_getPathStates() { return null; }
}
