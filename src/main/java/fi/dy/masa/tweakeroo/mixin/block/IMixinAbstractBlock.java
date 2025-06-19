package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlock.class)
public interface IMixinAbstractBlock
{
    @Mutable
    @Accessor("slipperiness")
    void setFriction(float friction);
}
