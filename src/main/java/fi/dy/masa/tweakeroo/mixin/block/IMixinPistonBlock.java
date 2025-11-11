package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.block.PistonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Copied From Tweak Fork by Andrew54757
 */
@Mixin(PistonBlock.class)
public interface IMixinPistonBlock
{
    @Accessor("sticky")
    boolean getSticky();
}
