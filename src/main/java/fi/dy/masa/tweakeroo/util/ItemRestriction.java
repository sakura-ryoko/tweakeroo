package fi.dy.masa.tweakeroo.util;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction.ListType;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class ItemRestriction
{
    private ListType type = ListType.NONE;
    private final HashSet<Item> blackList = new HashSet<>();
    private final HashSet<Item> whiteList = new HashSet<>();

    public void setValues(ListType type, List<String> namesBlacklist, List<String> namesWhitelist)
    {
        this.type = type;
        this.setValuesForList(ListType.BLACKLIST, namesBlacklist);
        this.setValuesForList(ListType.WHITELIST, namesWhitelist);
    }

    protected void setValuesForList(ListType type, List<String> names)
    {
        HashSet<Item> set = type == ListType.WHITELIST ? this.whiteList : this.blackList;
        set.clear();

        for (String name : names)
        {
            try
            {
                //Item item = Registries.ITEM.get(Identifier.tryParse(name));
	            Identifier id = Identifier.tryParse(name);

				if (id != null)
				{
					Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(id);

					if (opt.isPresent() && opt.get().value() != Items.AIR)
					{
						set.add(opt.get().value());
					}
					else
					{
						Tweakeroo.LOGGER.warn("Invalid item name in a black- or whitelist: '{}", name);
					}
				}
            }
            catch (Exception e)
            {
                Tweakeroo.LOGGER.warn("Invalid item name in a black- or whitelist: '{}", name, e);
            }
        }
    }

    public boolean isItemAllowed(ItemStack stack)
    {
	    return switch (this.type)
	    {
		    case BLACKLIST -> !this.blackList.contains(stack.getItem());
		    case WHITELIST -> this.whiteList.contains(stack.getItem());
		    default -> true;
	    };
    }
}
