package fi.dy.masa.tweakeroo.data;

import fi.dy.masa.malilib.data.CachedBlockTags;
import fi.dy.masa.malilib.data.CachedItemTags;
import fi.dy.masa.malilib.data.CachedTagKey;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Caches Block/Item Tags as if they are real Vanilla Block/Item tags.
 */
public class CachedTagManager
{
	public static final CachedTagKey SILK_TOUCH_OVERRIDE_KEY    = new CachedTagKey(Reference.MOD_ID, "silk_touch_override");
	public static final CachedTagKey PICKAXE_OVERRIDE_KEY       = new CachedTagKey(Reference.MOD_ID, "pickaxe_override");
	public static final CachedTagKey NEEDS_PICKAXE_KEY          = new CachedTagKey(Reference.MOD_ID, "needs_pickaxe");
	public static final CachedTagKey NEEDS_SHEARS_KEY           = new CachedTagKey(Reference.MOD_ID, "needs_shears");
	public static final CachedTagKey NEEDS_SILK_TOUCH_KEY       = new CachedTagKey(Reference.MOD_ID, "needs_silk_touch");
	public static final CachedTagKey ORE_BLOCKS_KEY             = new CachedTagKey(Reference.MOD_ID, "ore_blocks");
	public static final CachedTagKey TRAPDOORS_KEY              = new CachedTagKey(Reference.MOD_ID, "trapdoors");
	public static final CachedTagKey STAIRS_KEY                 = new CachedTagKey(Reference.MOD_ID, "stairs");

    public static void startCache()
    {
        clearCache();

	    CachedBlockTags.getInstance().build(NEEDS_PICKAXE_KEY, buildNeedsPickaxeCache());
		CachedBlockTags.getInstance().build(NEEDS_SHEARS_KEY, buildNeedsShearsCache());
		CachedBlockTags.getInstance().build(NEEDS_SILK_TOUCH_KEY, buildNeedsSilkTouchCache());
		CachedBlockTags.getInstance().build(ORE_BLOCKS_KEY, buildOreBlocksCache());
	    CachedItemTags.getInstance().build(TRAPDOORS_KEY, buildTrapdoorItemsCache());
	    CachedItemTags.getInstance().build(STAIRS_KEY, buildStairItemsCache());
	}

	private static List<String> buildNeedsPickaxeCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#"+BlockTags.IMPERMEABLE.location().toString());
		// No Glass Pane block tag in Vanilla
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.GLASS_PANE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.black()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.blue()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.brown()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.cyan()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.gray()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.green()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.lightBlue()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.lightGray()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.lime()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.magenta()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.orange()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.pink()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.purple()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.red()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.yellow()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.white()).toString());
		// Others?
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.BEACON).toString());

		return list;
	}

	private static List<String> buildNeedsShearsCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#"+BlockTags.LEAVES.location().toString());
		list.add("#"+BlockTags.WOOL.location().toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.CAVE_VINES).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.CAVE_VINES_PLANT).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.COBWEB).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.DEAD_BUSH).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.FERN).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.GLOW_LICHEN).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.HANGING_ROOTS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.LARGE_FERN).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.NETHER_SPROUTS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.PALE_HANGING_MOSS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SHORT_GRASS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SHORT_DRY_GRASS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SEAGRASS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.TALL_GRASS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.TALL_DRY_GRASS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.TALL_SEAGRASS).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.TRIPWIRE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.TWISTING_VINES).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.TWISTING_VINES_PLANT).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.WEEPING_VINES).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.WEEPING_VINES_PLANT).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.VINE).toString());

		return list;
	}

	private static List<String> buildNeedsSilkTouchCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#"+BlockTags.IMPERMEABLE.location().toString());        // Glass Blocks
		list.add("#"+BlockTags.LEAVES.location().toString());             // All Leaves
		list.add("#"+BlockTags.CORALS.location().toString());             // Fans + Plants
		list.add("#"+BlockTags.WALL_CORALS.location().toString());        // Wall Coral Fans
		// No Glass Pane block tag in Vanilla
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.GLASS_PANE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.black()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.blue()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.brown()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.cyan()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.gray()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.green()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.lightBlue()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.lightGray()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.lime()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.magenta()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.orange()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.pink()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.purple()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.red()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.yellow()).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.STAINED_GLASS_PANE.white()).toString());
		// No Sculk Block Tags in Vanilla
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.CALIBRATED_SCULK_SENSOR).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SCULK).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SCULK_CATALYST).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SCULK_SENSOR).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SCULK_SHRIEKER).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SCULK_VEIN).toString());
		// Other Blocks
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.BEEHIVE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.BEE_NEST).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.BOOKSHELF).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.BLUE_ICE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.BUSH).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.CAMPFIRE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.CHISELED_BOOKSHELF).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.ENDER_CHEST).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.ICE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.PACKED_ICE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.SOUL_CAMPFIRE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.TURTLE_EGG).toString());

		return list;
	}

	private static List<String> buildOreBlocksCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#" + BlockTags.COPPER_ORES.location().toString());
		list.add("#" + BlockTags.GOLD_ORES.location().toString());
		list.add("#" + BlockTags.IRON_ORES.location().toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.COAL_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.DEEPSLATE_COAL_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.DEEPSLATE_DIAMOND_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.DEEPSLATE_EMERALD_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.DEEPSLATE_LAPIS_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.DEEPSLATE_REDSTONE_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.DIAMOND_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.EMERALD_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.LAPIS_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.REDSTONE_ORE).toString());
		list.add(BuiltInRegistries.BLOCK.getKey(Blocks.NETHER_QUARTZ_ORE).toString());

		return list;
	}

	private static List<String> buildTrapdoorItemsCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#" + ItemTags.WOODEN_TRAPDOORS.location().toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.IRON_TRAPDOOR).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.COPPER_TRAPDOOR.weathering().unaffected()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.COPPER_TRAPDOOR.weathering().exposed()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.COPPER_TRAPDOOR.weathering().weathered()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.COPPER_TRAPDOOR.weathering().oxidized()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.COPPER_TRAPDOOR.waxed().unaffected()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.COPPER_TRAPDOOR.waxed().exposed()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.COPPER_TRAPDOOR.waxed().weathered()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.COPPER_TRAPDOOR.waxed().oxidized()).toString());

		return list;
	}

	private static List<String> buildStairItemsCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#" + ItemTags.WOODEN_STAIRS.location().toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.COBBLESTONE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.STONE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.STONE_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.MOSSY_COBBLESTONE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.MOSSY_STONE_BRICK_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.COBBLED_DEEPSLATE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.DEEPSLATE_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.DEEPSLATE_TILE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_DEEPSLATE_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.TUFF_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.TUFF_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_TUFF_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.ANDESITE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.DIORITE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.GRANITE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_ANDESITE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_DIORITE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_GRANITE_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.CINNABAR_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.SULFUR_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.CINNABAR_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.SULFUR_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_CINNABAR_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_SULFUR_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.CUT_COPPER_STAIRS.weathering().unaffected()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.CUT_COPPER_STAIRS.weathering().exposed()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.CUT_COPPER_STAIRS.weathering().weathered()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.CUT_COPPER_STAIRS.weathering().oxidized()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.CUT_COPPER_STAIRS.waxed().unaffected()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.CUT_COPPER_STAIRS.waxed().exposed()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.CUT_COPPER_STAIRS.waxed().weathered()).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.CUT_COPPER_STAIRS.waxed().oxidized()).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.MUD_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.RESIN_BRICK_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.PRISMARINE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.PRISMARINE_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.DARK_PRISMARINE_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.SANDSTONE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.RED_SANDSTONE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.SMOOTH_SANDSTONE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.SMOOTH_RED_SANDSTONE_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.QUARTZ_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.SMOOTH_QUARTZ_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.BLACKSTONE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_BLACKSTONE_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.POLISHED_BLACKSTONE_BRICK_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.NETHER_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.RED_NETHER_BRICK_STAIRS).toString());

		list.add(BuiltInRegistries.ITEM.getKey(Items.END_STONE_BRICK_STAIRS).toString());
		list.add(BuiltInRegistries.ITEM.getKey(Items.PURPUR_STAIRS).toString());

		return list;
	}

	public static void parseSilkTouchOverride(List<String> configStrs)
	{
		if (Minecraft.getInstance().level == null)
		{
			return;
		}

		if (configStrs.isEmpty())
		{
			if (Configs.Generic.TOOL_SWAP_SILK_TOUCH_OVERRIDE.getBooleanValue())
			{
				Tweakeroo.LOGGER.error("parseSilkTouchOverride: Config List '{}' is empty.", Configs.Lists.SILK_TOUCH_OVERRIDE.getName());
			}

			return;
		}

		CachedBlockTags.getInstance().clearEntry(SILK_TOUCH_OVERRIDE_KEY);
		CachedBlockTags.getInstance().build(SILK_TOUCH_OVERRIDE_KEY, configStrs);
	}

	public static void parsePickaxeOverride(List<String> configStrs)
	{
		if (Minecraft.getInstance().level == null)
		{
			return;
		}

		if (configStrs.isEmpty())
		{
			if (Configs.Generic.TOOL_SWAP_PICKAXE_OVERRIDE.getBooleanValue())
			{
				Tweakeroo.LOGGER.error("parsePickaxeOverride: Config List '{}' is empty.", Configs.Lists.PICKAXE_OVERRIDE.getName());
			}

			return;
		}

		CachedBlockTags.getInstance().clearEntry(PICKAXE_OVERRIDE_KEY);
		CachedBlockTags.getInstance().build(PICKAXE_OVERRIDE_KEY, configStrs);
	}

	private static void clearCache()
    {
        CachedBlockTags.getInstance().clearEntry(SILK_TOUCH_OVERRIDE_KEY);
	    CachedBlockTags.getInstance().clearEntry(PICKAXE_OVERRIDE_KEY);
		CachedBlockTags.getInstance().clearEntry(NEEDS_SHEARS_KEY);
		CachedBlockTags.getInstance().clearEntry(NEEDS_SILK_TOUCH_KEY);
		CachedBlockTags.getInstance().clearEntry(ORE_BLOCKS_KEY);
	    CachedItemTags.getInstance().clearEntry(TRAPDOORS_KEY);
	    CachedItemTags.getInstance().clearEntry(STAIRS_KEY);
    }

	public static boolean isNeedsPickaxe(BlockState state)
	{
		return CachedBlockTags.getInstance().match(NEEDS_PICKAXE_KEY, state);
	}

	public static boolean isNeedsShears(BlockState state)
	{
		return CachedBlockTags.getInstance().match(NEEDS_SHEARS_KEY, state);
	}

	public static boolean isNeedsSilkTouch(BlockState state)
	{
		return CachedBlockTags.getInstance().match(NEEDS_SILK_TOUCH_KEY, state);
	}

	public static boolean isOreBlock(BlockState state)
	{
		return CachedBlockTags.getInstance().match(ORE_BLOCKS_KEY, state);
	}

	public static boolean isSilkTouchOverride(BlockState state)
	{
		return CachedBlockTags.getInstance().match(SILK_TOUCH_OVERRIDE_KEY, state);
	}

	public static boolean isPickaxeOverride(BlockState state)
	{
		return CachedBlockTags.getInstance().match(PICKAXE_OVERRIDE_KEY, state);
	}

	public static boolean isTrapdoor(ItemStack stack)
	{
		return CachedItemTags.getInstance().match(TRAPDOORS_KEY, stack.getItem());
	}

	public static boolean isStairs(ItemStack stack)
	{
		return CachedItemTags.getInstance().match(STAIRS_KEY, stack.getItem());
	}
}
