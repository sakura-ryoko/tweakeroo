package fi.dy.masa.tweakeroo.data;

import fi.dy.masa.malilib.data.CachedBlockTags;
import fi.dy.masa.malilib.data.CachedTagKey;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;

import java.util.ArrayList;
import java.util.List;

/**
 * Caches Block/Item Tags as if they are real Vanilla Block/Item tags.
 */
public class CachedTagManager
{
	public static final CachedTagKey SILK_TOUCH_OVERRIDE_KEY    = new CachedTagKey(Reference.MOD_ID, "silk_touch_override");
	public static final CachedTagKey NEEDS_SHEARS_KEY           = new CachedTagKey(Reference.MOD_ID, "needs_shears");
	public static final CachedTagKey NEEDS_SILK_TOUCH_KEY       = new CachedTagKey(Reference.MOD_ID, "needs_silk_touch");
	public static final CachedTagKey ORE_BLOCKS_KEY             = new CachedTagKey(Reference.MOD_ID, "ore_blocks");

    public static void startCache()
    {
        clearCache();

		CachedBlockTags.getInstance().build(NEEDS_SHEARS_KEY, buildNeedsShearsCache());
		CachedBlockTags.getInstance().build(NEEDS_SILK_TOUCH_KEY, buildNeedsSilkTouchCache());
		CachedBlockTags.getInstance().build(ORE_BLOCKS_KEY, buildOreBlocksCache());
	}

	private static List<String> buildNeedsShearsCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#"+BlockTags.LEAVES.id().toString());
		list.add("#"+BlockTags.WOOL.id().toString());
		list.add(Registries.BLOCK.getId(Blocks.CAVE_VINES).toString());
		list.add(Registries.BLOCK.getId(Blocks.CAVE_VINES_PLANT).toString());
		list.add(Registries.BLOCK.getId(Blocks.COBWEB).toString());
		list.add(Registries.BLOCK.getId(Blocks.DEAD_BUSH).toString());
		list.add(Registries.BLOCK.getId(Blocks.FERN).toString());
		list.add(Registries.BLOCK.getId(Blocks.GLOW_LICHEN).toString());
		list.add(Registries.BLOCK.getId(Blocks.HANGING_ROOTS).toString());
		list.add(Registries.BLOCK.getId(Blocks.LARGE_FERN).toString());
		list.add(Registries.BLOCK.getId(Blocks.NETHER_SPROUTS).toString());
		list.add(Registries.BLOCK.getId(Blocks.PALE_HANGING_MOSS).toString());
		list.add(Registries.BLOCK.getId(Blocks.SHORT_GRASS).toString());
		list.add(Registries.BLOCK.getId(Blocks.SHORT_DRY_GRASS).toString());
		list.add(Registries.BLOCK.getId(Blocks.SEAGRASS).toString());
		list.add(Registries.BLOCK.getId(Blocks.TALL_GRASS).toString());
		list.add(Registries.BLOCK.getId(Blocks.TALL_DRY_GRASS).toString());
		list.add(Registries.BLOCK.getId(Blocks.TALL_SEAGRASS).toString());
		list.add(Registries.BLOCK.getId(Blocks.TRIPWIRE).toString());
		list.add(Registries.BLOCK.getId(Blocks.TWISTING_VINES).toString());
		list.add(Registries.BLOCK.getId(Blocks.TWISTING_VINES_PLANT).toString());
		list.add(Registries.BLOCK.getId(Blocks.WEEPING_VINES).toString());
		list.add(Registries.BLOCK.getId(Blocks.WEEPING_VINES_PLANT).toString());
		list.add(Registries.BLOCK.getId(Blocks.VINE).toString());

		return list;
	}

	private static List<String> buildNeedsSilkTouchCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#"+BlockTags.IMPERMEABLE.id().toString());        // Glass Blocks
		list.add("#"+BlockTags.LEAVES.id().toString());             // All Leaves
		list.add("#"+BlockTags.CORALS.id().toString());             // Fans + Plants
		list.add("#"+BlockTags.WALL_CORALS.id().toString());        // Wall Coral Fans
		// No Glass Pane block tag in Vanilla
		list.add(Registries.BLOCK.getId(Blocks.GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.BLACK_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.BLUE_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.BROWN_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.CYAN_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.GRAY_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.GREEN_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.LIME_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.MAGENTA_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.ORANGE_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.PINK_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.PURPLE_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.RED_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.YELLOW_STAINED_GLASS_PANE).toString());
		list.add(Registries.BLOCK.getId(Blocks.WHITE_STAINED_GLASS_PANE).toString());
		// No Sculk Block Tags in Vanilla
		list.add(Registries.BLOCK.getId(Blocks.CALIBRATED_SCULK_SENSOR).toString());
		list.add(Registries.BLOCK.getId(Blocks.SCULK).toString());
		list.add(Registries.BLOCK.getId(Blocks.SCULK_CATALYST).toString());
		list.add(Registries.BLOCK.getId(Blocks.SCULK_SENSOR).toString());
		list.add(Registries.BLOCK.getId(Blocks.SCULK_SHRIEKER).toString());
		list.add(Registries.BLOCK.getId(Blocks.SCULK_VEIN).toString());
		// Other Blocks
		list.add(Registries.BLOCK.getId(Blocks.BEEHIVE).toString());
		list.add(Registries.BLOCK.getId(Blocks.BEE_NEST).toString());
		list.add(Registries.BLOCK.getId(Blocks.BOOKSHELF).toString());
		list.add(Registries.BLOCK.getId(Blocks.BLUE_ICE).toString());
		list.add(Registries.BLOCK.getId(Blocks.BUSH).toString());
		list.add(Registries.BLOCK.getId(Blocks.CAMPFIRE).toString());
		list.add(Registries.BLOCK.getId(Blocks.CHISELED_BOOKSHELF).toString());
		list.add(Registries.BLOCK.getId(Blocks.ENDER_CHEST).toString());
		list.add(Registries.BLOCK.getId(Blocks.ICE).toString());
		list.add(Registries.BLOCK.getId(Blocks.PACKED_ICE).toString());
		list.add(Registries.BLOCK.getId(Blocks.SOUL_CAMPFIRE).toString());
		list.add(Registries.BLOCK.getId(Blocks.TURTLE_EGG).toString());

		return list;
	}

	private static List<String> buildOreBlocksCache()
	{
		List<String> list = new ArrayList<>();

		list.add("#" + BlockTags.COAL_ORES.id().toString());
		list.add("#" + BlockTags.COPPER_ORES.id().toString());
		list.add("#" + BlockTags.DIAMOND_ORES.id().toString());
		list.add("#" + BlockTags.EMERALD_ORES.id().toString());
		list.add("#" + BlockTags.GOLD_ORES.id().toString());
		list.add("#" + BlockTags.IRON_ORES.id().toString());
		list.add("#" + BlockTags.LAPIS_ORES.id().toString());
		list.add("#" + BlockTags.REDSTONE_ORES.id().toString());
		list.add(Registries.BLOCK.getId(Blocks.NETHER_QUARTZ_ORE).toString());

		return list;
	}

	public static void parseSilkTouchOverride(List<String> configStrs)
	{
		if (MinecraftClient.getInstance().world == null)
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

	private static void clearCache()
    {
        CachedBlockTags.getInstance().clearEntry(SILK_TOUCH_OVERRIDE_KEY);
		CachedBlockTags.getInstance().clearEntry(NEEDS_SHEARS_KEY);
		CachedBlockTags.getInstance().clearEntry(NEEDS_SILK_TOUCH_KEY);
		CachedBlockTags.getInstance().clearEntry(ORE_BLOCKS_KEY);
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
}
