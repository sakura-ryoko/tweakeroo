package fi.dy.masa.tweakeroo.util;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;

public interface IGuiEditSign
{
    SignBlockEntity tweakeroo$getTile();

    void tweakeroo$applyText(SignText text);
}
