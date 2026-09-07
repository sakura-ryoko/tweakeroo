package fi.dy.masa.tweakeroo.util;

import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;

public interface ISignTextAccess
{
    SignText tweakeroo$getText(SignTextSlot slot);
}
