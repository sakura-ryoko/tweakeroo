package fi.dy.masa.tweakeroo.mixin.item;

import java.util.Map;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShovelItem.class)
public interface IMixinShovelItem
{
    @Accessor("FLATTENABLES")
    static Map<Block, BlockState> tweakeroo_getPathStates() { return null; }
}
