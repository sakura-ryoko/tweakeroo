package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractBlock.class)
public interface IMixinAbstractBlock
{
    @Mutable
    @Accessor("slipperiness")
    void setFriction(float friction);

    @Invoker("getPickStack")
    ItemStack tweakeroo_getPickStack(WorldView world, BlockPos pos, BlockState state, boolean bl);
}
