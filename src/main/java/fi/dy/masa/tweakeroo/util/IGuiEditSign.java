package fi.dy.masa.tweakeroo.util;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

public interface IGuiEditSign
{
    SignBlockEntity tweakeroo$getTile();

    void tweakeroo$applyText(SignText text);
}
