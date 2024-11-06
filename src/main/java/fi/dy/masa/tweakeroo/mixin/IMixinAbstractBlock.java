package fi.dy.masa.tweakeroo.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.block.AbstractBlock.class)
public interface IMixinAbstractBlock
{
    @Mutable
    @Accessor("slipperiness")
    void setFriction(float friction);

    @Invoker("getPickStack")
    ItemStack tweakeroo_getPickStack(WorldView world, BlockPos pos, BlockState state, boolean bl);
}
