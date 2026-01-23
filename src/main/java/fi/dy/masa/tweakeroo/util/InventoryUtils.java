package fi.dy.masa.tweakeroo.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.tuple.Pair;
import fi.dy.masa.malilib.data.CachedTagUtils;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.EquipmentUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.CachedTagManager;
import fi.dy.masa.tweakeroo.mixin.block.IMixinAbstractBlock;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;

public class InventoryUtils
{
    private static final List<EquipmentSlot> REPAIR_MODE_SLOTS = new ArrayList<>();
    private static final List<Integer> REPAIR_MODE_SLOT_NUMBERS = new ArrayList<>();
    private static final HashSet<Item> UNSTACKING_ITEMS = new HashSet<>();
    private static final List<Integer> TOOL_SWITCHABLE_SLOTS = new ArrayList<>();
    private static final List<Integer> TOOL_SWITCH_IGNORED_SLOTS = new ArrayList<>();
    private static final HashMap<EntityType<?>, HashSet<Item>> WEAPON_MAPPING = new HashMap<>();
//    private static boolean needsCache = false;

    public static void setToolSwitchableSlots(String configStr)
    {
        parseSlotsFromString(configStr, TOOL_SWITCHABLE_SLOTS);
    }

    public static void setToolSwitchIgnoreSlots(String configStr)
    {
        parseSlotsFromString(configStr, TOOL_SWITCH_IGNORED_SLOTS);
    }

    public static void parseSlotsFromString(String configStr, Collection<Integer> output)
    {
        String[] parts = configStr.split(",");
        Pattern patternRange = Pattern.compile("^(?<start>[0-9])-(?<end>[0-9])$");

        output.clear();

        if (configStr.isBlank())
        {
            return;
        }

        for (String str : parts)
        {
            try
            {
                Matcher matcher = patternRange.matcher(str);

                if (matcher.matches())
                {
                    int slotStart = Integer.parseInt(matcher.group("start")) - 1;
                    int slotEnd = Integer.parseInt(matcher.group("end")) - 1;

                    if (slotStart <= slotEnd &&
                        Inventory.isHotbarSlot(slotStart) &&
                        Inventory.isHotbarSlot(slotEnd))
                    {
                        for (int slotNum = slotStart; slotNum <= slotEnd; ++slotNum)
                        {
                            if (output.contains(slotNum) == false)
                            {
                                output.add(slotNum);
                            }
                        }
                    }
                }
                else
                {
                    int slotNum = Integer.parseInt(str) - 1;

                    if (Inventory.isHotbarSlot(slotNum) && output.contains(slotNum) == false)
                    {
                        output.add(slotNum);
                    }
                }
            }
            catch (NumberFormatException ignore)
            {
                InfoUtils.showGuiOrInGameMessage(Message.MessageType.ERROR, "Failed to parse slots from string %s", configStr);
            }
        }
    }

    public static void setUnstackingItems(List<String> names)
    {
        UNSTACKING_ITEMS.clear();

        for (String name : names)
        {
            try
            {
                //Item item = Registries.ITEM.get(Identifier.tryParse(name));
                Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(Identifier.tryParse(name));

                if (opt.isPresent() && opt.get().value() != Items.AIR)
                {
                    UNSTACKING_ITEMS.add(opt.get().value());
                }
            }
            catch (Exception e)
            {
                Tweakeroo.LOGGER.warn("Failed to set an unstacking protected item from name '{}'", name, e);
            }
        }
    }

    public static void setRepairModeSlots(List<String> names)
    {
        REPAIR_MODE_SLOTS.clear();
        REPAIR_MODE_SLOT_NUMBERS.clear();

        for (String name : names)
        {
            EquipmentSlot type = switch (name)
			{
				case "mainhand" -> EquipmentSlot.MAINHAND;
				case "offhand" -> EquipmentSlot.OFFHAND;
				case "head" -> EquipmentSlot.HEAD;
				case "chest" -> EquipmentSlot.CHEST;
				case "legs" -> EquipmentSlot.LEGS;
				case "feet" -> EquipmentSlot.FEET;
				default -> null;
			};

			if (type != null)
            {
                REPAIR_MODE_SLOTS.add(type);

                int slotNum = getSlotNumberForEquipmentType(type, null);

                if (slotNum >= 0)
                {
                    REPAIR_MODE_SLOT_NUMBERS.add(slotNum);
                }
            }
        }
    }

    public static void setWeaponMapping(List<String> mappings)
    {
        WEAPON_MAPPING.clear();

        for (String mapping : mappings)
        {
            String[] split = mapping.replaceAll(" ", "").split("=>");

            if (split.length != 2)
            {
                Tweakeroo.LOGGER.warn("Expected weapon mapping to be `entity_ids => weapon_ids` got '{}'", mapping);
                continue;
            }

            HashSet<Item> weapons = new HashSet<>();
            String entities = split[0].trim();
            String items = split[1].trim();

            if (items.equals("<ignore>") == false)
            {
                for (String itemId : items.split(","))
                {
                    try
                    {
                        Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(Identifier.tryParse(itemId));

                        if (opt.isPresent())
                        {
                            weapons.add(opt.get().value());
                            continue;
                        }
                    }
                    catch (Exception ignore) {}

                    Tweakeroo.LOGGER.warn("Unable to find item to use as weapon: '{}'", itemId);
                }
            }

            if (entities.equalsIgnoreCase("<default>"))
            {
                WEAPON_MAPPING.computeIfAbsent(null, s -> new HashSet<>()).addAll(weapons);
            }
            else
            {
                for (String entity_id : entities.split(","))
                {
                    try
                    {
                        Optional<Holder.Reference<EntityType<?>>> opt = BuiltInRegistries.ENTITY_TYPE.get(Identifier.tryParse(entity_id));

                        if (opt.isPresent())
                        {
                            WEAPON_MAPPING.computeIfAbsent(opt.get().value(), s -> new HashSet<>()).addAll(weapons);
                            continue;
                        }
                    }
                    catch (Exception ignore) {}

                    Tweakeroo.LOGGER.warn("Unable to find entity: '{}'", entity_id);
                }
            }
        }
    }

    private static boolean isConfiguredRepairSlot(int slotNum, Player player)
    {
        if (REPAIR_MODE_SLOTS.contains(EquipmentSlot.MAINHAND) &&
            (slotNum - 36) == player.getInventory().getSelectedSlot())
        {
            return true;
        }

        return REPAIR_MODE_SLOT_NUMBERS.contains(slotNum);
    }

    /**
     * Returns the equipment type for the given slot number,
     * assuming that the slot number is for the player's main inventory container
     */
    @Nullable
    private static EquipmentSlot getEquipmentTypeForSlot(int slotNum, Player player)
    {
        if (REPAIR_MODE_SLOTS.contains(EquipmentSlot.MAINHAND) &&
            (slotNum - 36) == player.getInventory().getSelectedSlot())
        {
            return EquipmentSlot.MAINHAND;
        }

		return switch (slotNum)
		{
			case 45 -> EquipmentSlot.OFFHAND;
			case 5 -> EquipmentSlot.HEAD;
			case 6 -> EquipmentSlot.CHEST;
			case 7 -> EquipmentSlot.LEGS;
			case 8 -> EquipmentSlot.FEET;
			default -> null;
		};
	}

    /**
     * Returns the slot number for the given equipment type
     * in the player's inventory container
     */
    private static int getSlotNumberForEquipmentType(EquipmentSlot type, @Nullable Player player)
    {
		return switch (type)
		{
			case MAINHAND -> player != null ? player.getInventory().getSelectedSlot() + 36 : -1;
			case OFFHAND -> 45;
			case HEAD -> 5;
			case CHEST -> 6;
			case LEGS -> 7;
			case FEET -> 8;
			default -> -1;
		};
	}

    public static void swapHotbarWithInventoryRow(Player player, int row)
    {
        AbstractContainerMenu container = player.inventoryMenu;
        row = Mth.clamp(row, 0, 2);
        int slot = row * 9 + 9;

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++)
        {
            fi.dy.masa.malilib.util.InventoryUtils.swapSlots(container, slot, hotbarSlot);
            slot++;
        }
    }

    public static void restockNewStackToHand(Player player, InteractionHand hand, ItemStack stackReference, boolean allowHotbar)
    {
        int slotWithItem;

        if (stackReference.isDamageableItem())
        {
            int minDurability = getMinDurability(stackReference);
            slotWithItem = findSlotWithSuitableReplacementToolWithDurabilityLeft(player.inventoryMenu, stackReference, minDurability);
        }
        else
        {
            slotWithItem = findSlotWithItem(player.inventoryMenu, stackReference, allowHotbar, true);
        }

        if (slotWithItem != -1)
        {
            swapItemToHand(player, hand, slotWithItem);
        }
    }

    public static void preRestockHand(Player player, InteractionHand hand, boolean allowHotbar)
    {
        ItemStack stackHand = player.getItemInHand(hand);
        int threshold = Configs.Generic.HAND_RESTOCK_PRE_THRESHOLD.getIntegerValue();

        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue() &&
            Configs.Generic.HAND_RESTOCK_PRE.getBooleanValue() &&
            stackHand.isEmpty() == false &&
            stackHand.getCount() <= threshold && stackHand.getMaxStackSize() > threshold &&
            PlacementTweaks.canUseItemWithRestriction(PlacementTweaks.HAND_RESTOCK_RESTRICTION, stackHand) &&
            player.containerMenu == player.inventoryMenu &&
            player.containerMenu.getCarried().isEmpty())
        {
            Minecraft mc = Minecraft.getInstance();
            AbstractContainerMenu container = player.inventoryMenu;
            int endSlot = allowHotbar ? 44 : 35;
            int currentMainHandSlot = player.getInventory().getSelectedSlot() + 36;
            int currentSlot = hand == InteractionHand.MAIN_HAND ? currentMainHandSlot : 45;

            for (int slotNum = 9; slotNum <= endSlot; ++slotNum)
            {
                if (slotNum == currentMainHandSlot)
                {
                    continue;
                }

                Slot slot = container.slots.get(slotNum);
                ItemStack stackSlot = slot.getItem();

                if (fi.dy.masa.malilib.util.InventoryUtils.areStacksEqualIgnoreDurability(stackSlot, stackHand))
                {
                    // If all the items from the found slot can fit into the current
                    // stack in hand, then left click, otherwise right click to split the stack
                    int button = stackSlot.getCount() + stackHand.getCount() <= stackHand.getMaxStackSize() ? 0 : 1;

                    mc.gameMode.handleInventoryMouseClick(container.containerId, slot.index, button, ClickType.PICKUP, player);
                    mc.gameMode.handleInventoryMouseClick(container.containerId, currentSlot, 0, ClickType.PICKUP, player);

                    break;
                }
            }
        }
    }

    public static void trySwapCurrentToolIfNearlyBroken()
    {
        Player player = Minecraft.getInstance().player;

        if (FeatureToggle.TWEAK_SWAP_ALMOST_BROKEN_TOOLS.getBooleanValue() && player != null)
        {
            trySwapCurrentToolIfNearlyBroken(InteractionHand.MAIN_HAND, player);
            trySwapCurrentToolIfNearlyBroken(InteractionHand.OFF_HAND, player);
        }
    }

    public static void trySwapCurrentToolIfNearlyBroken(InteractionHand hand, Player player)
    {
        ItemStack stack = player.getItemInHand(hand);

        if (!stack.isEmpty())
        {
            int minDurability = getMinDurability(stack);

            if (isItemAtLowDurability(stack, minDurability))
            {
                swapItemWithHigherDurabilityToHand(player, hand, stack, minDurability + 1);
            }
        }
    }

    public static void trySwitchToWeapon(Entity entity)
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && mc.level != null &&
			!TOOL_SWITCH_IGNORED_SLOTS.contains(player.getInventory().getSelectedSlot()))
        {
            AbstractContainerMenu container = player.inventoryMenu;
            ItemPickerTest test;

            // Ignore the MACE weapon when equipped.  Do not swap.
            if (player.getMainHandItem().is(Items.MACE))
            {
                return;
            }

            if (FeatureToggle.TWEAK_SWAP_ALMOST_BROKEN_TOOLS.getBooleanValue())
            {
                test = (currentStack, previous) -> InventoryUtils.isBetterWeaponAndHasDurability(currentStack, previous, entity);
            }
            else
            {
                test = (currentStack, previous) -> InventoryUtils.isBetterWeapon(currentStack, previous, entity);
            }

            int slotNumber = findSlotWithBestItemMatch(container, test, UniformInt.of(36, 44), UniformInt.of(9, 35));

            if (slotNumber != -1 && (slotNumber - 36) != player.getInventory().getSelectedSlot())
            {
                swapToolToHand(slotNumber, mc);
                PlacementTweaks.cacheStackInHand(InteractionHand.MAIN_HAND);
            }
        }
    }

    private static boolean isBetterWeapon(ItemStack testedStack, ItemStack previousWeapon, Entity entity)
    {
        boolean isWeapon = EquipmentUtils.isAnyWeapon(testedStack);

        if (testedStack.is(Items.MACE))
        {
            return false;
        }

        if (previousWeapon.isEmpty() && isWeapon)
        {
            return true;
        }

        if (!testedStack.isEmpty() && isWeapon)
        {
            final boolean mapping = matchesWeaponMapping(testedStack, entity);

            if (!matchesWeaponMapping(previousWeapon, entity))
            {
                return true;
            }

            // Ignore the Mace by default
            if (!mapping || testedStack.is(Items.MACE))
            {
                return false;
            }

            return isBetterWeaponEach(testedStack, previousWeapon);
        }

        return false;
    }

    private static boolean isBetterWeaponEach(ItemStack testedStack, ItemStack previousWeapon)
    {
        final boolean isRanged = EquipmentUtils.isRangedWeapon(testedStack);
        final boolean enchants = Configs.Generic.WEAPON_SWAP_BETTER_ENCHANTS.getBooleanValue() ? hasSameOrBetterWeaponEnchantments(testedStack, previousWeapon) : true;
        final boolean mats = hasTheSameOrBetterMaterial(testedStack, previousWeapon);
        final boolean rarity = hasTheSameOrBetterRarity(testedStack, previousWeapon);

        final double tested = getBaseAttackDamage(testedStack);
        final double prev = getBaseAttackDamage(previousWeapon);

        if (tested > prev)
        {
            return rarity || mats;
        }

        if (tested == prev)
        {
            return (rarity || mats) && enchants;
        }

        return false;
    }

    private static boolean isBetterWeaponAndHasDurability(ItemStack testedStack, ItemStack previousTool, Entity entity)
    {
        return hasEnoughDurability(testedStack) && isBetterWeapon(testedStack, previousTool, entity);
    }

    private static double getBaseAttackDamage(ItemStack stack)
    {
        Pair<Double, Double> pair = EquipmentUtils.getDamageAndSpeedAttributes(stack);

        if (pair.getLeft() > 0)
        {
            return pair.getLeft();
        }
        else
        {
            return 0;
        }
    }

    protected static boolean matchesWeaponMapping(ItemStack stack, Entity entity)
    {
        HashSet<Item> weapons = WEAPON_MAPPING.getOrDefault(entity.getType(), WEAPON_MAPPING.get(null));

        return weapons != null && weapons.contains(stack.getItem());
    }

    public static void trySwitchToEffectiveTool(BlockPos pos)
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && mc.level != null &&
			!TOOL_SWITCH_IGNORED_SLOTS.contains(player.getInventory().getSelectedSlot()))
        {
            BlockState state = mc.level.getBlockState(pos);
            AbstractContainerMenu container = player.inventoryMenu;
            ItemPickerTest test;

            if (FeatureToggle.TWEAK_SWAP_ALMOST_BROKEN_TOOLS.getBooleanValue())
            {
                test = (currentStack, previous) -> InventoryUtils.isBetterToolAndHasDurability(currentStack, previous, state);
            }
            else
            {
                test = (currentStack, previous) -> InventoryUtils.isBetterTool(currentStack, previous, state);
            }

            int slotNumber = findSlotWithBestItemMatch(container, test, UniformInt.of(36, 44), UniformInt.of(9, 35));

            if (slotNumber != -1 && (slotNumber - 36) != player.getInventory().getSelectedSlot())
            {
                swapToolToHand(slotNumber, mc);
            }
        }
    }

    private static boolean isBetterTool(ItemStack testedStack, ItemStack previousTool, BlockState state)
    {
        boolean isTool = EquipmentUtils.isAnyTool(testedStack);
        boolean isMisc = EquipmentUtils.isMiscTool(testedStack);
//        Tweakeroo.LOGGER.error("isBetterTool(): test [{}], prev [{}], state [{}] // isTool [{}] // isMisc [{}]", testedStack.toString(), previousTool.toString(), state.toString(), isTool, isMisc);

        if (previousTool.isEmpty() && isTool &&
            (Configs.Generic.TOOL_SWAP_BAMBOO_USES_SWORD_FIRST.getBooleanValue() && !state.is(Blocks.BAMBOO)))
        {
			//System.out.print("isBetterTool: (applyBambooNeedsSwordFirst) = TRUE\n");
            return true;
        }

        if (Configs.Generic.TOOL_SWAP_BAMBOO_USES_SWORD_FIRST.getBooleanValue() && state.is(Blocks.BAMBOO))
        {
            if (EquipmentUtils.isSword(testedStack))
            {
				//System.out.print("isBetterTool: (applyBambooNeedsSwordFirst) -> test\n");
                return applyBambooNeedsSwordFirst(testedStack, previousTool);
            }
            else if (EquipmentUtils.isSword(previousTool))
            {
				//System.out.print("isBetterTool: (applyBambooNeedsSwordFirst) = FALSE\n");
                return false;
            }
        }

        if (Configs.Generic.TOOL_SWAP_GLASS_USES_PICKAXE_FIRST.getBooleanValue() && isGlassBlock(state))
        {
            boolean testPickaxe = EquipmentUtils.isPickAxe(testedStack);
            boolean prevPickaxe = EquipmentUtils.isPickAxe(previousTool);
            boolean silkFirst = Configs.Generic.TOOL_SWAP_SILK_TOUCH_FIRST.getBooleanValue();
            boolean testSilk = silkFirst && EquipmentUtils.hasSilkTouch(testedStack);
            boolean prevSilk = silkFirst && EquipmentUtils.hasSilkTouch(previousTool);

            if (testPickaxe || prevPickaxe || testSilk || prevSilk)
            {
                return applyGlassUsesPickaxeFirst(testedStack, previousTool);
            }
        }

        if (!testedStack.isEmpty() && isMisc &&
            Configs.Generic.TOOL_SWAP_NEEDS_SHEARS_FIRST.getBooleanValue() && CachedTagManager.isNeedsShears(state) &&
            testedStack.is(Items.SHEARS) && !EquipmentUtils.isCorrectTool(testedStack, state))
        {
			//System.out.printf("applyNeedsShearsFirst: result: %s\n", test);
			return applyNeedsShearsFirst(testedStack, previousTool, state, isMisc);
        }

        if (!testedStack.isEmpty() && isTool)
        {
			if ((Configs.Generic.TOOL_SWAP_SILK_TOUCH_FIRST.getBooleanValue() && CachedTagManager.isNeedsSilkTouch(state)) ||
				(Configs.Generic.TOOL_SWAP_SILK_TOUCH_ORES.getBooleanValue()  && CachedTagManager.isOreBlock(state) &&
				EquipmentUtils.isPickAxe(testedStack) && EquipmentUtils.isCorrectTool(testedStack, state)))
			{
				//System.out.printf("applySilkTouchFirst:B: result: %s\n", test);
				return applySilkTouchFirst(testedStack, previousTool, state, isMisc);
            }
            else if (Configs.Generic.TOOL_SWAP_SILK_TOUCH_OVERRIDE.getBooleanValue() && CachedTagManager.isSilkTouchOverride(state))
            {
				//System.out.printf("applySilkTouchFirst:C: result: %s\n", test);
				return applySilkTouchFirst(testedStack, previousTool, state, isMisc);
            }

			//System.out.printf("isBetterToolEach: result: %s\n", test);
			return isBetterToolEach(testedStack, previousTool, state, isMisc, true);
        }

		//System.out.printf("isBetterTool: (Default-Correct?) result: %s\n", test);
        return EquipmentUtils.isCorrectTool(testedStack, state);
    }

    private static boolean isGlassBlock(BlockState state)
    {
        boolean isGlassPane = CachedTagUtils.matchBlockTag(fi.dy.masa.malilib.data.CachedTagManager.GLASS_PANES_KEY, state);
        boolean isGlass = state.is(BlockTags.IMPERMEABLE);
        return isGlass || isGlassPane;
    }

    // Even though an Axe is the "Correct tool" for Bamboo, a Sword is preferred
    private static boolean applyBambooNeedsSwordFirst(ItemStack testedStack, ItemStack previousTool)
    {
        final boolean prevSword = EquipmentUtils.isSword(previousTool);
        final boolean enchants = Configs.Generic.WEAPON_SWAP_BETTER_ENCHANTS.getBooleanValue() ? hasSameOrBetterWeaponEnchantments(testedStack, previousTool) : true;
        final boolean mats = hasTheSameOrBetterMaterial(testedStack, previousTool);
        final boolean result = (mats) && enchants;

        //System.out.print ("   (applyBambooNeedsSwordFirst)");
        //System.out.printf("   Mats result: %s", mats);
        //System.out.printf("   Enchant result: %s", enchants);
        //System.out.printf("\n   Prev Sword: %s -> %s\n", prevSword, result);

        if (prevSword)
        {
            return result;
        }

        return true;
    }

    // Prefer Silk Touch or Pickaxe first on glass depending on TOOL_SWAP_SILK_TOUCH_FIRST
    private static boolean applyGlassUsesPickaxeFirst(ItemStack testedStack, ItemStack previousTool)
    {
        final boolean testSilk = EquipmentUtils.hasSilkTouch(testedStack);
        final boolean prevSilk = EquipmentUtils.hasSilkTouch(previousTool);
        final boolean testPickaxe = EquipmentUtils.isPickAxe(testedStack);
        final boolean prevPickaxe = EquipmentUtils.isPickAxe(previousTool);
        final boolean silkFirst = Configs.Generic.TOOL_SWAP_SILK_TOUCH_FIRST.getBooleanValue();
        final int testRank = silkFirst
                ? (testSilk ? 2 : 0) + (testPickaxe ? 1 : 0)
                : (testPickaxe ? 2 : 0) + (testSilk ? 1 : 0);
        final int prevRank = silkFirst
                ? (prevSilk ? 2 : 0) + (prevPickaxe ? 1 : 0)
                : (prevPickaxe ? 2 : 0) + (prevSilk ? 1 : 0);

        if (testRank != prevRank)
        {
            return testRank > prevRank;
        }

        final boolean enchants = Configs.Generic.TOOL_SWAP_BETTER_ENCHANTS.getBooleanValue() ? hasSameOrBetterToolEnchantments(testedStack, previousTool) : true;
        final boolean mats = hasTheSameOrBetterMaterial(testedStack, previousTool);
        return mats && enchants;
    }

    // Use shears if block needs shears.  Do this before needs_silk_touch, because
    // the fact that an item needs shears doesn't pass the 'isCorrectTool()', and doesn't nessecarily need silk touch.
    private static boolean applyNeedsShearsFirst(ItemStack testedStack, ItemStack previousTool, BlockState state, boolean isMisc)
    {
        if (!isMisc) return false;

        final boolean enchants = Configs.Generic.TOOL_SWAP_BETTER_ENCHANTS.getBooleanValue() ? hasSameOrBetterToolEnchantments(testedStack, previousTool) : true;
        final float testSpeed = getBaseBlockBreakingSpeed(testedStack, state);
        final float prevSpeed = getBaseBlockBreakingSpeed(previousTool, state);
        final boolean prevShears = previousTool.is(Items.SHEARS);
        final boolean result = prevShears ? (testSpeed >= prevSpeed) && enchants : true;

        //System.out.print ("   (applyNeedsShearsFirst)");
        //System.out.printf("   Enchant result: %s", enchants);
        //System.out.printf("   Result: %s", result);
        //System.out.printf("\n   Speed test [%f] vs prev [%f]\n", testSpeed, prevSpeed);

        return result;
    }

    // Note that this function is designed not to check the 'Correct Tool' status of a tool,
    // but apply isBetterTool() the same as if it was, as long as it has Silk Touch.
    private static boolean applySilkTouchFirst(ItemStack testedStack, ItemStack previousTool, BlockState state, boolean isMisc)
    {
        final boolean prevSilk = EquipmentUtils.hasSilkTouch(previousTool);

        if (EquipmentUtils.hasSilkTouch(testedStack))
        {
            final boolean mats = hasTheSameOrBetterMaterial(testedStack, previousTool);
            final boolean rarity = hasTheSameOrBetterRarity(testedStack, previousTool);
            final float testSpeed = getBaseBlockBreakingSpeed(testedStack, state);
            final float prevSpeed = getBaseBlockBreakingSpeed(previousTool, state);

            //System.out.print ("   (applySilkTouchFirst)");
            //System.out.printf("   Mats result: %s", mats);
            //System.out.printf("   Rarity result: %s", rarity);
            //System.out.printf("\n   Speed test [%f] vs prev [%f]\n", testSpeed, prevSpeed);

            if (testSpeed > prevSpeed)
            {
                return true;
            }
            else if (testSpeed == prevSpeed)
            {
                return isMisc ? !prevSilk : prevSilk ? (rarity && mats) : true;
            }
            else if (testSpeed < prevSpeed && !prevSilk)
            {
                return isMisc ? true : (rarity && mats);
            }
        }
        else if (prevSilk && !EquipmentUtils.hasSilkTouch(testedStack))
        {
            return false;
        }

		// Should default to original behavior.
		//System.out.printf("applySilkTouchFirst: (Default-Correct?) result: %s\n", test);
		return isBetterToolEach(testedStack, previousTool, state, isMisc, true);
    }

    private static boolean isBetterToolEach(ItemStack testedStack, ItemStack previousTool, BlockState state, boolean isMisc, boolean loop)
    {
        final boolean correct = EquipmentUtils.isCorrectTool(testedStack, state);
        final float testSpeed = getBaseBlockBreakingSpeed(testedStack, state);
        final float prevSpeed = getBaseBlockBreakingSpeed(previousTool, state);
        final boolean testSilkTouch = EquipmentUtils.hasSilkTouch(testedStack);
        final boolean prevSilkTouch = EquipmentUtils.hasSilkTouch(previousTool);

        if (!correct)
        {
            return false;
        }

        final boolean enchants = Configs.Generic.TOOL_SWAP_BETTER_ENCHANTS.getBooleanValue() ? hasSameOrBetterToolEnchantments(testedStack, previousTool) : true;
        final boolean mats = hasTheSameOrBetterMaterial(testedStack, previousTool);
        final boolean rarity = hasTheSameOrBetterRarity(testedStack, previousTool);

        //System.out.print ("   (isBetterToolEach)");
        //System.out.printf("   Enchant result: %s", enchants);
        //System.out.printf("   Mats result: %s", mats);
        //System.out.printf("   Rarity result: %s", rarity);
        //System.out.printf("   Silk Touch result: test - %s, prev - %s", testSilkTouch, prevSilkTouch);
        //System.out.printf("\n   Speed test [%f] vs prev [%f]", testSpeed, prevSpeed);
        //System.out.printf("\n   CorrectTool result: %s\n", correct);

        if (testSpeed > prevSpeed)
        {
            return isMisc ? correct : (rarity || mats) && correct;
        }
        else if (testSpeed == prevSpeed)
        {
            final boolean preferSilk = Configs.Generic.TOOL_SWAP_PREFER_SILK_TOUCH.getBooleanValue();
            Configs.Generic.TOOL_SWAP_PREFER_SILK_TOUCH.setBooleanValue(false);
            final boolean result = isMisc ? enchants && correct : (rarity || mats) && enchants && correct;
            final boolean prevResult = loop ? isBetterToolEach(previousTool, testedStack, state, isMisc, false) : false;
            Configs.Generic.TOOL_SWAP_PREFER_SILK_TOUCH.setBooleanValue(preferSilk);

            //System.out.printf("   Silk Touch Preference results: config: %s // test - %s, prev - %s", preferSilk, result, prevResult);

            // Filter out matches based on config for Silk Touch over Non-Silk Touch tools
            // when all other checks cannot determine which one should be picked.
            if (prevResult && result)
            {
                if (preferSilk)
                {
                    return testSilkTouch && !prevSilkTouch;
                }
                else
                {
                    if (!testSilkTouch && prevSilkTouch)
                    {
                        return true;
                    }
                    else if (testSilkTouch && !prevSilkTouch)
                    {
                        return false;
                    }
                }
            }

            return result;
        }

        return false;
    }

    private static boolean isBetterToolAndHasDurability(ItemStack testedStack, ItemStack previousTool, BlockState state)
    {
        return hasEnoughDurability(testedStack) && isBetterTool(testedStack, previousTool, state);
    }

    private static boolean hasTheSameOrBetterRarity(ItemStack testedStack, ItemStack previousTool)
    {
        return Integer.compare(getRarityWeight(testedStack), getRarityWeight(previousTool)) >= 0;
    }

    private static int getRarityWeight(ItemStack stack)
    {
        // Because using the Vanilla methods, doesn't seem to work right.
        switch (stack.getRarity())
        {
            case EPIC -> { return 4; }
            case RARE -> { return 3; }
            case UNCOMMON -> { return 2; }
            case COMMON -> { return 1; }
            case null -> { return -1; }
            default -> { return 0; }
        }
    }

    private static boolean hasTheSameOrBetterMaterial(ItemStack testedStack, ItemStack previousTool)
    {
        return Integer.compare(getMaterialWeight(testedStack), getMaterialWeight(previousTool)) >= 0;
    }

    private static int getMaterialWeight(ItemStack stack)
    {
        String itemType = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        if (itemType.contains("netherite")) return 6;
        if (itemType.contains("diamond")) return 5;
        if (itemType.contains("iron")) return 4;
	    if (itemType.contains("copper")) return 3;
        if (itemType.contains("stone")) return 2;
        if (itemType.contains("gold")) return 1;
        if (itemType.contains("wood")) return 0;

        return -1;
    }

    /**
     * Creates a total additive value of the essential Enchantment Levels.
     * If one of them does not contain the same Enchantment;
     * then the level should be -1, and will reduce its total weighted value;
     * But if the enchantment level is better, then the weight is +1, and adds to it's value.
     * The same Enchantment Level would then be a 0; and has no weighted change.
     * The result is then in favor for the testedStack if the total weight is > 0.
     */
    private static boolean hasSameOrBetterToolEnchantments(ItemStack testedStack, ItemStack previousTool)
    {
        int count = 0;

        // Core Tool Enchants
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.MENDING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.UNBREAKING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.EFFICIENCY);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.FORTUNE);

        return count >= 0;
    }

    private static boolean hasSameOrBetterWeaponEnchantments(ItemStack testedStack, ItemStack previousTool)
    {
        int count = 0;

        // Core Weapon Enchantments
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.MENDING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.UNBREAKING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.LOOTING);

        // Damage Dealing
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.SHARPNESS);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.SMITE);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.BANE_OF_ARTHROPODS);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.POWER);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.IMPALING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.DENSITY);

        // Support
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.SWEEPING_EDGE);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.FIRE_ASPECT);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.PUNCH);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.INFINITY);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.FLAME);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.MULTISHOT);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.QUICK_CHARGE);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.PIERCING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.RIPTIDE);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.LOYALTY);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.CHANNELING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.BREACH);

        return count >= 0;
    }

    protected static float getBaseBlockBreakingSpeed(ItemStack stack, BlockState state)
    {
        float speed = EquipmentUtils.getMiningSpeed(stack, state);

        if (speed > 1.0f)
        {
            int effLevel = EquipmentUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);

            if (effLevel > 0)
            {
                speed += (effLevel * effLevel) + 1;
            }
        }

        if (state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state))
        {
            speed /= (100F / 30F);
        }

        return speed;
    }

    protected static boolean hasEnoughDurability(ItemStack stack)
    {
        return stack.getMaxDamage() - stack.getDamageValue() > getMinDurability(stack);
    }

    private static int findSuitableSlot(AbstractContainerMenu container, Predicate<ItemStack> itemTest)
    {
        return findSuitableSlot(container, itemTest, UniformInt.of(9, container.slots.size() - 1));
    }

    private static int findSuitableSlot(AbstractContainerMenu container, Predicate<ItemStack> itemTest, UniformInt... ranges)
    {
        final int max = container.slots.size() - 1;

        for (UniformInt range : ranges)
        {
            int end = Math.min(max, range.getMaxValue());

            for (int slotNumber = range.getMinValue(); slotNumber <= end; ++slotNumber)
            {
                if (itemTest.test(container.getSlot(slotNumber).getItem()))
                {
                    return slotNumber;
                }
            }
        }

        return -1;
    }

    private static int findSlotWithBestItemMatch(AbstractContainerMenu container, ItemPickerTest itemTest, UniformInt... ranges)
    {
        final int max = container.slots.size() - 1;
        ItemStack bestMatch = ItemStack.EMPTY;
        int slotNum = -1;

        for (UniformInt range : ranges)
        {
            int end = Math.min(max, range.getMaxValue());

            for (int slotNumber = range.getMinValue(); slotNumber <= end; ++slotNumber)
            {
                Slot slot = container.getSlot(slotNumber);

                if (itemTest.isBetterMatch(slot.getItem(), bestMatch))
                {
                    bestMatch = slot.getItem();
                    slotNum = slot.index;
                }
            }
        }

        return slotNum;
    }

    private static int findEmptySlot(AbstractContainerMenu container, Collection<Integer> slotNumbers)
    {
        final int maxSlot = container.slots.size() - 1;

        for (int slotNumber : slotNumbers)
        {
            if (slotNumber >= 0 && slotNumber <= maxSlot &&
				!container.getSlot(slotNumber).hasItem())
            {
                return slotNumber;
            }
        }

        return -1;
    }

    public interface ItemPickerTest
    {
        boolean isBetterMatch(ItemStack testedStack, ItemStack previousBestMatch);
    }

    private static boolean isItemAtLowDurability(ItemStack stack, int minDurability)
    {
        return stack.isDamageableItem() && (stack.getMaxDamage() - stack.getDamageValue()) <= minDurability;
    }

    private static int getMinDurability(ItemStack stack)
    {
        if (!FeatureToggle.TWEAK_SWAP_ALMOST_BROKEN_TOOLS.getBooleanValue() ||
	        (Configs.Generic.TOOL_SWAP_ALLOW_UNENCHANTED_TO_BREAK.getBooleanValue() && !stack.isEnchanted()))
        {
            return 0;
        }

        int minDurability = Configs.Generic.ITEM_SWAP_DURABILITY_THRESHOLD.getIntegerValue();

        // For items with low maximum durability, use 8% as the threshold,
        // if the configured durability threshold is over that.
        if (stack.getMaxDamage() <= 100 && minDurability <= 20 &&
            (double) minDurability / (double) stack.getMaxDamage() > 0.08)
        {
            minDurability = (int) Math.ceil(stack.getMaxDamage() * 0.08);
        }

        return minDurability;
    }

    private static void swapItemWithHigherDurabilityToHand(Player player, InteractionHand hand, ItemStack stackReference, int minDurabilityLeft)
    {
        AbstractContainerMenu container = player.inventoryMenu;
        int slotWithItem = findSlotWithSuitableReplacementToolWithDurabilityLeft(container, stackReference, minDurabilityLeft);

        if (slotWithItem != -1)
        {
            swapItemToHand(player, hand, slotWithItem);
            InfoUtils.printActionbarMessage("tweakeroo.message.swapped_low_durability_item_for_better_durability");
            return;
        }

        slotWithItem = fi.dy.masa.malilib.util.InventoryUtils.findEmptySlotInPlayerInventory(container, false, false);

        if (slotWithItem != -1)
        {
            swapItemToHand(player, hand, slotWithItem);
            InfoUtils.printActionbarMessage("tweakeroo.message.swapped_low_durability_item_off_players_hand");
            return;
        }

        slotWithItem = findSuitableSlot(container, (s) -> s.isDamageableItem() == false);

        if (slotWithItem != -1)
        {
            swapItemToHand(player, hand, slotWithItem);
            InfoUtils.printActionbarMessage("tweakeroo.message.swapped_low_durability_item_for_dummy_item");
        }
    }

    public static void repairModeSwapItems(Player player)
    {
        if (player.containerMenu == player.inventoryMenu)
        {
            for (EquipmentSlot type : REPAIR_MODE_SLOTS)
            {
                repairModeHandleSlot(player, type);
            }
        }
    }

    private static void repairModeHandleSlot(Player player, EquipmentSlot type)
    {
        int slotNum = getSlotNumberForEquipmentType(type, player);

        if (slotNum == -1)
        {
            return;
        }

        ItemStack stack = player.getItemBySlot(type);

        if (stack.isEmpty() == false &&
            (stack.isDamageableItem() == false ||
             stack.isDamaged() == false ||
            EquipmentUtils.getEnchantmentLevel(stack, Enchantments.MENDING) <= 0))
        {
            Slot slot = player.containerMenu.getSlot(slotNum);
            int slotRepairableItem = findRepairableItemNotInRepairableSlot(slot, player);

            if (slotRepairableItem != -1)
            {
                swapItemToEquipmentSlot(player, type, slotRepairableItem);
                InfoUtils.printActionbarMessage("tweakeroo.message.repair_mode.swapped_repairable_item_to_slot", type.getName());
            }
        }
    }

    /**
     * Adds the enchantment checks for Tools or Weapons
     */
    private static int findRepairableItemNotInRepairableSlot(Slot targetSlot, Player player)
    {
        AbstractContainerMenu containerPlayer = player.containerMenu;

        for (Slot slot : containerPlayer.slots)
        {
            if (slot.hasItem() && isConfiguredRepairSlot(slot.index, player) == false)
            {
                ItemStack stack = slot.getItem();

                // Don't take items from the current hotbar slot
                if ((slot.index - 36) != player.getInventory().getSelectedSlot() &&
                    stack.isDamageableItem() && stack.isDamaged() && targetSlot.mayPlace(stack) &&
                    EquipmentUtils.getEnchantmentLevel(stack, Enchantments.MENDING) > 0)
                {
                    return slot.index;
                }
            }
        }

        return -1;
    }

    public static void equipBestElytra(Player player)
    {
        if (player == null || GuiUtils.getCurrentScreen() != null)
        {
            return;
        }

        AbstractContainerMenu container = player.containerMenu;

        Predicate<ItemStack> filter = (s) ->  s.getItem().equals(Items.ELYTRA) &&
                s.get(DataComponents.EQUIPPABLE).canBeEquippedBy(EntityType.PLAYER) &&
                s.getDamageValue() < s.getMaxDamage() - 10;

        int targetSlot = findSlotWithBestItemMatch(container, (testedStack, previousBestMatch) -> {
            if (!filter.test(testedStack)) return false;
            if (!filter.test(previousBestMatch)) return true;
            if (EquipmentUtils.getEnchantmentLevel(testedStack, Enchantments.UNBREAKING) > EquipmentUtils.getEnchantmentLevel(previousBestMatch, Enchantments.UNBREAKING))
            {
                return true;
            }
            if (EquipmentUtils.getEnchantmentLevel(testedStack, Enchantments.UNBREAKING) < EquipmentUtils.getEnchantmentLevel(previousBestMatch, Enchantments.UNBREAKING))
            {
                return false;
            }
            return testedStack.getDamageValue() <= previousBestMatch.getDamageValue();
        }, UniformInt.of(9, container.slots.size() - 1));

        if (targetSlot >= 0)
        {
            swapItemToEquipmentSlot(player, EquipmentSlot.CHEST, targetSlot);
        }
    }

    // todo for easier forwards porting when the `ArmorItem` disappears
    private static boolean checkChestSlot(ItemStack stack)
    {
//        return stack.getItem() instanceof ArmorItem && EquipmentUtils.matchArmorSlot(stack, EquipmentSlot.CHEST);
        return EquipmentUtils.matchArmorSlot(stack, EquipmentSlot.CHEST);
    }

    public static void swapElytraAndChestPlate(@Nullable Player player)
    {
        if (player == null || GuiUtils.getCurrentScreen() != null)
        {
            return;
        }

        AbstractContainerMenu container = player.containerMenu;
        ItemStack currentStack = player.getItemBySlot(EquipmentSlot.CHEST);

        Predicate<ItemStack> stackFilterChestPlate = (s) -> checkChestSlot(s);

        if (currentStack.isEmpty() || stackFilterChestPlate.test(currentStack))
        {
            equipBestElytra(player);
        }
        else
        {
            Predicate<ItemStack> finalFilter = (s) -> stackFilterChestPlate.test(s) && s.getDamageValue() < s.getMaxDamage() - 10;

            int targetSlot = findSlotWithBestItemMatch(container, (testedStack, previousBestMatch) -> {
                if (!finalFilter.test(testedStack)) return false;
                if (!finalFilter.test(previousBestMatch)) return true;
                if (getArmorAndArmorToughnessValue(previousBestMatch, 1, EquipmentSlotGroup.CHEST) < getArmorAndArmorToughnessValue(testedStack, 1, EquipmentSlotGroup.CHEST))
                {
                    return true;
                }
                if (getArmorAndArmorToughnessValue(previousBestMatch, 1, EquipmentSlotGroup.CHEST) > getArmorAndArmorToughnessValue(testedStack, 1, EquipmentSlotGroup.CHEST))
                {
                    return false;
                }
                return EquipmentUtils.getEnchantmentLevel(previousBestMatch, Enchantments.PROTECTION) <= EquipmentUtils.getEnchantmentLevel(testedStack, Enchantments.PROTECTION);
            }, UniformInt.of(9, container.slots.size() - 1));

            if (targetSlot >= 0)
            {
                swapItemToEquipmentSlot(player, EquipmentSlot.CHEST, targetSlot);
            }
        }
    }

    private static double getArmorAndArmorToughnessValue(ItemStack stack, double base, EquipmentSlotGroup slot)
    {
        final double[] total = {base};

        stack.forEachModifier(slot, (entry, modifier, consumer) -> {
            if (entry.unwrapKey().orElseThrow() == Attributes.ARMOR
                || entry.unwrapKey().orElseThrow() == Attributes.ARMOR_TOUGHNESS)
            {
                switch (modifier.operation())
                {
                    case ADD_VALUE:
                        total[0] += modifier.amount();
                        break;
                    case ADD_MULTIPLIED_BASE:
                        total[0] += modifier.amount() * base;
                        break;
                    case ADD_MULTIPLIED_TOTAL:
                        total[0] += modifier.amount() * total[0];
                        break;
                    default:
                        throw new MatchException(null, null);
                }
            }
        });

        return total[0];
    }

    /**
     *
     * Finds a slot with an identical item than <b>stackReference</b>, ignoring the durability
     * of damageable items. Does not allow crafting or armor slots or the offhand slot
     * in the ContainerPlayer container.
     * @return the slot number, or -1 if none were found
     */
    public static int findSlotWithItem(AbstractContainerMenu container, ItemStack stackReference, boolean allowHotbar, boolean reverse)
    {
        final int startSlot = reverse ? container.slots.size() - 1 : 0;
        final int endSlot = reverse ? -1 : container.slots.size();
        final int increment = reverse ? -1 : 1;
        final boolean isPlayerInv = container instanceof InventoryMenu;

        for (int slotNum = startSlot; slotNum != endSlot; slotNum += increment)
        {
            Slot slot = container.slots.get(slotNum);

            if ((isPlayerInv == false || fi.dy.masa.malilib.util.InventoryUtils.isRegularInventorySlot(slot.index, false)) &&
                (allowHotbar || isHotbarSlot(slot) == false) &&
                fi.dy.masa.malilib.util.InventoryUtils.areStacksEqualIgnoreDurability(slot.getItem(), stackReference))
            {
                return slot.index;
            }
        }

        return -1;
    }

    private static boolean isHotbarSlot(Slot slot)
    {
        return isHotbarSlot(slot.index);
    }

    public static boolean isHotbarSlot(int slot)
    {
        return slot >= 36 && slot < (36 + Inventory.getSelectionSize());
    }

    public static boolean isOffhandSlot(int slot)
    {
        return slot == (36 + Inventory.getSelectionSize());
    }

    private static void swapItemToHand(Player player, InteractionHand hand, int slotNumber)
    {
        AbstractContainerMenu container = player.containerMenu;

        if (slotNumber != -1 && container == player.inventoryMenu)
        {
            Minecraft mc = Minecraft.getInstance();
            Inventory inventory = player.getInventory();

            if (hand == InteractionHand.MAIN_HAND)
            {
                int currentHotbarSlot = inventory.getSelectedSlot();

                if (isHotbarSlot(slotNumber))
                {
                    inventory.setSelectedSlot(slotNumber - 36);
                    mc.getConnection().send(new ServerboundSetCarriedItemPacket(inventory.getSelectedSlot()));
                }
                else
                {
                    mc.gameMode.handleInventoryMouseClick(container.containerId, slotNumber, currentHotbarSlot, ClickType.SWAP, mc.player);
                }
            }
            else if (hand == InteractionHand.OFF_HAND)
            {
                mc.gameMode.handleInventoryMouseClick(container.containerId, slotNumber, 40, ClickType.SWAP, mc.player);
            }
        }
    }

    public static void swapItemToEquipmentSlot(Player player, EquipmentSlot type, int sourceSlotNumber)
    {
        if (sourceSlotNumber != -1 && player.containerMenu == player.inventoryMenu)
        {
            int equipmentSlotNumber = getSlotNumberForEquipmentType(type, player);
            swapSlots(player, sourceSlotNumber, equipmentSlotNumber);
        }
    }

    public static void swapSlots(Player player, int slotNum, int otherSlot)
    {
        Minecraft mc = Minecraft.getInstance();
        AbstractContainerMenu container = player.containerMenu;
        mc.gameMode.handleInventoryMouseClick(container.containerId, slotNum, 0, ClickType.SWAP, player);
        mc.gameMode.handleInventoryMouseClick(container.containerId, otherSlot, 0, ClickType.SWAP, player);
        mc.gameMode.handleInventoryMouseClick(container.containerId, slotNum, 0, ClickType.SWAP, player);
    }

    private static void swapToolToHand(int slotNumber, Minecraft mc)
    {
        Player player = mc.player;

        if (slotNumber >= 0 && player.containerMenu == player.inventoryMenu)
        {
            Inventory inventory = player.getInventory();
            AbstractContainerMenu container = player.inventoryMenu;

            if (isHotbarSlot(slotNumber))
            {
                inventory.setSelectedSlot(slotNumber - 36);
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(inventory.getSelectedSlot()));
            }
            else
            {
                int selectedSlot = inventory.getSelectedSlot();
                int hotbarSlot = getUsableHotbarSlotForTool(selectedSlot, TOOL_SWITCHABLE_SLOTS, container);

                if (Inventory.isHotbarSlot(hotbarSlot))
                {
                    if (hotbarSlot != selectedSlot)
                    {
                        inventory.setSelectedSlot(hotbarSlot);
                        mc.getConnection().send(new ServerboundSetCarriedItemPacket(inventory.getSelectedSlot()));
                    }

                    mc.gameMode.handleInventoryMouseClick(container.containerId, slotNumber, hotbarSlot, ClickType.SWAP, mc.player);
                }
            }
        }
    }

    private static int getUsableHotbarSlotForTool(int currentHotbarSlot, Collection<Integer> validSlots, AbstractContainerMenu container)
    {
        int first = -1;
        int nonTool = -1;

        if (validSlots.contains(currentHotbarSlot))
        {
            ItemStack stack = container.getSlot(currentHotbarSlot + 36).getItem();

            if (stack.isEmpty())
            {
                return currentHotbarSlot;
            }

            /*
            if ((stack.getItem() instanceof MiningToolItem) == false)
            {
                nonTool = currentHotbarSlot;
            }
             */
            if (EquipmentUtils.isRegularTool(stack) == false)
            {
                nonTool = currentHotbarSlot;
            }
        }

        for (int hotbarSlot : validSlots)
        {
            ItemStack stack = container.getSlot(hotbarSlot + 36).getItem();

            if (stack.isEmpty())
            {
                return hotbarSlot;
            }

            //if (nonTool == -1 && (stack.getItem() instanceof MiningToolItem) == false)
            if (nonTool == -1 && EquipmentUtils.isRegularTool(stack) == false)
            {
                nonTool = hotbarSlot;
            }

            if (first == -1)
            {
                first = hotbarSlot;
            }
        }

        return nonTool >= 0 ? nonTool : first;
    }

    private static int findSlotWithSuitableReplacementToolWithDurabilityLeft(AbstractContainerMenu container, ItemStack stackReference, int minDurabilityLeft)
    {
        for (Slot slot : container.slots)
        {
            ItemStack stackSlot = slot.getItem();

            // Only accept regular inventory slots (no crafting, armor slots, or offhand)
            if (fi.dy.masa.malilib.util.InventoryUtils.isRegularInventorySlot(slot.index, false) &&
                ItemStack.isSameItem(stackSlot, stackReference) &&
                stackSlot.getMaxDamage() - stackSlot.getDamageValue() >= minDurabilityLeft &&
                //hasSameOrBetterToolEnchantments(stackReference, stackSlot))
                hasSameIshEnchantments(stackReference, stackSlot))
            {
                return slot.index;
            }
        }

        return -1;
    }

    private static boolean hasSameIshEnchantments(ItemStack stackReference, ItemStack stack)
    {
        int level = EquipmentUtils.getEnchantmentLevel(stackReference, Enchantments.SILK_TOUCH);

        if (level > 0)
        {
            return EquipmentUtils.getEnchantmentLevel(stack, Enchantments.SILK_TOUCH) >= level;
        }

        level = EquipmentUtils.getEnchantmentLevel(stackReference, Enchantments.FORTUNE);

        if (level > 0)
        {
            return EquipmentUtils.getEnchantmentLevel(stack, Enchantments.FORTUNE) >= level;
        }

        return true;
    }

    private static int findSlotWithEffectiveItemWithDurabilityLeft(AbstractContainerMenu container, BlockState state)
    {
        int slotNum = -1;
        float bestSpeed = -1f;

        for (Slot slot : container.slots)
        {
            // Don't consider armor and crafting slots
            if (slot.index <= 8 || slot.hasItem() == false)
            {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (stack.getMaxDamage() - stack.getDamageValue() > getMinDurability(stack))
            {
                float speed = stack.getDestroySpeed(state);

                if (speed > 1.0f)
                {
                    int effLevel = EquipmentUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);

                    if (effLevel > 0)
                    {
                        speed += (effLevel * effLevel) + 1;
                    }
                }

                if (speed > 1f && (slotNum == -1 || speed > bestSpeed))
                {
                    slotNum = slot.index;
                    bestSpeed = speed;
                }
            }
        }

        return slotNum;
    }

    private static void tryCombineStacksInInventory(Player player, ItemStack stackReference)
    {
        List<Slot> slots = new ArrayList<>();
        AbstractContainerMenu container = player.inventoryMenu;
        Minecraft mc = Minecraft.getInstance();

        for (Slot slot : container.slots)
        {
            // Inventory crafting and armor slots are not valid
            if (slot.index < 8)
            {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (stack.getCount() < stack.getMaxStackSize() && fi.dy.masa.malilib.util.InventoryUtils.areStacksEqual(stackReference, stack))
            {
                slots.add(slot);
            }
        }

        for (int i = 0; i < slots.size(); ++i)
        {
            Slot slot1 = slots.get(i);

            for (int j = i + 1; j < slots.size(); ++j)
            {
                Slot slot2 = slots.get(j);
                ItemStack stack = slot1.getItem();

                if (stack.getCount() < stack.getMaxStackSize())
                {
                    // Pick up the item from slot1 and try to put it in slot2
                    mc.gameMode.handleInventoryMouseClick(container.containerId, slot1.index, 0, ClickType.PICKUP, player);
                    mc.gameMode.handleInventoryMouseClick(container.containerId, slot2.index, 0, ClickType.PICKUP, player);

                    // If the items didn't all fit, return the rest
                    if (player.getInventory().getSelectedItem().isEmpty() == false)
                    {
                        mc.gameMode.handleInventoryMouseClick(container.containerId, slot1.index, 0, ClickType.PICKUP, player);
                    }

                    if (slot2.getItem().getCount() >= slot2.getItem().getMaxStackSize())
                    {
                        slots.remove(j);
                        --j;
                    }
                }

                if (slot1.hasItem() == false)
                {
                    break;
                }
            }
        }
    }

    public static boolean canUnstackingItemNotFitInInventory(ItemStack stack, Player player)
    {
        if (FeatureToggle.TWEAK_ITEM_UNSTACKING_PROTECTION.getBooleanValue() &&
            stack.getCount() > 1 &&
            UNSTACKING_ITEMS.contains(stack.getItem()))
        {
            if (fi.dy.masa.malilib.util.InventoryUtils.findEmptySlotInPlayerInventory(player.inventoryMenu, false, false) == -1)
            {
                tryCombineStacksInInventory(player, stack);

                return fi.dy.masa.malilib.util.InventoryUtils.findEmptySlotInPlayerInventory(player.inventoryMenu, false, false) == -1;
            }
        }

        return false;
    }

    public static void switchToPickedBlock()
    {
        Minecraft mc  = Minecraft.getInstance();
        Player player = mc.player;
        Level world = mc.level;

        if (player == null || world == null || player.containerMenu != player.inventoryMenu)
        {
            return;
        }

        double reach = mc.player.blockInteractionRange();
        boolean isCreative = player.isCreative();
        HitResult trace = player.pick(reach, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), false);

        if (trace != null && trace.getType() == HitResult.Type.BLOCK)
        {
            BlockPos pos = ((BlockHitResult) trace).getBlockPos();
            BlockState stateTargeted = world.getBlockState(pos);
            ItemStack stack = ((IMixinAbstractBlock) stateTargeted.getBlock()).tweakeroo_getPickStack(world, pos, stateTargeted, false);

            if (stack.isEmpty() == false &&
                fi.dy.masa.malilib.util.InventoryUtils.areStacksEqual(stack, player.getMainHandItem()) == false)
            {
                AbstractContainerMenu container = player.containerMenu;
                Inventory inventory = player.getInventory();
                /*
                if (isCreative)
                {
                    TileEntity te = world.getTileEntity(pos);

                    if (te != null)
                    {
                        mc.storeTEInStack(stack, te);
                    }
                }
                */

                if (isCreative)
                {
                    inventory.addAndPickItem(stack);
                    mc.gameMode.handleCreativeModeItemAdd(player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.getSelectedSlot());
                }
                else
                {
                    //player.getInventory().getSlotFor(stack);
                    int slotNumber = fi.dy.masa.malilib.util.InventoryUtils.findSlotWithItem(container, stack, true);

                    if (slotNumber != -1)
                    {
                        swapItemToHand(player, InteractionHand.MAIN_HAND, slotNumber);
                    }
                }
            }
        }
    }
}
