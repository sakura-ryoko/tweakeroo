package fi.dy.masa.tweakeroo.mixin.option;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = OptionInstance.class, priority = 1010)
public interface IMixinSimpleOption<T>
{
    @Accessor("value")
    void tweakeroo_setValueWithoutCheck(T value);

    @Accessor("value")
    T tweakeroo_getValueWithoutCheck();
}
