package fi.dy.masa.tweakeroo.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.EquipmentUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.HandSlot;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.CachedTagManager;
import fi.dy.masa.tweakeroo.mixin.block.IMixinAbstractBlock;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;

public class InventoryUtils
{
    // private static final AnsiLogger LOGGER = new AnsiLogger(InventoryUtils.class,
    // true, true);
    private static final List<EquipmentSlot> REPAIR_MODE_SLOTS = new ArrayList<>();
    private static final List<Integer> REPAIR_MODE_SLOT_NUMBERS = new ArrayList<>();
    private static final HashSet<Item> UNSTACKING_ITEMS = new HashSet<>();
    private static final List<Integer> TOOL_SWITCHABLE_SLOTS = new ArrayList<>();
    private static final List<Integer> TOOL_SWITCH_IGNORED_SLOTS = new ArrayList<>();
    private static final HashMap<EntityType<?>, HashSet<Item>> WEAPON_MAPPING = new HashMap<>();
    private static int lastSpyglassSlot = -1;

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
        if (configStr.isBlank()) { return; }

        for (String str : parts)
        {
            try
            {
                Matcher matcher = patternRange.matcher(str);

                if (matcher.matches())
                {
                    int slotStart = Integer.parseInt(matcher.group("start")) - 1;
                    int slotEnd = Integer.parseInt(matcher.group("end")) - 1;

                    if (slotStart <= slotEnd && Inventory.isHotbarSlot(slotStart) && Inventory.isHotbarSlot(slotEnd))
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
                // Item item = Registries.ITEM.get(Identifier.tryParse(name));
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
        if (REPAIR_MODE_SLOTS.contains(EquipmentSlot.MAINHAND) && (slotNum - 36) == player.getInventory().getSelectedSlot())
        {
            return true;
        }

        return REPAIR_MODE_SLOT_NUMBERS.contains(slotNum);
    }

    /**
     * Returns the equipment type for the given slot number, assuming that the slot
     * number is for the player's main inventory container
     */
    @Nullable
    private static EquipmentSlot getEquipmentTypeForSlot(int slotNum, Player player)
    {
        if (REPAIR_MODE_SLOTS.contains(EquipmentSlot.MAINHAND) && (slotNum - 36) == player.getInventory().getSelectedSlot())
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
     * Returns the slot number for the given equipment type in the player's
     * inventory container
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

        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue() && Configs.Generic.HAND_RESTOCK_PRE.getBooleanValue() && stackHand.isEmpty() == false && stackHand.getCount() <= threshold && stackHand.getMaxStackSize() > threshold
            && PlacementTweaks.canUseItemWithRestriction(PlacementTweaks.HAND_RESTOCK_RESTRICTION, stackHand) && player.containerMenu == player.inventoryMenu && player.containerMenu.getCarried().isEmpty())
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

        if (player != null && mc.level != null && !TOOL_SWITCH_IGNORED_SLOTS.contains(player.getInventory().getSelectedSlot()))
        {
            AbstractContainerMenu container = player.inventoryMenu;
            ItemPickerTest test;

            // Ignore the MACE weapon when equipped. Do not swap.
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

        if (player != null && mc.level != null && !TOOL_SWITCH_IGNORED_SLOTS.contains(player.getInventory().getSelectedSlot()))
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

    protected static boolean isBetterTool(ItemStack testedStack, ItemStack previousTool, BlockState state)
    {
        Supplier<Boolean> blockHasToolTagRule = () ->
        {
            // If the block has no valid tool type at all (e.g. torch, flower, fire),
            // there is no reason to switch tools — any item breaks it equally.
            if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(BlockTags.MINEABLE_WITH_AXE) && !state.is(BlockTags.MINEABLE_WITH_SHOVEL) && !state.is(BlockTags.MINEABLE_WITH_HOE) && !state.is(BlockTags.SWORD_EFFICIENT) && !CachedTagManager.isPickaxeOverride(state)
                    && !CachedTagManager.isSilkTouchOverride(state) && !CachedTagManager.isNeedsSilkTouch(state) && !CachedTagManager.isNeedsPickaxe(state) && !CachedTagManager.isNeedsShears(state) && !CachedTagManager.isOreBlock(state))
            {
                return false;
            }

            return null;
        };
        Supplier<Boolean> correctToolRule = () ->
        {
            // Use correct tool if we have it
            if (EquipmentUtils.isCorrectTool(testedStack, state) && !EquipmentUtils.isCorrectTool(previousTool, state))
            {
                return true;
            }
            if (!EquipmentUtils.isCorrectTool(testedStack, state) && EquipmentUtils.isCorrectTool(previousTool, state))
            {
                return false;
            }

            return null;
        };
        Supplier<Boolean> anyToolRule = () ->
        {
            // Any tool is better than no tool
            if (!(EquipmentUtils.isAnyTool(previousTool) || EquipmentUtils.isSword(previousTool)))
            {
                boolean isAnyTool = EquipmentUtils.isAnyTool(testedStack) || EquipmentUtils.isSword(testedStack);
                return isAnyTool;
            }

            return null;
        };
        Supplier<Boolean> swordOnBambooRule = () ->
        {
            // Any sword is better than no sword for bamboo.
            if (state.is(Blocks.BAMBOO))
            {
                if (EquipmentUtils.isSword(testedStack) && !EquipmentUtils.isSword(previousTool))
                {
                    return true;
                }
                if (!EquipmentUtils.isSword(testedStack) && EquipmentUtils.isSword(previousTool))
                {
                    return false;
                }
            }

            return null;
        };
        Supplier<Boolean> hoeOnLeavesRule = () ->
        {
            // Any hoe is better than no hoe for leaves.
            if (state.is(BlockTags.LEAVES))
            {
                if (EquipmentUtils.isHoe(testedStack) && !EquipmentUtils.isHoe(previousTool))
                {
                    return true;
                }
                if (!EquipmentUtils.isHoe(testedStack) && EquipmentUtils.isHoe(previousTool))
                {
                    return false;
                }
            }

            return null;
        };
        Supplier<Boolean> shearsOnNeedsShearsRule = () ->
        {
            // Any shears is better than no shears for needs_shears blocks.
            if (CachedTagManager.isNeedsShears(state))
            {
                if (EquipmentUtils.isShears(testedStack) && !EquipmentUtils.isShears(previousTool))
                {
                    return true;
                }
                if (!EquipmentUtils.isShears(testedStack) && EquipmentUtils.isShears(previousTool))
                {
                    return false;
                }
            }

            return null;
        };
        Supplier<Boolean> pickaxeOnNeedsPickaxeRule = () ->
        {
            // Any pickaxe is better than no pickaxe for needs_pickaxe blocks.
            if (CachedTagManager.isNeedsPickaxe(state))
            {
                if (EquipmentUtils.isPickAxe(testedStack) && !EquipmentUtils.isPickAxe(previousTool))
                {
                    return true;
                }
                if (!EquipmentUtils.isPickAxe(testedStack) && EquipmentUtils.isPickAxe(previousTool))
                {
                    return false;
                }
            }

            return null;
        };
        Supplier<Boolean> useFortuneRule = () ->
        {
            // Use fortune if we have it
            if (EquipmentUtils.hasFortune(testedStack) && !EquipmentUtils.hasFortune(previousTool))
            {
                return true;
            }
            if (!EquipmentUtils.hasFortune(testedStack) && EquipmentUtils.hasFortune(previousTool))
            {
                return false;
            }

            return null;
        };
        Supplier<Boolean> useSilkTouchRule = () ->
        {
            // Use silk touch if we have it
            if (EquipmentUtils.hasSilkTouch(testedStack) && !EquipmentUtils.hasSilkTouch(previousTool))
            {
                return true;
            }
            if (!EquipmentUtils.hasSilkTouch(testedStack) && EquipmentUtils.hasSilkTouch(previousTool))
            {
                return false;
            }

            return null;
        };
        Supplier<Boolean> silkTouchFirstRule = () ->
        {
            // Silk touch if we need it
            if (CachedTagManager.isNeedsSilkTouch(state))
            {
                if (EquipmentUtils.hasSilkTouch(testedStack) && !EquipmentUtils.hasSilkTouch(previousTool))
                {
                    return true;
                }
                if (!EquipmentUtils.hasSilkTouch(testedStack) && EquipmentUtils.hasSilkTouch(previousTool))
                {
                    return false;
                }
            }

            return null;
        };
        Supplier<Boolean> silkTouchOresRule = () ->
        {
            // Silk touch if we need it
            if (CachedTagManager.isOreBlock(state))
            {
                if (EquipmentUtils.hasSilkTouch(testedStack) && !EquipmentUtils.hasSilkTouch(previousTool))
                {
                    return true;
                }
                if (!EquipmentUtils.hasSilkTouch(testedStack) && EquipmentUtils.hasSilkTouch(previousTool))
                {
                    return false;
                }
            }

            return null;
        };
        Supplier<Boolean> silkTouchOverrideRule = () ->
        {
            // Silk touch if we need it
            if (CachedTagManager.isSilkTouchOverride(state))
            {
                if (EquipmentUtils.hasSilkTouch(testedStack) && !EquipmentUtils.hasSilkTouch(previousTool))
                {
                    return true;
                }
                if (!EquipmentUtils.hasSilkTouch(testedStack) && EquipmentUtils.hasSilkTouch(previousTool))
                {
                    return false;
                }
            }

            return null;
        };
        Supplier<Boolean> pickaxeOverrideRule = () ->
        {
            // Use a pick axe if it's on the Override list
            if (CachedTagManager.isPickaxeOverride(state))
            {
                if (EquipmentUtils.isPickAxe(testedStack) && !EquipmentUtils.isPickAxe(previousTool))
                {
                    return true;
                }
                if (!EquipmentUtils.isPickAxe(testedStack) && EquipmentUtils.isPickAxe(previousTool))
                {
                    return false;
                }
            }

            return null;
        };
        Supplier<Boolean> betterEnchantRule = () ->
        {
            if (hasBetterToolEnchantments(testedStack, previousTool))
            {
                return true;
            }
            if (hasBetterToolEnchantments(previousTool, testedStack))
            {
                return false;
            }

            return null;
        };
        Supplier<Boolean> betterMaterialRule = () ->
        {
            // If the new tool has a better material
            int testedWeight = getMaterialWeight(testedStack);
            int prevWeight = getMaterialWeight(previousTool);

            if (testedWeight > prevWeight)
            {
                return true;
            }
            if (prevWeight > testedWeight)
            {
                return false;
            }

            return null;
        };
        Supplier<Boolean> betterRarityRule = () ->
        {
            // If the new tool has better rarity
            int testedWeight = getRarityWeight(testedStack);
            int prevWeight = getRarityWeight(previousTool);

            if (testedWeight > prevWeight)
            {
                return true;
            }
            if (prevWeight < testedWeight)
            {
                return false;
            }

            return null;
        };
        Supplier<Boolean> fasterTool = () ->
        {
            // If the new tool is faster, use it
            float testedSpeed = getBaseBlockBreakingSpeed(testedStack, state);
            float prevSpeed = getBaseBlockBreakingSpeed(previousTool, state);

            if (testedSpeed > prevSpeed)
            {
                return true;
            }
            if (testedSpeed < prevSpeed)
            {
                return false;
            }

            return null;
        };
        // Combines two rules in a way that if they contradict each other, then the
        // result is null to skip the check, but if one of them has a non-null result,
        // then that result is used
        BiFunction<Supplier<Boolean>, Supplier<Boolean>, Supplier<Boolean>> combineRules = (r1, r2) ->
        {
            return () ->
            {
                Boolean r1Result = r1.get();
                Boolean r2Result = r2.get();

                if (r1Result != r2Result)
                {
                    if (r1Result == null)
                    {
                        return r2Result;
                    }
                    if (r2Result == null)
                    {
                        return r1Result;
                    }

                    // If the rules contradict each other, then we cannot determine which tool is
                    // better, so return null to skip this check
                    return null;
                }
                return r1Result;
            };
        };

        Map<String, Supplier<Boolean>> ruleMap = Map.ofEntries(Map.entry("blockHasToolTag", blockHasToolTagRule), Map.entry("correctTool", correctToolRule), Map.entry("anyTool", anyToolRule), Map.entry("swordOnBamboo", swordOnBambooRule), Map.entry("hoeOnLeaves", hoeOnLeavesRule),
                Map.entry("shearsOnNeedsShears", shearsOnNeedsShearsRule), Map.entry("pickaxeOnNeedsPickaxe", pickaxeOnNeedsPickaxeRule), Map.entry("useFortune", combineRules.apply(correctToolRule, useFortuneRule)), Map.entry("useSilkTouch", combineRules.apply(correctToolRule, useSilkTouchRule)),
                Map.entry("silkTouchFirst", silkTouchFirstRule), Map.entry("silkTouchOres", combineRules.apply(correctToolRule, silkTouchOresRule)), Map.entry("silkTouchOverride", silkTouchOverrideRule), Map.entry("pickaxeOverride", pickaxeOverrideRule),
                Map.entry("betterEnchantments", betterEnchantRule), Map.entry("betterMaterial", betterMaterialRule), Map.entry("betterRarity", betterRarityRule), Map.entry("fasterTool", fasterTool));

        List<String> rules = new ArrayList<String>();
        rules.add("blockHasToolTag");
        // rules.add("anyTool");
        if (Configs.Generic.TOOL_SWAP_BAMBOO_USES_SWORD_FIRST.getBooleanValue())
        {
            rules.add("swordOnBamboo");
        }
        if (Configs.Generic.TOOL_SWAP_LEAVES_USES_HOE_FIRST.getBooleanValue())
        {
            rules.add("hoeOnLeaves");
        }
        if (Configs.Generic.TOOL_SWAP_NEEDS_SHEARS_FIRST.getBooleanValue())
        {
            rules.add("shearsOnNeedsShears");
        }
        if (Configs.Generic.TOOL_SWAP_NEEDS_PICKAXE_FIRST.getBooleanValue())
        {
            rules.add("pickaxeOnNeedsPickaxe");
        }
        if (Configs.Generic.TOOL_SWAP_PICKAXE_OVERRIDE.getBooleanValue())
        {
            rules.add("pickaxeOverride");
        }
        if (Configs.Generic.TOOL_SWAP_PREFER_FORTUNE_OVERRIDE.getBooleanValue())
        {
            // Get the correct tool and then find the fortune version if there is one
            rules.add("useFortune");
        }
        if (Configs.Generic.TOOL_SWAP_PREFER_SILK_TOUCH.getBooleanValue())
        {
            // Get the correct tool and then find the silk touch version if there is one
            rules.add("useSilkTouch");
        }

        // For silk touch flags use the wrong silk touch tool over the right non-silk
        // touch
        if (Configs.Generic.TOOL_SWAP_SILK_TOUCH_FIRST.getBooleanValue())
        {
            rules.add("silkTouchFirst");
        }
        if (Configs.Generic.TOOL_SWAP_SILK_TOUCH_ORES.getBooleanValue())
        {
            rules.add("silkTouchOres");
        }
        if (Configs.Generic.TOOL_SWAP_SILK_TOUCH_OVERRIDE.getBooleanValue())
        {
            rules.add("silkTouchOverride");
        }

        rules.add("correctTool");
        if (Configs.Generic.TOOL_SWAP_BETTER_ENCHANTS.getBooleanValue())
        {
            rules.add("betterEnchantments");
        }
        rules.add("betterMaterial");
        rules.add("fasterTool");
        // rules.add("betterRarity");
        List<String> testedRules = new ArrayList<>();
        for (String ruleName : rules)
        {
            testedRules.add(ruleName);
            Supplier<Boolean> rule = ruleMap.get(ruleName);
            Boolean result = rule.get();

            if (result != null)
            {
                if (result)
                {
                    Tweakeroo.debugLog("ToolSwap {}\nCompare {}[{}] to {}[{}]", String.join("->", testedRules), previousTool, previousTool.getEnchantments(), testedStack, testedStack.getEnchantments());
                }

                return result;
            }
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
        return switch (stack.getRarity())
        {
            case EPIC -> 4;
            case RARE -> 3;
            case UNCOMMON -> 2;
            case COMMON -> 1;
            case null -> -1;
            default -> 0;
        };
    }

    private static boolean hasTheSameOrBetterMaterial(ItemStack testedStack, ItemStack previousTool)
    {
        return Integer.compare(getMaterialWeight(testedStack), getMaterialWeight(previousTool)) >= 0;
    }

    private static int getMaterialWeight(ItemStack stack)
    {
        String itemType = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        if (itemType.contains("netherite"))
            return 6;
        if (itemType.contains("diamond"))
            return 5;
        if (itemType.contains("iron"))
            return 4;
        if (itemType.contains("copper"))
            return 3;
        if (itemType.contains("stone"))
            return 2;
        if (itemType.contains("gold"))
            return 1;
        if (itemType.contains("wood"))
            return 0;

        return -1;
    }

    /**
     * Creates a total additive value of the essential Enchantment Levels. If one of
     * them does not contain the same Enchantment; then the level should be -1, and
     * will reduce its total weighted value; But if the enchantment level is better,
     * then the weight is +1, and adds to it's value. The same Enchantment Level
     * would then be a 0; and has no weighted change. The result is then in favor
     * for the testedStack if the total weight is > 0.
     */
    private static boolean hasBetterToolEnchantments(ItemStack testedStack, ItemStack previousTool)
    {
        int count = 0;

        // Core Tool Enchants
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.MENDING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.UNBREAKING);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.EFFICIENCY);
        count += EquipmentUtils.hasSameOrBetterEnchantment(testedStack, previousTool, Enchantments.FORTUNE);

        return count > 0;
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
            if (slotNumber >= 0 && slotNumber <= maxSlot && !container.getSlot(slotNumber).hasItem())
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
        if (!FeatureToggle.TWEAK_SWAP_ALMOST_BROKEN_TOOLS.getBooleanValue() || (Configs.Generic.TOOL_SWAP_ALLOW_UNENCHANTED_TO_BREAK.getBooleanValue() && !stack.isEnchanted()))
        {
            return 0;
        }

        int minDurability = Configs.Generic.ITEM_SWAP_DURABILITY_THRESHOLD.getIntegerValue();

        // For items with low maximum durability, use 8% as the threshold,
        // if the configured durability threshold is over that.
        if (stack.getMaxDamage() <= 100 && minDurability <= 20 && (double) minDurability / (double) stack.getMaxDamage() > 0.08)
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
        if (slotNum == -1) { return; }
        ItemStack stack = player.getItemBySlot(type);

        if (stack.isEmpty() == false && (stack.isDamageableItem() == false || stack.isDamaged() == false || EquipmentUtils.getEnchantmentLevel(stack, Enchantments.MENDING) <= 0))
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
                if ((slot.index - 36) != player.getInventory().getSelectedSlot() && stack.isDamageableItem() && stack.isDamaged() && targetSlot.mayPlace(stack) && EquipmentUtils.getEnchantmentLevel(stack, Enchantments.MENDING) > 0)
                {
                    return slot.index;
                }
            }
        }

        return -1;
    }

    public static void equipBestElytra(Player player)
    {
        if (player == null || GuiUtils.getCurrentScreen() != null) { return; }
        AbstractContainerMenu container = player.containerMenu;

        Predicate<ItemStack> filter = (s) -> s.getItem().equals(Items.ELYTRA) && s.get(DataComponents.EQUIPPABLE).canBeEquippedBy(EntityType.PLAYER) && s.getDamageValue() < s.getMaxDamage() - 10;

        int targetSlot = findSlotWithBestItemMatch(container, (testedStack, previousBestMatch) ->
        {
            if (!filter.test(testedStack))
            {
                return false;
            }
            if (!filter.test(previousBestMatch))
            {
                return true;
            }
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

    public static void equipBestFlightRockets(Player player)
    {
        if (player == null || GuiUtils.getCurrentScreen() != null) { return; }
        AbstractContainerMenu container = player.containerMenu;
        Predicate<ItemStack> filter;

        if (Configs.Generic.ROCKET_SWAP_ALLOW_EXPLOSIONS.getBooleanValue())
        {
            // Allows the player to Auto Swap any fireworks
            filter = (s) -> s.getItem().equals(Items.FIREWORK_ROCKET);
        }
        else
        {
            filter = (s) -> s.getItem().equals(Items.FIREWORK_ROCKET) && s.get(DataComponents.FIREWORKS).explosions().isEmpty();
        }

        int slotNumber = findSlotWithBestItemMatch(container, (testedStack, previousBestMatch) ->
        {
            if (!filter.test(testedStack))
            {
                return false;
            }
            if (!filter.test(previousBestMatch))
            {
                return true;
            }

            return testedStack.get(DataComponents.FIREWORKS).flightDuration() > previousBestMatch.get(DataComponents.FIREWORKS).flightDuration()
                    || (testedStack.get(DataComponents.FIREWORKS).flightDuration() == previousBestMatch.get(DataComponents.FIREWORKS).flightDuration() && testedStack.getCount() > previousBestMatch.getCount());
        }, UniformInt.of(9, container.slots.size() - 1));

        InteractionHand hand = fi.dy.masa.malilib.util.InventoryUtils.getHandSlot((HandSlot) Configs.Generic.UTILITY_HAND_SLOT.getOptionListValue());

        if (slotNumber >= 0)
        {
            swapItemToHand(player, hand, slotNumber);
        }
    }

    public static void swapFlightRocketsFromHand(Player player, InteractionHand hand, ItemStack stackReference)
    {
        if (player == null || GuiUtils.getCurrentScreen() != null) { return; }
        AbstractContainerMenu container = player.containerMenu;
        int targetSlot = InventoryUtils.findSlotWithItem(container, stackReference, true, false);

        if (targetSlot >= 0)
        {
            swapItemToHand(player, hand, targetSlot);
        }
    }

    public static void swapElytraFromChest(Player player, ItemStack stackReference)
    {
        if (player == null || GuiUtils.getCurrentScreen() != null) { return; }
        AbstractContainerMenu container = player.containerMenu;
        int targetSlot = findSlotWithItem(container, stackReference, true, false);

        if (targetSlot >= 0)
        {
            swapItemToEquipmentSlot(player, EquipmentSlot.CHEST, targetSlot);
        }
        else
        {
            // cached item not found, try to swap to the default chest plate.
            swapElytraAndChestPlate(player);
        }
    }

    // todo for easier forwards porting when the `ArmorItem` disappears
    private static boolean checkChestSlot(ItemStack stack)
    {
        // return stack.getItem() instanceof ArmorItem &&
        // EquipmentUtils.matchArmorSlot(stack, EquipmentSlot.CHEST);
        return EquipmentUtils.matchArmorSlot(stack, EquipmentSlot.CHEST);
    }

    public static void swapElytraAndChestPlate(@Nullable Player player)
    {
        if (player == null || GuiUtils.getCurrentScreen() != null) { return; }
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

            int targetSlot = findSlotWithBestItemMatch(container, (testedStack, previousBestMatch) ->
            {
                if (!finalFilter.test(testedStack))
                {
                    return false;
                }
                if (!finalFilter.test(previousBestMatch))
                {
                    return true;
                }
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
        final double[] total =
        { base };

        stack.forEachModifier(slot, (entry, modifier, consumer) ->
        {
            if (entry.unwrapKey().orElseThrow() == Attributes.ARMOR || entry.unwrapKey().orElseThrow() == Attributes.ARMOR_TOUGHNESS)
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
     * Finds a slot with an identical item than <b>stackReference</b>, ignoring the
     * durability of damageable items. Does not allow crafting or armor slots or the
     * offhand slot in the ContainerPlayer container.
     * 
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

            if ((isPlayerInv == false || fi.dy.masa.malilib.util.InventoryUtils.isRegularInventorySlot(slot.index, false)) && (allowHotbar || isHotbarSlot(slot) == false) && fi.dy.masa.malilib.util.InventoryUtils.areStacksEqualIgnoreDurability(slot.getItem(), stackReference))
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
             * if ((stack.getItem() instanceof MiningToolItem) == false) { nonTool =
             * currentHotbarSlot; }
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

            // if (nonTool == -1 && (stack.getItem() instanceof MiningToolItem) == false)
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
            if (fi.dy.masa.malilib.util.InventoryUtils.isRegularInventorySlot(slot.index, false) && ItemStack.isSameItem(stackSlot, stackReference) && stackSlot.getMaxDamage() - stackSlot.getDamageValue() >= minDurabilityLeft &&
            // hasSameOrBetterToolEnchantments(stackReference, stackSlot))
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
            if (slot.index < 8) { continue; }
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
        if (FeatureToggle.TWEAK_ITEM_UNSTACKING_PROTECTION.getBooleanValue() && stack.getCount() > 1 && UNSTACKING_ITEMS.contains(stack.getItem()))
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
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level world = mc.level;

        if (player == null || world == null || player.containerMenu != player.inventoryMenu) { return; }
        double reach = mc.player.blockInteractionRange();
        boolean isCreative = player.isCreative();
        HitResult trace = player.pick(reach, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), false);

        if (trace != null && trace.getType() == HitResult.Type.BLOCK)
        {
            BlockPos pos = ((BlockHitResult) trace).getBlockPos();
            BlockState stateTargeted = world.getBlockState(pos);
            ItemStack stack = ((IMixinAbstractBlock) stateTargeted.getBlock()).tweakeroo_getPickStack(world, pos, stateTargeted, false);

            if (stack.isEmpty() == false && fi.dy.masa.malilib.util.InventoryUtils.areStacksEqual(stack, player.getMainHandItem()) == false)
            {
                AbstractContainerMenu container = player.containerMenu;
                Inventory inventory = player.getInventory();
                /*
                 * if (isCreative) { TileEntity te = world.getTileEntity(pos);
                 * 
                 * if (te != null) { mc.storeTEInStack(stack, te); } }
                 */

                if (isCreative)
                {
                    inventory.addAndPickItem(stack);
                    mc.gameMode.handleCreativeModeItemAdd(player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.getSelectedSlot());
                }
                else
                {
                    // player.getInventory().getSlotFor(stack);
                    int slotNumber = fi.dy.masa.malilib.util.InventoryUtils.findSlotWithItem(container, stack, true);

                    if (slotNumber != -1)
                    {
                        swapItemToHand(player, InteractionHand.MAIN_HAND, slotNumber);
                    }
                }
            }
        }
    }

    public static void swapSpyglassToHand()
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) { return; }
        AbstractContainerMenu container = player.containerMenu;
        InteractionHand hand = fi.dy.masa.malilib.util.InventoryUtils.getHandSlot((HandSlot) Configs.Generic.UTILITY_HAND_SLOT.getOptionListValue());

        int slotNumber = findSlotWithItem(container, Items.SPYGLASS.getDefaultInstance(), true, true);

        if (slotNumber != -1)
        {
            lastSpyglassSlot = slotNumber;
            swapItemToHand(player, hand, slotNumber);

            if (!mc.options.keyUse.isDown())
            {
                mc.options.keyUse.setDown(true);
            }
        }
    }

    public static void returnSpyglassToInventory()
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) { return; }
        InteractionHand hand = fi.dy.masa.malilib.util.InventoryUtils.getHandSlot((HandSlot) Configs.Generic.UTILITY_HAND_SLOT.getOptionListValue());

        if (lastSpyglassSlot != -1)
        {
            if (mc.options.keyUse.isDown())
            {
                mc.options.keyUse.setDown(false);
            }

            swapItemToHand(player, hand, lastSpyglassSlot);
            lastSpyglassSlot = -1;
        }
    }
}