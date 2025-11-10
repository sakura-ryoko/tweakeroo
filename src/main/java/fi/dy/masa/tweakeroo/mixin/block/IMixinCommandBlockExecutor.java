package fi.dy.masa.tweakeroo.mixin.block;

import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseCommandBlock.class)
public interface IMixinCommandBlockExecutor
{
    @Accessor("updateLastExecution")
    boolean getUpdateLastExecution();

    @Accessor("updateLastExecution")
    void setUpdateLastExecution(boolean value);
}
