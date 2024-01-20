package fi.dy.masa.tweakeroo.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.state.State;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
    This Mixin is to replace the @Deprecated calls to isSolid(), isLiquid(), and blocksMovement()
    --> Perhaps move to malilib if working for more mods to use
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState extends State<Block, BlockState> implements IMixinAbstractBlockState {
    protected MixinAbstractBlockState(Block owner, ImmutableMap<Property<?>, Comparable<?>> entries, MapCodec<BlockState> codec) {
        super(owner, entries, codec);
    }
    @Shadow public abstract Block getBlock();
    // This removes the Deprecated warnings
    // --> Will adjust later if this gets removed
    @Shadow @Final @SuppressWarnings("Deprecated") private boolean liquid;
    @Unique
    private boolean this_solid;
    @Unique
    private final boolean this_liquid = liquid;
    @Inject(method = "shouldBeSolid", at = @At("RETURN"))
    private void checkSolid(CallbackInfoReturnable<Boolean> cir) {
        this_solid = cir.getReturnValue();
    }
    @Unique
    public boolean tweakeroo$isLiquid() {
        return this_liquid;
    }
    @Unique
    public boolean tweakeroo$isSolid() {
        return this_solid;
    }
    @Unique
    public boolean tweakeroo$blocksMovement() {
        Block block = this.getBlock();
        return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && this.tweakeroo$isSolid();
    }
}
