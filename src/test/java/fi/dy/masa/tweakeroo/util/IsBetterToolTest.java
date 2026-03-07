package fi.dy.masa.tweakeroo.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import fi.dy.masa.malilib.data.CachedBlockTags;
import fi.dy.masa.malilib.util.log.AnsiLogger;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.data.CachedTagManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for isBetterTool using the worst-possible-winner principle:
 * each test that expects true uses the weakest tool that should still win,
 * against the strongest tool that should still lose, isolating the specific
 * rule.
 * -
 * Rule chain order: anyTool -> [swordOnBamboo] -> [hoeOnLeaves] ->
 * [shearsOnNeedsShears] -> [pickaxeOnNeedsPickaxe] -> [pickaxeOverride] ->
 * [useFortune] -> [useSilkTouch] -> [silkTouchFirst] -> [silkTouchOres] ->
 * [silkTouchOverride] -> correctTool -> [betterEnchantments] -> betterMaterial
 * -> fasterTool
 */
@DisplayName("isBetterTool Method Integration Tests")
class IsBetterToolTest
{
        private static final AnsiLogger LOGGER = new AnsiLogger(IsBetterToolTest.class, true, true);
        private static final Map<ResourceKey<Enchantment>, Holder<Enchantment>> ENCHANT_HOLDERS = new HashMap<>();

        @BeforeAll
        static void setupMinecraft()
        {
                SharedConstants.tryDetectVersion();
                Bootstrap.bootStrap();
                bindVanillaBlockTagsForTests();
                registerTestEnchantments();
                initializeTagCacheForTests();
        }

        // ===== BLOCK TAG BINDING =====

        private static void bindVanillaBlockTagsForTests()
        {
                bindBlockTags(Blocks.STONE, BlockTags.MINEABLE_WITH_PICKAXE);
                bindBlockTags(Blocks.COBBLESTONE, BlockTags.MINEABLE_WITH_PICKAXE);
                bindBlockTags(Blocks.IRON_ORE, BlockTags.MINEABLE_WITH_PICKAXE);
                bindBlockTags(Blocks.COAL_ORE, BlockTags.MINEABLE_WITH_PICKAXE);
                bindBlockTags(Blocks.DIAMOND_ORE, BlockTags.MINEABLE_WITH_PICKAXE);
                bindBlockTags(Blocks.ENDER_CHEST, BlockTags.MINEABLE_WITH_PICKAXE);

                bindBlockTags(Blocks.OAK_LOG, BlockTags.MINEABLE_WITH_AXE, BlockTags.LOGS, BlockTags.LOGS_THAT_BURN);
                bindBlockTags(Blocks.BIRCH_LOG, BlockTags.MINEABLE_WITH_AXE, BlockTags.LOGS, BlockTags.LOGS_THAT_BURN);
                bindBlockTags(Blocks.OAK_PLANKS, BlockTags.MINEABLE_WITH_AXE, BlockTags.PLANKS);
                bindBlockTags(Blocks.CHEST, BlockTags.MINEABLE_WITH_AXE);

                bindBlockTags(Blocks.DIRT, BlockTags.MINEABLE_WITH_SHOVEL);
                bindBlockTags(Blocks.SAND, BlockTags.MINEABLE_WITH_SHOVEL);
                bindBlockTags(Blocks.GRAVEL, BlockTags.MINEABLE_WITH_SHOVEL);

                bindBlockTags(Blocks.OAK_LEAVES, BlockTags.MINEABLE_WITH_HOE, BlockTags.LEAVES);
                bindBlockTags(Blocks.SCULK_SENSOR, BlockTags.MINEABLE_WITH_HOE);

                bindBlockTags(Blocks.BAMBOO, BlockTags.MINEABLE_WITH_AXE, BlockTags.SWORD_EFFICIENT);
                bindBlockTags(Blocks.COBWEB, BlockTags.SWORD_EFFICIENT);
        }

        @SafeVarargs
        private static void bindBlockTags(Block block, TagKey<Block>... tags)
        {
                try
                {
                        var holder = block.builtInRegistryHolder;
                        var method = holder.getClass().getDeclaredMethod("bindTags", java.util.Collection.class);
                        method.setAccessible(true);
                        method.invoke(holder, List.of(tags));
                }
                catch (Exception e)
                {
                        throw new RuntimeException("Failed to bind tags for " + block, e);
                }
        }

        // ===== ENCHANTMENT REGISTRY =====

        /**
         * Create a minimal enchantment registry so we can enchant items in headless
         * tests. Enchantments are data-driven in 1.21+ and not populated by
         * Bootstrap.bootStrap(). We register dummy Enchantment objects with correct
         * ResourceKeys so that Holder.is(ResourceKey) returns true when EquipmentUtils
         * checks enchantment levels.
         */
        private static void registerTestEnchantments()
        {
                MappedRegistry<Enchantment> registry = new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());

                Enchantment.EnchantmentDefinition def = new Enchantment.EnchantmentDefinition(HolderSet.direct(), Optional.empty(), 1, 5, new Enchantment.Cost(1, 0), new Enchantment.Cost(1, 0), 1, List.of(EquipmentSlotGroup.MAINHAND));

                registerEnch(registry, Enchantments.SILK_TOUCH, "Silk Touch", def);
                registerEnch(registry, Enchantments.FORTUNE, "Fortune", def);
                registerEnch(registry, Enchantments.EFFICIENCY, "Efficiency", def);
                registerEnch(registry, Enchantments.MENDING, "Mending", def);
                registerEnch(registry, Enchantments.UNBREAKING, "Unbreaking", def);

                registry.freeze();
        }

        private static void registerEnch(MappedRegistry<Enchantment> registry, ResourceKey<Enchantment> key, String name, Enchantment.EnchantmentDefinition def)
        {
                Enchantment ench = new Enchantment(Component.literal(name), def, HolderSet.direct(), DataComponentMap.EMPTY);
                Holder.Reference<Enchantment> holder = registry.register(key, ench, RegistrationInfo.BUILT_IN);
                ENCHANT_HOLDERS.put(key, holder);
        }

        // ===== TAG CACHE =====

        private static void initializeTagCacheForTests()
        {
                CachedBlockTags tags = CachedBlockTags.getInstance();

                tags.clearEntry(CachedTagManager.NEEDS_PICKAXE_KEY);
                tags.clearEntry(CachedTagManager.NEEDS_SHEARS_KEY);
                tags.clearEntry(CachedTagManager.NEEDS_SILK_TOUCH_KEY);
                tags.clearEntry(CachedTagManager.ORE_BLOCKS_KEY);
                tags.clearEntry(CachedTagManager.SILK_TOUCH_OVERRIDE_KEY);
                tags.clearEntry(CachedTagManager.PICKAXE_OVERRIDE_KEY);

                tags.build(CachedTagManager.NEEDS_PICKAXE_KEY, List.of("minecraft:stone", "minecraft:cobblestone", "minecraft:glass", "minecraft:beacon"));
                tags.build(CachedTagManager.NEEDS_SHEARS_KEY, List.of("minecraft:oak_leaves", "minecraft:cobweb", "minecraft:white_wool", "minecraft:vine"));
                tags.build(CachedTagManager.NEEDS_SILK_TOUCH_KEY, List.of("minecraft:glass", "minecraft:ender_chest", "minecraft:sculk_sensor"));
                tags.build(CachedTagManager.ORE_BLOCKS_KEY, List.of("minecraft:coal_ore", "minecraft:iron_ore", "minecraft:diamond_ore"));
                tags.build(CachedTagManager.SILK_TOUCH_OVERRIDE_KEY, List.of("minecraft:stone", "minecraft:glass"));
                tags.build(CachedTagManager.PICKAXE_OVERRIDE_KEY, List.of("minecraft:stone", "minecraft:cobblestone", "minecraft:glass"));
        }

        // ===== ENCHANTMENT HELPERS =====

        private static ItemStack withEnchant(ItemStack stack, ResourceKey<Enchantment> key, int level)
        {
                stack.enchant(ENCHANT_HOLDERS.get(key), level);
                return stack;
        }

        /**
         * Best possible diamond pickaxe WITHOUT Fortune (for fortune-specific rule
         * tests).
         */
        private static ItemStack diamondPickNoFortune()
        {
                ItemStack s = new ItemStack(Items.DIAMOND_PICKAXE);
                withEnchant(s, Enchantments.SILK_TOUCH, 1);
                withEnchant(s, Enchantments.MENDING, 1);
                withEnchant(s, Enchantments.UNBREAKING, 3);
                return s;
        }

        /**
         * Best possible diamond pickaxe WITHOUT Silk Touch (for silk-touch-specific
         * rule tests).
         */
        private static ItemStack diamondPickNoSilkTouch()
        {
                ItemStack s = new ItemStack(Items.DIAMOND_PICKAXE);
                withEnchant(s, Enchantments.FORTUNE, 3);
                withEnchant(s, Enchantments.MENDING, 1);
                withEnchant(s, Enchantments.UNBREAKING, 3);
                return s;
        }

        /**
         * Best possible diamond hoe WITHOUT Fortune (for hoe + fortune preference
         * tests).
         */
        private static ItemStack diamondHoeNoFortune()
        {
                ItemStack s = new ItemStack(Items.DIAMOND_HOE);
                withEnchant(s, Enchantments.SILK_TOUCH, 1);
                withEnchant(s, Enchantments.MENDING, 1);
                withEnchant(s, Enchantments.UNBREAKING, 3);
                return s;
        }

        /**
         * Best possible diamond hoe WITHOUT Silk Touch (for hoe + silk touch preference
         * tests).
         */
        private static ItemStack diamondHoeNoSilkTouch()
        {
                ItemStack s = new ItemStack(Items.DIAMOND_HOE);
                withEnchant(s, Enchantments.FORTUNE, 3);
                withEnchant(s, Enchantments.MENDING, 1);
                withEnchant(s, Enchantments.UNBREAKING, 3);
                return s;
        }

        // ===== CONFIG HELPERS =====

        record ConfigTestCase(Map<String, Boolean> configFlags, ItemStack testedStack, ItemStack previousTool, BlockState state, boolean expectedBehavior, String description)
        {
                @Override
                public @NonNull String toString()
                {
                        return this.description;
                }
        }

        private static Map<String, Boolean> configToolSwap(String... enabledFlags)
        {
                String[] allToolSwapFlags =
                { "TOOL_SWAP_BETTER_ENCHANTS", "TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_PREFER_SILK_TOUCH", "TOOL_SWAP_BAMBOO_USES_SWORD_FIRST", "TOOL_SWAP_LEAVES_USES_HOE_FIRST", "TOOL_SWAP_NEEDS_SHEARS_FIRST", "TOOL_SWAP_NEEDS_PICKAXE_FIRST", "TOOL_SWAP_SILK_TOUCH_FIRST",
                                "TOOL_SWAP_SILK_TOUCH_ORES", "TOOL_SWAP_SILK_TOUCH_OVERRIDE", "TOOL_SWAP_PICKAXE_OVERRIDE" };

                Map<String, Boolean> configFlags = new HashMap<>();
                for (String flag : allToolSwapFlags)
                        configFlags.put(flag, false);
                for (String enabledFlag : enabledFlags)
                        configFlags.put(enabledFlag, true);
                return configFlags;
        }

        // ===== TEST EXECUTION =====

        @ParameterizedTest(name = "{0}")
        @MethodSource("toolConfigTestData")
        @DisplayName("isBetterTool method execution tests")
        void testIsBetterToolExecution(ConfigTestCase testCase)
        {
                assertNotNull(testCase.testedStack(), "Tested stack should be provided");
                assertNotNull(testCase.previousTool(), "Previous tool should be provided");
                assertNotNull(testCase.state(), "Block state should be provided");

                System.clearProperty("tweakeroo.debug.stdout");
                Configs.Generic.DEBUG_LOGGING.setBooleanValue(false);
                configureToolSwapFlags(testCase.configFlags());

                boolean result = assertDoesNotThrow(() -> InventoryUtils.isBetterTool(testCase.testedStack(), testCase.previousTool(), testCase.state()), testCase.description());
                assertEquals(testCase.expectedBehavior(), result, testCase.description());
        }

        private void configureToolSwapFlags(Map<String, Boolean> configFlags)
        {
                for (Map.Entry<String, Boolean> entry : configFlags.entrySet())
                {
                        try
                        {
                                switch (entry.getKey())
                                {
                                        case "TOOL_SWAP_BETTER_ENCHANTS" -> Configs.Generic.TOOL_SWAP_BETTER_ENCHANTS.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_PREFER_FORTUNE_OVERRIDE" -> Configs.Generic.TOOL_SWAP_PREFER_FORTUNE_OVERRIDE.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_PREFER_SILK_TOUCH" -> Configs.Generic.TOOL_SWAP_PREFER_SILK_TOUCH.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_BAMBOO_USES_SWORD_FIRST" -> Configs.Generic.TOOL_SWAP_BAMBOO_USES_SWORD_FIRST.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_LEAVES_USES_HOE_FIRST" -> Configs.Generic.TOOL_SWAP_LEAVES_USES_HOE_FIRST.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_NEEDS_SHEARS_FIRST" -> Configs.Generic.TOOL_SWAP_NEEDS_SHEARS_FIRST.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_NEEDS_PICKAXE_FIRST" -> Configs.Generic.TOOL_SWAP_NEEDS_PICKAXE_FIRST.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_SILK_TOUCH_FIRST" -> Configs.Generic.TOOL_SWAP_SILK_TOUCH_FIRST.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_SILK_TOUCH_ORES" -> Configs.Generic.TOOL_SWAP_SILK_TOUCH_ORES.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_SILK_TOUCH_OVERRIDE" -> Configs.Generic.TOOL_SWAP_SILK_TOUCH_OVERRIDE.setBooleanValue(entry.getValue());
                                        case "TOOL_SWAP_PICKAXE_OVERRIDE" -> Configs.Generic.TOOL_SWAP_PICKAXE_OVERRIDE.setBooleanValue(entry.getValue());
                                }
                        }
                        catch (Exception e)
                        {
                                LOGGER.warn("Warning: Could not set config flag {}: {}", entry.getKey(), e.getMessage());
                        }
                }
        }

        // ===== TEST DATA =====

        static Stream<Arguments> toolConfigTestData()
        {
                try
                {
                        BlockState stone = Blocks.STONE.defaultBlockState();
                        BlockState coalOre = Blocks.COAL_ORE.defaultBlockState();
                        BlockState oakLog = Blocks.OAK_LOG.defaultBlockState();
                        BlockState oakLeaves = Blocks.OAK_LEAVES.defaultBlockState();
                        BlockState dirt = Blocks.DIRT.defaultBlockState();
                        BlockState bamboo = Blocks.BAMBOO.defaultBlockState();
                        BlockState cobweb = Blocks.COBWEB.defaultBlockState();
                        BlockState enderChest = Blocks.ENDER_CHEST.defaultBlockState();
                        BlockState chest = Blocks.CHEST.defaultBlockState();
                        BlockState sculkSensor = Blocks.SCULK_SENSOR.defaultBlockState();
                        BlockState glass = Blocks.GLASS.defaultBlockState();
                        BlockState torch = Blocks.TORCH.defaultBlockState();

                        ItemStack emptyHand = ItemStack.EMPTY;
                        ItemStack stick = new ItemStack(Items.STICK);

                        return Stream.of(
                                        // ================================================================
                                        // anyTool — any tool beats a non-tool/empty hand
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_PICKAXE), stick, stone, true, "anyTool: worst tool (wooden pick) beats non-tool (stick) on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), stick, new ItemStack(Items.DIAMOND_PICKAXE), stone, false, "anyTool: non-tool (stick) can't beat best tool (diamond pick)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_PICKAXE), emptyHand, stone, true, "anyTool: worst tool (wooden pick) beats empty hand")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), emptyHand, new ItemStack(Items.DIAMOND_SWORD), stone, false, "anyTool: empty hand can't beat diamond sword")),

                                        // ================================================================
                                        // swordOnBamboo — worst sword beats best non-sword on bamboo
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BAMBOO_USES_SWORD_FIRST"), new ItemStack(Items.WOODEN_SWORD), new ItemStack(Items.DIAMOND_AXE), bamboo, true, "swordOnBamboo: worst sword (wooden) beats best axe (diamond)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BAMBOO_USES_SWORD_FIRST"), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.WOODEN_SWORD), bamboo, false, "swordOnBamboo: best axe (diamond) can't beat worst sword (wooden)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.WOODEN_SWORD), bamboo, true, "swordOnBamboo OFF: diamond axe beats wooden sword (axe is correct tool for bamboo)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_SWORD), new ItemStack(Items.DIAMOND_AXE), bamboo, false, "swordOnBamboo OFF: wooden sword can't beat diamond axe (axe is correct tool for bamboo)")),

                                        // ================================================================
                                        // hoeOnLeaves — worst hoe beats best non-hoe on leaves
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_LEAVES_USES_HOE_FIRST"), new ItemStack(Items.WOODEN_HOE), new ItemStack(Items.DIAMOND_AXE), oakLeaves, true, "hoeOnLeaves: worst hoe (wooden) beats best axe (diamond) on leaves")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_LEAVES_USES_HOE_FIRST"), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.WOODEN_HOE), oakLeaves, false, "hoeOnLeaves: best axe (diamond) can't beat worst hoe (wooden) on leaves")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.WOODEN_HOE), oakLeaves, false, "hoeOnLeaves OFF: diamond axe still can't beat wooden hoe (hoe is correct tool for leaves)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.WOODEN_HOE), oakLeaves, false, "hoeOnLeaves OFF: diamond pick still can't beat wooden hoe (hoe is correct tool for leaves)")),

                                        // ================================================================
                                        // shearsOnNeedsShears — shears beat best non-shears on shears blocks
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_NEEDS_SHEARS_FIRST"), new ItemStack(Items.SHEARS), new ItemStack(Items.DIAMOND_SWORD), cobweb, true, "shearsFirst: shears beat best sword (diamond) on cobweb")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_NEEDS_SHEARS_FIRST"), new ItemStack(Items.SHEARS), new ItemStack(Items.DIAMOND_AXE), oakLeaves, true, "shearsFirst: shears beat best axe (diamond) on leaves")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.SHEARS), cobweb, true, "shearsFirst OFF: diamond sword beats shears on cobweb (sword is correct tool, no shears override)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.SHEARS), new ItemStack(Items.DIAMOND_SWORD), cobweb, false, "shearsFirst OFF: shears can't beat diamond sword on cobweb (no shears override)")),

                                        // ================================================================
                                        // pickaxeOnNeedsPickaxe — worst pickaxe beats best non-pickaxe
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_NEEDS_PICKAXE_FIRST"), new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.DIAMOND_SWORD), stone, true, "pickaxeFirst: worst pick (wooden) beats best sword (diamond) on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.DIAMOND_SWORD), stone, true, "pickaxeFirst OFF: wooden pick still beats diamond sword on stone (pick is correct tool)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.WOODEN_PICKAXE), stone, false, "pickaxeFirst OFF: diamond sword can't beat wooden pick on stone (pick is correct tool)")),

                                        // ================================================================
                                        // pickaxeOverride — pickaxe beats non-pickaxe on override list blocks,
                                        // including blocks with no correct tool tag (e.g. glass)
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PICKAXE_OVERRIDE"), new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.DIAMOND_SWORD), stone, true, "pickaxeOverride: worst pick (wooden) beats best sword (diamond) on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PICKAXE_OVERRIDE"), new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.DIAMOND_SWORD), glass, true,
                                                        "pickaxeOverride: wooden pick beats diamond sword on glass (no tool tags, but in override list)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.DIAMOND_SWORD), stone, true, "pickaxeOverride OFF: wooden pick still beats diamond sword on stone (pick is correct tool)")),

                                        // ================================================================
                                        // useFortune — fortune pick beats non-fortune on correct tool,
                                        // but wrong tool type with fortune is ignored (combineRules)
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), coalOre, true,
                                                        "useFortune: wooden pick+Fortune I beats diamond pick+ST+Mend+Unbreak III on coal ore")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), new ItemStack(Items.DIAMOND_PICKAXE), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), coalOre, false,
                                                        "useFortune: diamond pick (no fortune) can't beat wooden pick+Fortune I on coal ore")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_AXE), Enchantments.FORTUNE, 3), new ItemStack(Items.WOODEN_PICKAXE), stone, false,
                                                        "useFortune: wooden axe+Fortune III can't beat wooden pick on stone (axe isn't correct tool)")),

                                        // ================================================================
                                        // useSilkTouch — ST pick beats non-ST on correct tool,
                                        // but wrong tool type with ST is ignored (combineRules)
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), stone, true,
                                                        "useSilkTouch: wooden pick+ST beats diamond pick+Fortune III+Mend+Unbreak III on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), stone, true,
                                                        "useSilkTouch OFF: diamond pick beats wooden pick+ST on stone (ST ignored, material wins)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(new ItemStack(Items.WOODEN_AXE), Enchantments.SILK_TOUCH, 1), new ItemStack(Items.WOODEN_PICKAXE), stone, false,
                                                        "useSilkTouch: wooden axe+ST can't beat wooden pick on stone (axe isn't correct tool)")),
                                        // BUG: useSilkTouch combined rule lets wrong-tool-type+ST override
                                        // correct-tool on non-silk-touch blocks like dirt.
                                        // combineRules passes through when correctTool returns true (tested
                                        // is correct), then useSilkTouchRule overrides with false because
                                        // previous has ST.
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(withEnchant(new ItemStack(Items.DIAMOND_SHOVEL), Enchantments.EFFICIENCY, 2), Enchantments.MENDING, 1),
                                                        withEnchant(withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.SILK_TOUCH, 1), Enchantments.UNBREAKING, 2), dirt, true,
                                                        "useSilkTouch: diamond shovel+Eff II+Mend should beat diamond pick+ST+Unbreak II on dirt (shovel is correct tool, dirt doesn't need ST)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.SILK_TOUCH, 1), Enchantments.UNBREAKING, 2),
                                                        withEnchant(withEnchant(new ItemStack(Items.DIAMOND_SHOVEL), Enchantments.EFFICIENCY, 2), Enchantments.MENDING, 1), dirt, false,
                                                        "useSilkTouch: diamond pick+ST+Unbreak II should not beat diamond shovel+Eff II+Mend on dirt (pick is wrong tool for dirt)")),

                                        // ================================================================
                                        // silkTouchFirst — ST tool beats non-ST on needs-silk-touch blocks.
                                        // Standalone rule (NOT combined with correctTool), so wrong tool
                                        // type with ST still wins.
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_FIRST"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), enderChest, true,
                                                        "silkTouchFirst: wooden pick+ST beats diamond pick+Fort+Mend+Unbreak on ender chest")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_FIRST"), new ItemStack(Items.DIAMOND_PICKAXE), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), enderChest, false,
                                                        "silkTouchFirst: diamond pick (no ST) can't beat wooden pick+ST on ender chest")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_FIRST"), withEnchant(new ItemStack(Items.WOODEN_AXE), Enchantments.SILK_TOUCH, 1), new ItemStack(Items.DIAMOND_PICKAXE), enderChest, true,
                                                        "silkTouchFirst: wooden axe+ST beats diamond pick on ender chest (standalone, wrong tool still wins)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), enderChest, true,
                                                        "silkTouchFirst OFF: diamond pick beats wooden pick+ST on ender chest (ST ignored, material wins)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_FIRST"), withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.SILK_TOUCH, 1), new ItemStack(Items.WOODEN_SHOVEL), dirt, false,
                                                        "silkTouchFirst: diamond pick+ST can't beat wooden shovel on dirt (dirt not in needs-silk-touch, shovel is correct tool)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_FIRST"), new ItemStack(Items.WOODEN_SHOVEL), withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.SILK_TOUCH, 1), dirt, true,
                                                        "silkTouchFirst: wooden shovel beats diamond pick+ST on dirt (dirt not in needs-silk-touch, shovel is correct tool)")),

                                        // ================================================================
                                        // silkTouchOres — ST tool beats non-ST on ore blocks.
                                        // Combined with correctTool, so wrong tool type with ST is blocked.
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_ORES"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), coalOre, true,
                                                        "silkTouchOres: wooden pick+ST beats diamond pick+Fort+Mend+Unbreak on coal ore")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_ORES"), withEnchant(new ItemStack(Items.WOODEN_AXE), Enchantments.SILK_TOUCH, 1), new ItemStack(Items.WOODEN_PICKAXE), coalOre, false,
                                                        "silkTouchOres: wooden axe+ST can't beat wooden pick on ore (combined with correctTool blocks wrong tool)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), coalOre, true,
                                                        "silkTouchOres OFF: diamond pick beats wooden pick+ST on ore (ST ignored, material wins)")),

                                        // ================================================================
                                        // silkTouchOverride — ST tool beats non-ST on override list blocks.
                                        // Standalone rule (NOT combined with correctTool), so wrong tool
                                        // type with ST still wins.
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), stone, true,
                                                        "silkTouchOverride: wooden pick+ST beats diamond pick+Fort+Mend+Unbreak on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_AXE), Enchantments.SILK_TOUCH, 1), new ItemStack(Items.DIAMOND_PICKAXE), stone, true,
                                                        "silkTouchOverride: wooden axe+ST beats diamond pick on stone (standalone, wrong tool still wins)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), stone, true,
                                                        "silkTouchOverride OFF: diamond pick beats wooden pick+ST on stone (ST ignored, material wins)")),

                                        // ================================================================
                                        // correctTool — worst correct tool beats best incorrect tool
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.DIAMOND_AXE), stone, true, "correctTool: worst correct (wooden pick) beats best incorrect (diamond axe) on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.WOODEN_PICKAXE), stone, false, "correctTool: best incorrect (diamond axe) can't beat worst correct (wooden pick) on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_SHOVEL), new ItemStack(Items.DIAMOND_PICKAXE), dirt, true, "correctTool: worst correct shovel (wooden) beats diamond pick on dirt")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_HOE), new ItemStack(Items.DIAMOND_PICKAXE), sculkSensor, true, "correctTool: worst correct hoe (wooden) beats diamond pick on sculk sensor")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_AXE), new ItemStack(Items.DIAMOND_PICKAXE), chest, true, "correctTool: worst correct axe (wooden) beats diamond pick on chest")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_PICKAXE), new ItemStack(Items.DIAMOND_AXE), oakLog, false, "correctTool: wooden pick (wrong) can't beat diamond axe (correct) on oak log")),

                                        // ================================================================
                                        // betterEnchantments — tool with more enchantments wins when enabled
                                        // (hasBetterToolEnchantments checks: MENDING, UNBREAKING, EFFICIENCY, FORTUNE)
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.EFFICIENCY, 1), new ItemStack(Items.DIAMOND_PICKAXE), stone, true,
                                                        "betterEnchants: wooden pick+Eff I beats unenchanted diamond pick on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.MENDING, 1), new ItemStack(Items.DIAMOND_PICKAXE), stone, true,
                                                        "betterEnchants: wooden pick+Mending beats unenchanted diamond pick")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.UNBREAKING, 1), new ItemStack(Items.DIAMOND_PICKAXE), stone, true,
                                                        "betterEnchants: wooden pick+Unbreaking I beats unenchanted diamond pick")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), new ItemStack(Items.DIAMOND_PICKAXE), stone, true,
                                                        "betterEnchants: wooden pick+Fortune I beats unenchanted diamond pick")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BETTER_ENCHANTS"), new ItemStack(Items.DIAMOND_PICKAXE), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.EFFICIENCY, 1), stone, false,
                                                        "betterEnchants: unenchanted diamond pick can't beat wooden pick+Eff I")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), withEnchant(new ItemStack(Items.IRON_PICKAXE), Enchantments.EFFICIENCY, 5), stone, true,
                                                        "betterEnchants OFF: diamond pick beats iron pick+Eff V (enchants ignored, material wins)")),

                                        // ================================================================
                                        // betterMaterial — minimal material advantage wins
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.STONE_PICKAXE), new ItemStack(Items.GOLDEN_PICKAXE), stone, true, "betterMaterial: stone pick (mat 2) beats gold pick (mat 1) — minimal advantage")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_PICKAXE), stone, true, "betterMaterial: diamond pick beats iron pick on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.GOLDEN_PICKAXE), stone, true, "betterMaterial: diamond pick beats gold pick on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.GOLDEN_PICKAXE), stone, true, "betterMaterial: iron pick beats gold pick on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.IRON_AXE), oakLog, true, "betterMaterial: diamond axe beats iron axe on oak log")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_SHOVEL), new ItemStack(Items.IRON_SHOVEL), dirt, true, "betterMaterial: diamond shovel beats iron shovel on dirt")),

                                        // ================================================================
                                        // fasterTool — speed wins when material is equal
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.GOLDEN_PICKAXE), new ItemStack(Items.STONE_PICKAXE), stone, false, "fasterTool: gold pick is faster but stone pick (mat 2) beats gold (mat 1) via material")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.GOLDEN_AXE), new ItemStack(Items.STONE_AXE), oakLog, false, "fasterTool: gold axe is faster but stone axe (mat 2) beats gold (mat 1) via material")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), withEnchant(new ItemStack(Items.GOLDEN_PICKAXE), Enchantments.EFFICIENCY, 5), withEnchant(new ItemStack(Items.GOLDEN_PICKAXE), Enchantments.EFFICIENCY, 1), stone, true,
                                                        "fasterTool: gold pick+Eff V beats gold pick+Eff I on stone (same material, speed wins)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), withEnchant(new ItemStack(Items.DIAMOND_AXE), Enchantments.EFFICIENCY, 5), withEnchant(new ItemStack(Items.DIAMOND_AXE), Enchantments.EFFICIENCY, 1), oakLog, true,
                                                        "fasterTool: diamond axe+Eff V beats diamond axe+Eff I on oak log (same material, speed wins)")),

                                        // ================================================================
                                        // Both tools have same enchantment — falls through to material/speed
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), withEnchant(new ItemStack(Items.STONE_PICKAXE), Enchantments.FORTUNE, 3), withEnchant(new ItemStack(Items.GOLDEN_PICKAXE), Enchantments.FORTUNE, 3), coalOre, true,
                                                        "sameFortune: both have Fortune III — stone pick (mat 2) beats gold pick (mat 1) via material")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(new ItemStack(Items.STONE_PICKAXE), Enchantments.SILK_TOUCH, 1), withEnchant(new ItemStack(Items.GOLDEN_PICKAXE), Enchantments.SILK_TOUCH, 1), stone, true,
                                                        "sameSilk: both have ST — stone pick (mat 2) beats gold pick (mat 1) via material")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.STONE_PICKAXE), Enchantments.EFFICIENCY, 5), withEnchant(new ItemStack(Items.GOLDEN_PICKAXE), Enchantments.EFFICIENCY, 5), stone, true,
                                                        "sameEnchants: both have Eff V — stone pick (mat 2) beats gold pick (mat 1) via material")),

                                        // ================================================================
                                        // Tool type priority — tool type rules fire BEFORE enchantment rules
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_LEAVES_USES_HOE_FIRST", "TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), new ItemStack(Items.WOODEN_HOE), withEnchant(new ItemStack(Items.DIAMOND_AXE), Enchantments.FORTUNE, 3), oakLeaves, true,
                                                        "hoe>fortune: wooden hoe beats diamond axe+Fortune III on leaves (hoe rule fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BAMBOO_USES_SWORD_FIRST", "TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), new ItemStack(Items.WOODEN_SWORD), withEnchant(new ItemStack(Items.DIAMOND_AXE), Enchantments.FORTUNE, 3), bamboo, true,
                                                        "sword>fortune: wooden sword beats diamond axe+Fortune III on bamboo (sword rule fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_NEEDS_SHEARS_FIRST", "TOOL_SWAP_PREFER_SILK_TOUCH"), new ItemStack(Items.SHEARS), withEnchant(new ItemStack(Items.DIAMOND_SWORD), Enchantments.SILK_TOUCH, 1), cobweb, true,
                                                        "shears>silk: shears beat diamond sword+ST on cobweb (shears rule fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_NEEDS_PICKAXE_FIRST", "TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), new ItemStack(Items.WOODEN_PICKAXE), withEnchant(new ItemStack(Items.DIAMOND_SWORD), Enchantments.FORTUNE, 3), stone, true,
                                                        "pick>fortune: wooden pick beats diamond sword+Fortune III on stone (pickaxe rule fires first)")),

                                        // ================================================================
                                        // Enchantment preference within same tool type — when both tools
                                        // match the tool-type rule, it returns null and enchantment rules decide
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_LEAVES_USES_HOE_FIRST", "TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_HOE), Enchantments.FORTUNE, 1), diamondHoeNoFortune(), oakLeaves, true,
                                                        "hoe+fortune: wooden hoe+Fortune I beats diamond hoe+ST+Mend+Unbreak on leaves")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_LEAVES_USES_HOE_FIRST", "TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(new ItemStack(Items.WOODEN_HOE), Enchantments.SILK_TOUCH, 1), diamondHoeNoSilkTouch(), oakLeaves, true,
                                                        "hoe+silk: wooden hoe+ST beats diamond hoe+Fort+Mend+Unbreak on leaves")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_NEEDS_PICKAXE_FIRST", "TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), stone, true,
                                                        "pick+fortune: wooden pick+Fortune I beats diamond pick+ST+Mend+Unbreak on stone")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_NEEDS_PICKAXE_FIRST", "TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), stone, true,
                                                        "pick+silk: wooden pick+ST beats diamond pick+Fort+Mend+Unbreak on stone")),

                                        // ================================================================
                                        // Fortune vs silk touch priority — useFortune fires before
                                        // useSilkTouch in the chain
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), coalOre, true,
                                                        "fortune>silk: wooden pick+Fortune I beats diamond pick+ST on ore (fortune fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_PREFER_SILK_TOUCH"), diamondPickNoFortune(), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), coalOre, false,
                                                        "fortune>silk: diamond pick+ST can't beat wooden pick+Fortune on ore (fortune fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), coalOre, true,
                                                        "silk-only: wooden pick+ST beats diamond pick+Fortune on ore (no fortune config)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), stone, true,
                                                        "fortune-only: wooden pick+Fortune I beats diamond pick+ST on stone")),

                                        // ================================================================
                                        // Fortune vs silkTouchFirst — useFortune fires before silkTouchFirst
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_SILK_TOUCH_FIRST"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), enderChest, true,
                                                        "fortune>stFirst: wooden pick+Fortune beats diamond pick+ST on ender chest (fortune fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_SILK_TOUCH_FIRST"), diamondPickNoFortune(), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), enderChest, false,
                                                        "fortune>stFirst: diamond pick+ST can't beat wooden pick+Fortune on ender chest")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_FIRST"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), enderChest, true,
                                                        "stFirst-only: wooden pick+ST beats diamond pick+Fortune on ender chest")),

                                        // ================================================================
                                        // Fortune vs silkTouchOres — useFortune fires before silkTouchOres
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_SILK_TOUCH_ORES"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), coalOre, true,
                                                        "fortune>stOres: wooden pick+Fortune beats diamond pick+ST on ore (fortune fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_ORES"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), coalOre, true,
                                                        "stOres-only: wooden pick+ST beats diamond pick+Fortune on ore")),

                                        // ================================================================
                                        // Fortune vs silkTouchOverride — useFortune fires before silkTouchOverride
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_SILK_TOUCH_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), stone, true,
                                                        "fortune>stOverride: wooden pick+Fortune beats diamond pick+ST on stone (fortune fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), diamondPickNoSilkTouch(), stone, true,
                                                        "stOverride-only: wooden pick+ST beats diamond pick+Fortune on stone")),

                                        // ================================================================
                                        // All silk touch configs — earliest matching rule decides
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH", "TOOL_SWAP_SILK_TOUCH_FIRST", "TOOL_SWAP_SILK_TOUCH_ORES", "TOOL_SWAP_SILK_TOUCH_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1),
                                                        diamondPickNoSilkTouch(), coalOre, true, "allSilk: wooden pick+ST beats diamond pick+Fortune on ore (all silk configs on)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH", "TOOL_SWAP_SILK_TOUCH_FIRST", "TOOL_SWAP_SILK_TOUCH_ORES", "TOOL_SWAP_SILK_TOUCH_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1),
                                                        diamondPickNoSilkTouch(), enderChest, true, "allSilk: wooden pick+ST beats diamond pick+Fortune on ender chest")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_SILK_TOUCH", "TOOL_SWAP_SILK_TOUCH_FIRST", "TOOL_SWAP_SILK_TOUCH_ORES", "TOOL_SWAP_SILK_TOUCH_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1),
                                                        diamondPickNoSilkTouch(), stone, true, "allSilk: wooden pick+ST beats diamond pick+Fortune on stone (override block)")),

                                        // ================================================================
                                        // All configs — fortune takes priority over all silk touch configs
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_PREFER_SILK_TOUCH", "TOOL_SWAP_SILK_TOUCH_FIRST", "TOOL_SWAP_SILK_TOUCH_ORES", "TOOL_SWAP_SILK_TOUCH_OVERRIDE"),
                                                        withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), coalOre, true, "allConfigs: fortune wins over all silk configs on ore")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_PREFER_SILK_TOUCH", "TOOL_SWAP_SILK_TOUCH_FIRST", "TOOL_SWAP_SILK_TOUCH_ORES", "TOOL_SWAP_SILK_TOUCH_OVERRIDE"),
                                                        withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), enderChest, true, "allConfigs: fortune wins over all silk configs on ender chest")),

                                        // ================================================================
                                        // Multi-config interactions
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_LEAVES_USES_HOE_FIRST", "TOOL_SWAP_NEEDS_SHEARS_FIRST"), new ItemStack(Items.WOODEN_HOE), new ItemStack(Items.SHEARS), oakLeaves, true,
                                                        "combined: hoeOnLeaves fires before shearsFirst — wooden hoe beats shears on leaves")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_BAMBOO_USES_SWORD_FIRST"), new ItemStack(Items.STONE_SWORD), new ItemStack(Items.GOLDEN_SWORD), bamboo, true,
                                                        "combined: swordOnBamboo then betterMaterial — stone sword (mat 2) beats gold sword (mat 1)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.WOODEN_HOE), new ItemStack(Items.DIAMOND_AXE), oakLeaves, true, "combined: without hoe config, hoe still wins via correctTool on leaves")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.FORTUNE, 1), diamondPickNoFortune(), coalOre, true,
                                                        "combined: fortune fires first — wooden pick+Fortune I beats diamond pick+ST+Mend+Unbreak on ore")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_SILK_TOUCH_FIRST", "TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1),
                                                        withEnchant(withEnchant(withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.FORTUNE, 3), Enchantments.MENDING, 1), Enchantments.UNBREAKING, 3), enderChest, true,
                                                        "combined: wooden pick+ST beats diamond pick+Fort III+Mend+Unbreak III on ender chest (stFirst fires first)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PICKAXE_OVERRIDE", "TOOL_SWAP_SILK_TOUCH_OVERRIDE"), withEnchant(new ItemStack(Items.WOODEN_PICKAXE), Enchantments.SILK_TOUCH, 1), new ItemStack(Items.DIAMOND_SWORD), stone, true,
                                                        "combined: pickOverride+stOverride — wooden pick+ST beats diamond sword on stone")),

                                        // ================================================================
                                        // Fortune level comparison — Fortune III vs Fortune I
                                        // useFortuneRule only checks hasFortune (boolean), so when both
                                        // have fortune it returns null. betterEnchants via
                                        // hasSameOrBetterEnchantment compares levels (3-1=2 > 0).
                                        // Without betterEnchants, both diamond picks are identical.
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.FORTUNE, 3),
                                                        withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.FORTUNE, 1), coalOre, true, "fortuneLevel: diamond pick+Fortune III beats diamond pick+Fortune I (betterEnchants compares levels)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE", "TOOL_SWAP_BETTER_ENCHANTS"), withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.FORTUNE, 1),
                                                        withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.FORTUNE, 3), coalOre, false, "fortuneLevel: diamond pick+Fortune I can't beat diamond pick+Fortune III (betterEnchants compares levels)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap("TOOL_SWAP_PREFER_FORTUNE_OVERRIDE"), withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.FORTUNE, 3), withEnchant(new ItemStack(Items.DIAMOND_PICKAXE), Enchantments.FORTUNE, 1), coalOre, false,
                                                        "fortuneLevel: diamond pick+Fortune III does NOT beat Fortune I without betterEnchants (no level comparison)")),

                                        // ================================================================
                                        // noToolTag — blocks with no mineable tool tag (e.g. torch).
                                        // betterMaterial should NOT select a tool over a non-tool or
                                        // empty hand, since no tool is actually needed.
                                        // ================================================================
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), emptyHand, torch, false, "noToolTag: diamond pick should not beat empty hand on torch (no tool needed)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), stick, torch, false, "noToolTag: diamond pick should not beat stick on torch (no tool needed)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_SHOVEL), emptyHand, torch, false, "noToolTag: diamond shovel should not beat empty hand on torch (no tool needed)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), emptyHand, new ItemStack(Items.DIAMOND_PICKAXE), torch, false, "noToolTag: empty hand should not beat diamond pick on torch (neither is correct, no swap)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.IRON_PICKAXE), torch, false, "noToolTag: diamond pick should not beat iron pick on torch (neither is correct, no swap)")),
                                        Arguments.of(new ConfigTestCase(configToolSwap(), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.WOODEN_SHOVEL), torch, false, "noToolTag: diamond pick should not beat wooden shovel on torch (neither is correct, no swap)")));
                }
                catch (Exception e)
                {
                        LOGGER.error("Failed to create test data: {}", e.getMessage());
                        return Stream.empty();
                }
        }
}
