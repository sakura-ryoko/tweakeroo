package fi.dy.masa.tweakeroo.util;

import net.minecraft.item.ItemStack;

public interface IItemStackLimit
{
    Integer getMaxStackSize(ItemStack stack);
}
