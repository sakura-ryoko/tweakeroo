package fi.dy.masa.tweakeroo.util;

import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.ArrayListMultimap;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.InfestedBlock;

import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class CreativeExtraItems
{
    private static final ArrayListMultimap<CreativeModeTab, ItemStack> ADDED_ITEMS = ArrayListMultimap.create();
    private static final HashMap<Item, CreativeModeTab> OVERRIDDEN_GROUPS = new HashMap<>();

    @Nullable
    public static CreativeModeTab getGroupFor(Item item)
    {
        return OVERRIDDEN_GROUPS.get(item);
    }

    public static List<ItemStack> getExtraStacksForGroup(CreativeModeTab group)
    {
        return ADDED_ITEMS.get(group);
    }

    public static void setCreativeExtraItems(List<String> items)
    {
        // The references are private without Fabric API module fabric-item-group-api-v1
        // So use an ugly workaround for now to find the correct group, to avoid the API
        // dependency and an extra Mixin accessor.
        // TODO 1.19.3+ ?
        for (CreativeModeTab group : CreativeModeTabs.allTabs())
        {
            ComponentContents content = group.getDisplayName().getContents();

            if (content instanceof TranslatableContents translatableTextContent &&
                translatableTextContent.getKey().equals("itemGroup.op"))
            {
                setCreativeExtraItems(group, items);
                break;
            }
        }
    }

    private static void setCreativeExtraItems(CreativeModeTab group, List<String> items)
    {
        ADDED_ITEMS.clear();
        OVERRIDDEN_GROUPS.clear();

        if (items.isEmpty())
        {
            return;
        }

        Tweakeroo.LOGGER.info("Adding extra items to creative inventory group '{}'", group.getDisplayName().getString());

        for (String str : items)
        {
            ItemStack stack = InventoryUtils.getItemStackFromString(str);

            if (stack != null && stack.isEmpty() == false)
            {
                if (stack.getComponents().isEmpty() == false)
                {
                    ADDED_ITEMS.put(group, stack);
                }
                else
                {
                    OVERRIDDEN_GROUPS.put(stack.getItem(), group);
                }
            }
        }
    }

    public static void removeInfestedBlocks(NonNullList<ItemStack> stacks)
    {
        stacks.removeIf((stack) -> stack.getItem() instanceof BlockItem &&
                                   ((BlockItem) stack.getItem()).getBlock() instanceof InfestedBlock);
    }
}
