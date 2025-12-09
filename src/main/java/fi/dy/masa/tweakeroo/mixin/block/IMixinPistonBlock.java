package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.world.level.block.piston.PistonBaseBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Copied From Tweak Fork by Andrew54757
 */
@Mixin(PistonBaseBlock.class)
public interface IMixinPistonBlock
{
    @Accessor("isSticky")
    boolean getSticky();
}
