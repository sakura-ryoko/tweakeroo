package fi.dy.masa.tweakeroo.mixin.option;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SimpleOption.class)
public interface IMixinSimpleOption<T>
{
    @Accessor("value")
    void tweakeroo_setValueWithoutCheck(T value);
}
