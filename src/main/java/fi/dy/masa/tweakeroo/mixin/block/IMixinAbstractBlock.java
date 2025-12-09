package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockBehaviour.class)
public interface IMixinAbstractBlock
{
    @Mutable
    @Accessor("friction")
    void setFriction(float friction);

    @Invoker("getCloneItemStack")
    ItemStack tweakeroo_getPickStack(LevelReader world, BlockPos pos, BlockState state, boolean bl);
}
