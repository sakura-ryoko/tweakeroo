package fi.dy.masa.tweakeroo.tweaks;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.EquipmentUtils;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.MessageOutputType;
import fi.dy.masa.malilib.util.position.PositionUtils;
import fi.dy.masa.malilib.util.restrictions.BlockRestriction;
import fi.dy.masa.malilib.util.restrictions.ItemRestriction;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.mixin.block.IMixinAbstractBlock;
import fi.dy.masa.tweakeroo.util.*;

public class PlacementTweaks
{
    private static BlockPos posFirst = null;
    private static BlockPos posFirstBreaking = null;
    private static BlockPos posLast = null;
    private static PositionUtils.HitPart hitPartFirst = null;
    private static InteractionHand handFirst = InteractionHand.MAIN_HAND;
    private static Vec3 hitVecFirst = null;
    private static Direction sideFirst = null;
    private static Direction sideFirstBreaking = null;
    private static Direction sideRotatedFirst = null;
    private static float playerYawFirst;
    private static ItemStack[] stackBeforeUse = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY};
    private static boolean isFirstClick;
    private static boolean isEmulatedClick;
    private static boolean firstWasRotation;
    private static boolean firstWasOffset;
    private static int placementCount;
    private static int hotbarSlot = -1;
    private static ItemStack stackClickedOn = ItemStack.EMPTY;
    @Nullable
    private static BlockState stateClickedOn = null;
    public static final BlockRestriction BLOCK_TYPE_BREAK_RESTRICTION = new BlockRestriction();
    public static final BlockRestriction FAST_RIGHT_CLICK_BLOCK_RESTRICTION = new BlockRestriction();
    public static final ItemRestriction FAST_RIGHT_CLICK_ITEM_RESTRICTION = new ItemRestriction();
    public static final ItemRestriction FAST_PLACEMENT_ITEM_RESTRICTION = new ItemRestriction();
    public static final ItemRestriction HAND_RESTOCK_RESTRICTION = new ItemRestriction();

    public static void onTick(Minecraft mc)
    {
        boolean attack = mc.options.keyAttack.isDown();
        boolean use = mc.options.keyUse.isDown();

        if (GuiUtils.getCurrentScreen() == null && !FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue())
        {
            if (use)
            {
                onUsingTick();
            }

            if (attack)
            {
                onAttackTick(mc);
            }
        }
        else
        {
            stackBeforeUse[0] = ItemStack.EMPTY;
            stackBeforeUse[1] = ItemStack.EMPTY;
        }

        if (use == false)
        {
            clearClickedBlockInfoUse();

            // Clear the cached stack when releasing both keys, so that the restock doesn't happen when
            // using another item or an empty hand.
            if (attack == false)
            {
                stackBeforeUse[0] = ItemStack.EMPTY;
                stackBeforeUse[1] = ItemStack.EMPTY;
            }
        }

        if (attack == false)
        {
            clearClickedBlockInfoAttack();
        }
    }

    public static boolean onProcessRightClickPre(Player player, InteractionHand hand)
    {
        InventoryUtils.trySwapCurrentToolIfNearlyBroken();

        ItemStack stackOriginal = player.getItemInHand(hand);

        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue() &&
                stackOriginal.isEmpty() == false &&
                canUseItemWithRestriction(HAND_RESTOCK_RESTRICTION, stackOriginal))
        {
            if (isEmulatedClick == false)
            {
                //System.out.printf("onProcessRightClickPre storing stack: %s\n", stackOriginal);
                cacheStackInHand(hand);
            }

            // Don't allow taking stacks from elsewhere in the hotbar, if the cycle tweak is on
            boolean allowHotbar = FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE.getBooleanValue() == false &&
                    FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER.getBooleanValue() == false;
            InventoryUtils.preRestockHand(player, hand, allowHotbar);
        }

        return InventoryUtils.canUnstackingItemNotFitInInventory(stackOriginal, player);
    }

    public static void onProcessRightClickPost(Player player, InteractionHand hand)
    {
        //System.out.printf("onProcessRightClickPost -> tryRestockHand with: %s, current: %s\n", stackBeforeUse[hand.ordinal()], player.getStackInHand(hand));
        tryRestockHand(player, hand, stackBeforeUse[hand.ordinal()]);
    }

    public static void onLeftClickMousePre()
    {
        Minecraft mc = Minecraft.getInstance();
        HitResult trace = mc.hitResult;

        // Only set the position if it was null, otherwise the fast left click tweak
        // would just reset it every time.
        if (trace != null && trace.getType() == HitResult.Type.BLOCK && posFirstBreaking == null)
        {
            posFirstBreaking = ((BlockHitResult) trace).getBlockPos();
            sideFirstBreaking = ((BlockHitResult) trace).getDirection();
        }

        onProcessRightClickPre(mc.player, InteractionHand.MAIN_HAND);
    }

    public static void onLeftClickMousePost()
    {
        onProcessRightClickPost(Minecraft.getInstance().player, InteractionHand.MAIN_HAND);
    }

    public static void cacheStackInHand(InteractionHand hand)
    {
        Player player = Minecraft.getInstance().player;
        ItemStack stackOriginal = player.getItemInHand(hand);

        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue() &&
                stackOriginal.isEmpty() == false &&
                canUseItemWithRestriction(HAND_RESTOCK_RESTRICTION, stackOriginal))
        {
            stackBeforeUse[hand.ordinal()] = stackOriginal.copy();
            hotbarSlot = player.getInventory().getSelectedSlot();
        }
    }

    private static void onAttackTick(Minecraft mc)
    {
        if (FeatureToggle.TWEAK_FAST_LEFT_CLICK.getBooleanValue())
        {
            if (mc.player.getAbilities().instabuild ||
                    (Configs.Generic.FAST_LEFT_CLICK_ALLOW_TOOLS.getBooleanValue() || (EquipmentUtils.isAnyTool(mc.player.getMainHandItem())) == false))
            {
                final int count = Configs.Generic.FAST_LEFT_CLICK_COUNT.getIntegerValue();

                for (int i = 0; i < count; ++i)
                {
                    isEmulatedClick = true;
                    ((IMinecraftClientInvoker) mc).tweakeroo_invokeDoAttack();
                    isEmulatedClick = false;
                }
            }
        }
        else
        {
            InventoryUtils.trySwapCurrentToolIfNearlyBroken();
            InteractionHand hand = InteractionHand.MAIN_HAND;
            tryRestockHand(mc.player, hand, stackBeforeUse[hand.ordinal()]);
        }
    }

    private static void onUsingTick()
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null)
        {
            return;
        }

        if (posFirst != null && FeatureToggle.TWEAK_FAST_BLOCK_PLACEMENT.getBooleanValue() &&
                canUseItemWithRestriction(FAST_PLACEMENT_ITEM_RESTRICTION, mc.player))
        {
            LocalPlayer player = mc.player;
            Level world = player.level();
            final double reach = mc.player.blockInteractionRange();
            final int maxCount = Configs.Generic.FAST_BLOCK_PLACEMENT_COUNT.getIntegerValue();

            mc.hitResult = player.pick(reach, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), false);

            for (int i = 0; i < maxCount; ++i)
            {
                HitResult trace = mc.hitResult;

                if (trace == null || trace.getType() != HitResult.Type.BLOCK)
                {
                    break;
                }

                BlockHitResult blockHitResult = (BlockHitResult) trace;
                InteractionHand hand = handFirst;
                Direction side = blockHitResult.getDirection();
                BlockPos pos = blockHitResult.getBlockPos();
                Vec3 hitVec = blockHitResult.getLocation();

                // Written by Andrew54757 under TweakFork
                if (FeatureToggle.TWEAK_SCAFFOLD_PLACE.getBooleanValue())
                {
                    ItemStack stack = player.getItemInHand(hand);

                    side = getScaffoldPlaceDirection(side, hitPartFirst, player);
                    pos = getScaffoldPlacePosition(pos, side, world, stack, player);
                    if (pos == null) return;

                    pos = pos.relative(side.getOpposite());
                }

                BlockHitResult hitResult = new BlockHitResult(hitVec, side, pos, false);
                BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));
                BlockPos posNew = getPlacementPositionForTargetedPosition(world, pos, side, ctx);
                hitResult = new BlockHitResult(hitVec, side, posNew, false);
                ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));

                if (hand != null &&
                        posNew.equals(posLast) == false &&
                        canPlaceBlockIntoPosition(world, posNew, ctx) &&
                        isPositionAllowedByPlacementRestriction(posNew, side) &&
                        canPlaceBlockAgainst(world, pos, player, hand)
                )
                {
                    /*
                    IBlockState state = world.getBlockState(pos);
                    float x = (float) (trace.hitVec.x - pos.getX());
                    float y = (float) (trace.hitVec.y - pos.getY());
                    float z = (float) (trace.hitVec.z - pos.getZ());

                    if (state.getBlock().onBlockActivated(world, posNew, state, player, hand, side, x, y, z))
                    {
                        return;
                    }
                    */

                    hitVec = hitVecFirst.add(posNew.getX(), posNew.getY(), posNew.getZ());
                    InteractionResult result = tryPlaceBlock(mc.gameMode, player, mc.level,
                                                        posNew, sideFirst, sideRotatedFirst, playerYawFirst, hitVec, hand, hitPartFirst, false);

                    if (result == InteractionResult.SUCCESS)
                    {
                        posLast = posNew;
                        mc.hitResult = player.pick(reach, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false), false);
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }

            // Reset the timer to prevent the regular process method from re-firing
            ((IMinecraftClientInvoker) mc).tweakeroo_setItemUseCooldown(4);
        }
        else if (FeatureToggle.TWEAK_FAST_RIGHT_CLICK.getBooleanValue() &&
                mc.options.keyUse.isDown() &&
                canUseFastRightClick(mc.player))
        {
            final int count = Configs.Generic.FAST_RIGHT_CLICK_COUNT.getIntegerValue();

            for (int i = 0; i < count; ++i)
            {
                isEmulatedClick = true;
                ((IMinecraftClientInvoker) mc).tweakeroo_invokeDoItemUse();
                isEmulatedClick = false;
            }
        }
    }

    public static InteractionResult onProcessRightClickBlock(
            MultiPlayerGameMode controller,
            LocalPlayer player,
            ClientLevel world,
            InteractionHand hand,
            BlockHitResult hitResult)
    {
        if (CameraUtils.shouldPreventPlayerInputs())
        {
            return InteractionResult.PASS;
        }

        InventoryUtils.trySwapCurrentToolIfNearlyBroken();

        ItemStack stackPre = player.getItemInHand(hand);
        BlockPos posIn = hitResult.getBlockPos();

        if (Configs.Disable.DISABLE_AXE_STRIPPING.getBooleanValue() &&
            stackPre.getItem() instanceof AxeItem &&
            MiscUtils.isStrippableLog(world, posIn))
        {
            return InteractionResult.PASS;
        }

        if (Configs.Disable.DISABLE_SHOVEL_PATHING.getBooleanValue() &&
            stackPre.getItem() instanceof ShovelItem &&
            MiscUtils.isShovelPathConvertableBlock(world, posIn))
        {
            return InteractionResult.PASS;
        }

        stackPre = stackPre.copy();
        boolean restricted = FeatureToggle.TWEAK_PLACEMENT_RESTRICTION.getBooleanValue() || FeatureToggle.TWEAK_PLACEMENT_GRID.getBooleanValue();
        Direction sideIn = hitResult.getDirection();
        Vec3 hitVec = hitResult.getLocation();
        Direction playerFacingH = player.getDirection();
        PositionUtils.HitPart hitPart = PositionUtils.getHitPart(sideIn, playerFacingH, posIn, hitVec);
        Direction sideRotated = getRotatedFacing(sideIn, playerFacingH, hitPart);
        float yaw = player.getYRot();

        cacheStackInHand(hand);

        if (FeatureToggle.TWEAK_PLACEMENT_REST_FIRST.getBooleanValue() && stateClickedOn == null)
        {
            BlockState state = world.getBlockState(posIn);
            stackClickedOn = ((IMixinAbstractBlock) state.getBlock()).tweakeroo_getPickStack(world, posIn, state, false);
            stateClickedOn = state;
        }

        if (canPlaceBlockAgainst(world, posIn, player, hand) == false)
        {
            return InteractionResult.PASS;
        }

        boolean flexible = FeatureToggle.TWEAK_FLEXIBLE_BLOCK_PLACEMENT.getBooleanValue();
        boolean rotation = Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ROTATION.getKeybind().isKeybindHeld();
        boolean offset = Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_OFFSET.getKeybind().isKeybindHeld();
        boolean adjacent = Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ADJACENT.getKeybind().isKeybindHeld();

        // Written by Andrew54757 under TweakFork
        if (FeatureToggle.TWEAK_SCAFFOLD_PLACE.getBooleanValue() && (!flexible || (!rotation && !offset && !adjacent)))
        {
            ItemStack stack = player.getItemInHand(hand);
            Direction extendDirection = getScaffoldPlaceDirection(sideIn, hitPart, player);
            BlockPos newPos = getScaffoldPlacePosition(posIn, extendDirection, world, stack, player);

            if (newPos == null)
            {
                return InteractionResult.PASS;
            }

            newPos = newPos.relative(extendDirection.getOpposite());
            sideIn = extendDirection;
            hitVec = hitVec.subtract(posIn.getX(), posIn.getY(), posIn.getZ()).add(newPos.getX(),newPos.getY(),newPos.getZ());
            posIn = newPos;
        }

        //System.out.printf("onProcessRightClickBlock() pos: %s, side: %s, part: %s, hitVec: %s\n", posIn, sideIn, hitPart, hitVec);
        InteractionResult result = tryPlaceBlock(controller, player, world, posIn, sideIn, sideRotated, yaw, hitVec, hand, hitPart, true);

        // Store the initial click data for the fast placement mode
        if (posFirst == null && result == InteractionResult.SUCCESS && restricted)
        {
            boolean accurate = FeatureToggle.TWEAK_ACCURATE_BLOCK_PLACEMENT.getBooleanValue();
            boolean accurateIn = Hotkeys.ACCURATE_BLOCK_PLACEMENT_IN.getKeybind().isKeybindHeld();
            boolean accurateReverse = Hotkeys.ACCURATE_BLOCK_PLACEMENT_REVERSE.getKeybind().isKeybindHeld();

            firstWasRotation = (flexible && rotation) || (accurate && (accurateIn || accurateReverse));
            firstWasOffset = flexible && offset;
            BlockHitResult hitResultTmp = new BlockHitResult(hitVec, sideIn, posIn, false);
            BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResultTmp));
            posFirst = getPlacementPositionForTargetedPosition(world, posIn, sideIn, ctx);
            posLast = posFirst;
            hitPartFirst = hitPart;
            handFirst = hand;
            hitVecFirst = hitVec.subtract(posFirst.getX(), posFirst.getY(), posFirst.getZ());
            sideFirst = sideIn;
            sideRotatedFirst = sideRotated;
            playerYawFirst = yaw;
            stackBeforeUse[hand.ordinal()] = stackPre;
            //System.out.printf("plop store @ %s\n", posFirst);
        }

        return result;
    }

    // Written by Andrew54757 under TweakFork
    private static Direction getScaffoldPlaceDirection(Direction side, PositionUtils.HitPart hitPart, Player player)
    {
        Direction offsetIn = getRotatedFacing(side, player.getDirection(), hitPart).getOpposite();
        Direction extendDirection;

        if (side == Direction.UP || side == Direction.DOWN)
        {
            extendDirection = (hitPart == PositionUtils.HitPart.CENTER || Configs.Generic.SCAFFOLD_PLACE_VANILLA.getBooleanValue()) ? player.getDirection() : offsetIn;
        }
        else
        {
            extendDirection = (hitPart == PositionUtils.HitPart.CENTER || Configs.Generic.SCAFFOLD_PLACE_VANILLA.getBooleanValue()) ? Direction.UP : offsetIn;
        }

        return extendDirection;
    }

    private static BlockPos getScaffoldPlacePosition(BlockPos pos, Direction extendDirection, Level world, ItemStack stack, Player player)
    {
        if (!(stack.getItem() instanceof BlockItem) || extendDirection == null)
        {
            return null;
        }

        Block itemBlock = ((BlockItem)stack.getItem()).getBlock();
        Minecraft mc = Minecraft.getInstance();
        double reach = mc.player.blockInteractionRange();
        BlockPos.MutableBlockPos tempPos = new BlockPos.MutableBlockPos(pos.getX(),pos.getY(),pos.getZ());

        for (int i = 0; i < Configs.Generic.SCAFFOLD_PLACE_DISTANCE.getIntegerValue(); i++)
        {
            tempPos.move(extendDirection);

            if (!MiscUtils.isInReach(tempPos, player, reach))
            {
                return null;
            }

            BlockState state = world.getBlockState(tempPos);

            if (state.getBlock() != itemBlock)
            {
                if (state.isAir() || state.canBeReplaced())
                {
                    return tempPos.immutable();
                }

                return null;
            }
        }

        return null;
    }

    private static InteractionResult tryPlaceBlock(
            MultiPlayerGameMode controller,
            LocalPlayer player,
            ClientLevel world,
            BlockPos posIn,
            Direction sideIn,
            Direction sideRotatedIn,
            float playerYaw,
            Vec3 hitVec,
            InteractionHand hand,
            PositionUtils.HitPart hitPart,
            boolean isFirstClick)
    {
        Direction side = sideIn;
        boolean handleFlexible = false;
        BlockPos posNew = null;
        boolean flexible = FeatureToggle.TWEAK_FLEXIBLE_BLOCK_PLACEMENT.getBooleanValue();
        boolean rotationHeld = Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ROTATION.getKeybind().isKeybindHeld();
        boolean offsetHeld = Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_OFFSET.getKeybind().isKeybindHeld();
        boolean adjacent = Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ADJACENT.getKeybind().isKeybindHeld();
        boolean rememberFlexible = Configs.Generic.REMEMBER_FLEXIBLE.getBooleanValue();
        boolean rotation = rotationHeld || (rememberFlexible && firstWasRotation);
        boolean offset = offsetHeld || (rememberFlexible && firstWasOffset);
        ItemStack stack = player.getItemInHand(hand);

        if (flexible)
        {
            BlockHitResult hitResult = new BlockHitResult(hitVec, sideIn, posIn, false);
            BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));
            posNew = isFirstClick && (rotation || offset || adjacent) ? getPlacementPositionForTargetedPosition(world, posIn, sideIn, ctx) : posIn;

            // Place the block into the adjacent position
            if (adjacent && hitPart != null && hitPart != PositionUtils.HitPart.CENTER)
            {
                posNew = posNew.relative(sideRotatedIn.getOpposite()).relative(sideIn.getOpposite());
                hitVec = hitVec.add(Vec3.atLowerCornerOf(sideRotatedIn.getOpposite().getUnitVec3i().offset(sideIn.getOpposite().getUnitVec3i())));
                handleFlexible = true;
            }

            // Place the block facing/against the adjacent block (= just rotated from normal)
            if (rotation)
            {
                side = sideRotatedIn;
                handleFlexible = true;
            }
            else
            {
                // Don't rotate the player facing in handleFlexibleBlockPlacement()
                hitPart = null;
            }

            // Place the block into the diagonal position
            if (offset)
            {
                posNew = posNew.relative(sideRotatedIn.getOpposite());
                hitVec = hitVec.add(Vec3.atLowerCornerOf(sideRotatedIn.getOpposite().getUnitVec3i()));
                handleFlexible = true;
            }
        }

        boolean simpleOffset = false;

        if (handleFlexible == false &&
            FeatureToggle.TWEAK_FAKE_SNEAK_PLACEMENT.getBooleanValue() &&
            stack.getItem() instanceof BlockItem)
        {
            BlockHitResult hitResult = new BlockHitResult(hitVec, sideIn, posIn, false);
            BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));
            posNew = getPlacementPositionForTargetedPosition(world, posIn, sideIn, ctx);
            simpleOffset = true;
        }

        boolean accurate = FeatureToggle.TWEAK_ACCURATE_BLOCK_PLACEMENT.getBooleanValue();
        boolean accurateIn = Hotkeys.ACCURATE_BLOCK_PLACEMENT_IN.getKeybind().isKeybindHeld();
        boolean accurateReverse = Hotkeys.ACCURATE_BLOCK_PLACEMENT_REVERSE.getKeybind().isKeybindHeld();
        boolean afterClicker = FeatureToggle.TWEAK_AFTER_CLICKER.getBooleanValue();

        if (accurate && (accurateIn || accurateReverse || afterClicker))
        {
            Direction facing = side;
            boolean handleAccurate = false;

            if (posNew == null)
            {
                if (flexible == false || isFirstClick == false)
                {
                    posNew = posIn;
                }
                else
                {
                    BlockHitResult hitResult = new BlockHitResult(hitVec, side, posIn, false);
                    BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));
                    posNew = getPlacementPositionForTargetedPosition(world, posIn, side, ctx);
                }
            }

            if (accurateIn)
            {
                facing = sideIn;
                hitPart = null;
                handleAccurate = true;

                // Pistons, Droppers, Dispensers should face into the block, but Observers should point their back/output
                // side into the block when the Accurate Placement In hotkey is used
                if ((stack.getItem() instanceof BlockItem) == false || ((BlockItem) stack.getItem()).getBlock() != Blocks.OBSERVER)
                {
                    facing = facing.getOpposite();
                }
                //System.out.printf("accurate - IN - facingOrig: %s, facingNew: %s\n", facing, facing.getOpposite());
            }
            else if (flexible == false || rotation == false)
            {
                if (stack.getItem() instanceof BlockItem)
                {

                    BlockHitResult hitResult = new BlockHitResult(hitVec, sideIn, posNew, false);
                    BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));

                    BlockPos posPlacement = getPlacementPositionForTargetedPosition(world, posNew, sideIn, ctx);

                    hitResult = new BlockHitResult(hitVec, sideIn, posPlacement, false);
                    ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));

                    BlockItem item = (BlockItem) stack.getItem();
                    BlockState state = item.getBlock().getStateForPlacement(ctx);

                    // getStateForPlacement can return null in 1.13+
                    if (state == null)
                    {
                        return InteractionResult.PASS;
                    }

                    Optional<Direction> facingTmp = fi.dy.masa.malilib.util.game.BlockUtils.getFirstPropertyFacingValue(state);
                    //System.out.printf("accurate - sideIn: %s, state: %s, hit: %s, f: %s, posNew: %s\n", sideIn, state, hitVec, player.getHorizontalFacing(), posNew);

                    if (facingTmp.isPresent())
                    {
                        facing = facingTmp.get();
                    }
                }
                else
                {
                    facing = player.getDirection();
                }
            }

            if (accurateReverse)
            {
                //System.out.printf("accurate - REVERSE - facingOrig: %s, facingNew: %s\n", facing, facing.getOpposite());
                if (accurateIn || flexible == false || rotation == false)
                {
                    facing = facing.getOpposite();
                }

                hitPart = null;
                handleAccurate = true;
            }

            if ((handleAccurate || afterClicker) && Configs.Generic.ACCURATE_PLACEMENT_PROTOCOL.getBooleanValue())
            {
                // Carpet-Extra mod accurate block placement protocol support
                double relX = hitVec.x - posNew.getX();
                double x = hitVec.x;
                int afterClickerClickCount = Mth.clamp(Configs.Generic.AFTER_CLICKER_CLICK_COUNT.getIntegerValue(), 0, 32);

                if (handleAccurate && fi.dy.masa.malilib.util.game.BlockUtils.isFacingValidForDirection(stack, facing))
                {
                    int protocolValue = 0;
                    int shiftBy = 1;
                    final int facingAdj = (facing.get3DDataValue() * 2);

                    protocolValue |= facing.get3DDataValue() << shiftBy;
                    shiftBy += 3;

                    if (stack.is(ItemTags.TRAPDOORS) || stack.is(ItemTags.STAIRS))
                    {
                        // add BLOCK_HALF handling --> (BOTTOM)
                        int requiredBits = Mth.log2(Mth.smallestEncompassingPowerOfTwo(2));
                        protocolValue |= (1 << shiftBy);
                        shiftBy += requiredBits;
                    }

                    //System.out.printf("prot value (Facing) orig 0x%08X vs 0x%08X\n", facingAdj, protocolValue);

                    x = posNew.getX() + relX + 2 + (protocolValue);
                }
                else if (handleAccurate && fi.dy.masa.malilib.util.game.BlockUtils.isFacingValidForOrientation(stack, facing))
                {
                    int facingIndex = fi.dy.masa.malilib.util.game.BlockUtils.getOrientationFacingIndex(stack, facing);

                    if (facingIndex > 0)
                    {
                        x = posNew.getX() + relX + 2 + (facingIndex * 2);
                    }
                    else
                    {
                        x = posNew.getX() + relX + 2 + (facing.get3DDataValue() * 2);
                    }
                }

                if (afterClicker)
                {
                    x += afterClickerClickCount * 16;
                }

                //System.out.printf("accurate - pre hitVec: %s\n", hitVec);
                //System.out.printf("processRightClickBlockWrapper facing: %s, x: %.3f, pos: %s, side: %s\n", facing, x, posNew, side);
                hitVec = new Vec3(x, hitVec.y, hitVec.z);
                //System.out.printf("accurate - post hitVec: %s\n", hitVec);
            }

            //System.out.printf("accurate - facing: %s, side: %s, posNew: %s, hit: %s\n", facing, side, posNew, hitVec);
            return processRightClickBlockWrapper(controller, player, world, posNew, side, hitVec, hand);
        }

        if (handleFlexible)
        {
            BlockHitResult hitResult = new BlockHitResult(hitVec, side, posNew, false);
            BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));

            if (canPlaceBlockIntoPosition(world, posNew, ctx))
            {
                //System.out.printf("tryPlaceBlock() pos: %s, side: %s, part: %s, hitVec: %s\n", posNew, side, hitPart, hitVec);
                return handleFlexibleBlockPlacement(controller, player, world, posNew, side, playerYaw, hitVec, hand, hitPart);
            }
            else
            {
                return InteractionResult.PASS;
            }
        }

        if (isFirstClick == false && Configs.Generic.FAST_PLACEMENT_REMEMBER_ALWAYS.getBooleanValue())
        {
            return handleFlexibleBlockPlacement(controller, player, world, posIn, sideIn, playerYaw, hitVec, hand, null);
        }

        return processRightClickBlockWrapper(controller, player, world, simpleOffset ? posNew : posIn, sideIn, hitVec, hand);
    }

    private static boolean canPlaceBlockAgainst(Level world, BlockPos pos, Player player, InteractionHand hand)
    {
        if (FeatureToggle.TWEAK_PLACEMENT_REST_FIRST.getBooleanValue())
        {
            BlockState state = world.getBlockState(pos);

            if (stackClickedOn.isEmpty() == false)
            {
                ItemStack stack = ((IMixinAbstractBlock) state.getBlock()).tweakeroo_getPickStack(world, pos, state, false);

                if (fi.dy.masa.malilib.util.InventoryUtils.areStacksEqual(stackClickedOn, stack) == false)
                {
                    return false;
                }
            }
            else
            {
                if (state != stateClickedOn)
                {
                    return false;
                }
            }
        }

        if (FeatureToggle.TWEAK_PLACEMENT_REST_HAND.getBooleanValue())
        {
            BlockState state = world.getBlockState(pos);
            ItemStack stackClicked = ((IMixinAbstractBlock) state.getBlock()).tweakeroo_getPickStack(world, pos, state, false);
            ItemStack stackHand = player.getItemInHand(hand);

            return fi.dy.masa.malilib.util.InventoryUtils.areStacksEqual(stackClicked, stackHand);
        }

        return true;
    }

    public static boolean canUseItemWithRestriction(ItemRestriction restriction, InteractionHand hand, Player player)
    {
        ItemStack stack = player.getItemInHand(hand);
        return canUseItemWithRestriction(restriction, stack);
    }

    public static boolean canUseItemWithRestriction(ItemRestriction restriction, ItemStack stack)
    {
        return stack.isEmpty() || restriction.isAllowed(stack.getItem());
    }

    public static boolean canUseItemWithRestriction(ItemRestriction restriction, Player player)
    {
        return canUseItemWithRestriction(restriction, InteractionHand.MAIN_HAND, player) &&
                canUseItemWithRestriction(restriction, InteractionHand.OFF_HAND, player);
    }

    private static boolean canUseFastRightClick(Player player)
    {
        if (canUseItemWithRestriction(FAST_RIGHT_CLICK_ITEM_RESTRICTION, player) == false)
        {
            return false;
        }

        HitResult trace = player.pick(6, 0f, false);

        if (trace == null || trace.getType() != HitResult.Type.BLOCK)
        {
            return FAST_RIGHT_CLICK_BLOCK_RESTRICTION.isAllowed(Blocks.AIR);
        }

        Block block = player.level().getBlockState(((BlockHitResult) trace).getBlockPos()).getBlock();

        return FAST_RIGHT_CLICK_BLOCK_RESTRICTION.isAllowed(block);
    }

    public static void tryRestockHand(Player player, InteractionHand hand, ItemStack stackOriginal)
    {
        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue() &&
            canUseItemWithRestriction(HAND_RESTOCK_RESTRICTION, stackOriginal))
        {
            ItemStack stackCurrent = player.getItemInHand(hand);

            if (stackOriginal.isEmpty() == false && player.getInventory().getSelectedSlot() == hotbarSlot &&
                (stackCurrent.isEmpty() || ItemStack.isSameItem(stackCurrent, stackOriginal) == false))
            {
                // Don't allow taking stacks from elsewhere in the hotbar, if the cycle tweak is on
                boolean allowHotbar = FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE.getBooleanValue() == false &&
                        FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER.getBooleanValue() == false;

                InventoryUtils.restockNewStackToHand(player, hand, stackOriginal, allowHotbar);
            }
        }
    }

    private static InteractionResult processRightClickBlockWrapper(
            MultiPlayerGameMode controller,
            LocalPlayer player,
            ClientLevel world,
            BlockPos posIn,
            Direction sideIn,
            Vec3 hitVecIn,
            InteractionHand hand)
    {
        // 修复mod拓展手的数量问题
        // Fix for mods that extend the Hand enum (e.g. Accessorify/Accessories)
        // causing ArrayIndexOutOfBoundsException because stackBeforeUse is hardcoded to size 2.
        //System.out.printf("processRightClickBlockWrapper() start @ %s, side: %s, hand: %s\n", posIn, sideIn, hand);
        if (FeatureToggle.TWEAK_PLACEMENT_LIMIT.getBooleanValue() &&
            placementCount >= Configs.Generic.PLACEMENT_LIMIT.getIntegerValue() ||
            hand.ordinal() >= stackBeforeUse.length)
        {
            return InteractionResult.PASS;
        }

        // Don't allow taking stacks from elsewhere in the hotbar, if the cycle tweak is on
        boolean allowHotbar = FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE.getBooleanValue() == false &&
                FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER.getBooleanValue() == false;

        InventoryUtils.preRestockHand(player, hand, allowHotbar);

        // We need to grab the stack here if the cached stack is still empty,
        // because this code runs before the cached stack gets set on the first click/use.
        BlockHitResult hitResult = new BlockHitResult(hitVecIn, sideIn, posIn, false);
        BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, hand, hitResult));
        BlockPos posPlacement = getPlacementPositionForTargetedPosition(world, posIn, sideIn, ctx);
        BlockState stateBefore = world.getBlockState(posPlacement);
        BlockState state = world.getBlockState(posIn);
        ItemStack stackOriginal;

        if (stackBeforeUse[hand.ordinal()].isEmpty() == false &&
            FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE.getBooleanValue() == false &&
            FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER.getBooleanValue() == false)
        {
            stackOriginal = stackBeforeUse[hand.ordinal()];
        }
        else
        {
            stackOriginal = player.getItemInHand(hand).copy();
        }

        if (FeatureToggle.TWEAK_PLACEMENT_RESTRICTION.getBooleanValue() &&
            state.canBeReplaced(ctx) == false && state.canBeReplaced())
        {
            // If the block itself says it's not replaceable, but the material is (fluids),
            // then we need to offset the position back, otherwise the check in ItemBlock
            // will offset the position by one forward from the desired position.
            // FIXME This will break if the block behind the desired position is replaceable though... >_>
            posIn = posIn.relative(sideIn.getOpposite());
        }

        if (posFirst != null && isPositionAllowedByPlacementRestriction(posIn, sideIn) == false)
        {
            //System.out.printf("processRightClickBlockWrapper() PASS @ %s, side: %s\n", posIn, sideIn);
            return InteractionResult.PASS;
        }

        final int afterClickerClickCount = Mth.clamp(Configs.Generic.AFTER_CLICKER_CLICK_COUNT.getIntegerValue(), 0, 32);

        Direction facing = sideIn;
        boolean flexible = FeatureToggle.TWEAK_FLEXIBLE_BLOCK_PLACEMENT.getBooleanValue();
        boolean rotationHeld = Hotkeys.FLEXIBLE_BLOCK_PLACEMENT_ROTATION.getKeybind().isKeybindHeld();
        boolean rememberFlexible = Configs.Generic.REMEMBER_FLEXIBLE.getBooleanValue();
        boolean rotation = rotationHeld || (rememberFlexible && firstWasRotation);
        boolean accurate = FeatureToggle.TWEAK_ACCURATE_BLOCK_PLACEMENT.getBooleanValue();
        boolean keys = Hotkeys.ACCURATE_BLOCK_PLACEMENT_IN.getKeybind().isKeybindHeld() || Hotkeys.ACCURATE_BLOCK_PLACEMENT_REVERSE.getKeybind().isKeybindHeld();
        accurate = accurate && keys;

        // Carpet-Extra mod accurate block placement protocol support
        if (flexible && rotation && accurate == false &&
            Configs.Generic.ACCURATE_PLACEMENT_PROTOCOL.getBooleanValue() &&
            fi.dy.masa.malilib.util.game.BlockUtils.isFacingValidForDirection(stackOriginal, facing))
        {
            facing = facing.getOpposite(); // go from block face to click on to the requested facing
            //double relX = hitVecIn.x - posIn.getX();
            //double x = posIn.getX() + relX + 2 + (facing.getId() * 2);
            int protocolValue = 0;
            int shiftBy = 1;
            final int facingAdj = (facing.get3DDataValue() * 2);

            protocolValue |= facing.get3DDataValue() << shiftBy;
            shiftBy += 3;

            if (stackOriginal.is(ItemTags.TRAPDOORS) || stackOriginal.is(ItemTags.STAIRS))
            {
                // add BLOCK_HALF handling --> (BOTTOM)
                int requiredBits = Mth.log2(Mth.smallestEncompassingPowerOfTwo(2));
                protocolValue |= (1 << shiftBy);
                shiftBy += requiredBits;
            }

            //System.out.printf("prot value (Facing) orig 0x%08X vs 0x%08X\n", facingAdj, protocolValue);
//           double x = posIn.getX() + 2 + (facing.getIndex() * 2);
            double x = posIn.getX() + 2 + (protocolValue);

            if (FeatureToggle.TWEAK_AFTER_CLICKER.getBooleanValue())
            {
                x += afterClickerClickCount * 16;
            }

            //System.out.printf("processRightClickBlockWrapper/Direction req facing: %s, x: %.3f, pos: %s, sideIn: %s\n", facing, x, posIn, sideIn);
            hitVecIn = new Vec3(x, hitVecIn.y, hitVecIn.z);
        }
        else if (flexible && rotation && accurate == false &&
                Configs.Generic.ACCURATE_PLACEMENT_PROTOCOL.getBooleanValue() &&
                fi.dy.masa.malilib.util.game.BlockUtils.isFacingValidForOrientation(stackOriginal, facing))
        {
            facing = facing.getOpposite(); // go from block face to click on to the requested facing
            //double relX = hitVecIn.x - posIn.getX();
            //double x = posIn.getX() + relX + 2 + (facing.getId() * 2);

            int facingIndex = fi.dy.masa.malilib.util.game.BlockUtils.getOrientationFacingIndex(stackOriginal, facing);
            double x;
            if (facingIndex >= 0)
            {
                x = posIn.getX() + 2 + (facingIndex * 2);
            }
            else
            {
                x = posIn.getX() + 2 + (facing.get3DDataValue() * 2);
            }

            if (FeatureToggle.TWEAK_AFTER_CLICKER.getBooleanValue())
            {
                x += afterClickerClickCount * 16;
            }

            //System.out.printf("processRightClickBlockWrapper/Orientation req facing: %s, x: %.3f, pos: %s, sideIn: %s\n", facing, x, posIn, sideIn);
            hitVecIn = new Vec3(x, hitVecIn.y, hitVecIn.z);
        }

        if (FeatureToggle.TWEAK_Y_MIRROR.getBooleanValue() && Hotkeys.PLACEMENT_Y_MIRROR.getKeybind().isKeybindHeld())
        {
            double y = 1 - hitVecIn.y + 2 * posIn.getY(); // = 1 - (hitVec.y - pos.getY()) + pos.getY();
            hitVecIn = new Vec3(hitVecIn.x, y, hitVecIn.z);

            if (sideIn.getAxis() == Direction.Axis.Y)
            {
                posIn = posIn.relative(sideIn);
                sideIn = sideIn.getOpposite();
            }
        }

        if (FeatureToggle.TWEAK_PICK_BEFORE_PLACE.getBooleanValue())
        {
            InventoryUtils.switchToPickedBlock();
        }

        InventoryUtils.trySwapCurrentToolIfNearlyBroken();

        //System.out.printf("processRightClickBlockWrapper() pos: %s, side: %s, hitVec: %s\n", posIn, sideIn, hitVecIn);
        InteractionResult result;

        if (InventoryUtils.canUnstackingItemNotFitInInventory(stackOriginal, player))
        {
            result = InteractionResult.PASS;
        }
        else
        {
            //System.out.printf("processRightClickBlockWrapper() PLACE @ %s, side: %s, hit: %s\n", posIn, sideIn, hitVecIn);
            BlockHitResult context = new BlockHitResult(hitVecIn, sideIn, posIn, false);
            result = controller.useItemOn(player, hand, context);
        }

        if (result == InteractionResult.SUCCESS)
        {
            placementCount++;
        }

        // This restock needs to happen even with the pick-before-place tweak active,
        // otherwise the fast placement mode's checks (getHandWithItem()) will fail...
        //System.out.printf("processRightClickBlockWrapper -> tryRestockHand with: %s, current: %s\n", stackOriginal, player.getStackInHand(hand));
        tryRestockHand(player, hand, stackOriginal);

        if (FeatureToggle.TWEAK_AFTER_CLICKER.getBooleanValue() &&
            Configs.Generic.ACCURATE_PLACEMENT_PROTOCOL.getBooleanValue() == false &&
            world.getBlockState(posPlacement) != stateBefore)
        {
            // TODO --> Add EasyPlacement handling?
            for (int i = 0; i < afterClickerClickCount; i++)
            {
                //System.out.printf("processRightClickBlockWrapper() after-clicker - i: %d, pos: %s, side: %s, hitVec: %s\n", i, posPlacement, sideIn, hitVecIn);
                BlockHitResult context = new BlockHitResult(hitVecIn, sideIn, posPlacement, false);
                result = controller.useItemOn(player, hand, context);
            }
        }

        if (result == InteractionResult.SUCCESS)
        {
            Inventory inv = player.getInventory();

            if (FeatureToggle.TWEAK_HOTBAR_SLOT_CYCLE.getBooleanValue())
            {
                int newSlot = inv.getSelectedSlot() + 1;

                if (newSlot >= 9 || newSlot >= Configs.Generic.HOTBAR_SLOT_CYCLE_MAX.getIntegerValue())
                {
                    newSlot = 0;
                }

                inv.setSelectedSlot(newSlot);
            }
            else if (FeatureToggle.TWEAK_HOTBAR_SLOT_RANDOMIZER.getBooleanValue())
            {
                inv.setSelectedSlot(player.getRandom().nextInt(Configs.Generic.HOTBAR_SLOT_RANDOMIZER_MAX.getIntegerValue()));
            }
        }

        return result;
    }

    private static InteractionResult handleFlexibleBlockPlacement(
            MultiPlayerGameMode controller,
            LocalPlayer player,
            ClientLevel world,
            BlockPos pos,
            Direction side,
            float playerYaw,
            Vec3 hitVec,
            InteractionHand hand,
            @Nullable PositionUtils.HitPart hitPart)
    {
        Direction facing = Direction.from2DDataValue(Mth.floor((playerYaw * 4.0F / 360.0F) + 0.5D) & 3);
        Direction facingOrig = facing;
        float yawOrig = player.getYRot();

        if (hitPart == PositionUtils.HitPart.CENTER)
        {
            facing = facing.getOpposite();
        }
        else if (hitPart == PositionUtils.HitPart.LEFT)
        {
            facing = facing.getCounterClockWise();
        }
        else if (hitPart == PositionUtils.HitPart.RIGHT)
        {
            facing = facing.getClockWise();
        }

        float yaw = facing.toYRot();
        float pitch = player.getXRot();
        player.setYRot(yaw);
        player.connection.send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(), false));

        //System.out.printf("handleFlexibleBlockPlacement() pos: %s, side: %s, facing orig: %s facing new: %s\n", pos, side, facingOrig, facing);
        InteractionResult result = processRightClickBlockWrapper(controller, player, world, pos, side, hitVec, hand);

        player.setYRot(yawOrig);
        player.connection.send(new ServerboundMovePlayerPacket.Rot(yawOrig, pitch, player.onGround(), false));

        return result;
    }

    private static void clearClickedBlockInfoUse()
    {
        posFirst = null;
        hitPartFirst = null;
        hitVecFirst = null;
        sideFirst = null;
        sideRotatedFirst = null;
        firstWasRotation = false;
        firstWasOffset = false;
        isFirstClick = true;
        placementCount = 0;
        stackClickedOn = ItemStack.EMPTY;
        stateClickedOn = null;
    }

    private static void clearClickedBlockInfoAttack()
    {
        posFirstBreaking = null;
        sideFirstBreaking = null;
    }

    private static Direction getRotatedFacing(Direction originalSide, Direction playerFacingH, PositionUtils.HitPart hitPart)
    {
        if (originalSide.getAxis().isVertical())
        {
            return switch (hitPart)
            {
                case LEFT -> playerFacingH.getClockWise();
                case RIGHT -> playerFacingH.getCounterClockWise();
                case BOTTOM -> originalSide == Direction.UP ? playerFacingH : playerFacingH.getOpposite();
                case TOP -> originalSide == Direction.DOWN ? playerFacingH : playerFacingH.getOpposite();
                case CENTER -> originalSide.getOpposite();
            };
        }
        else
        {
            return switch (hitPart)
            {
                case LEFT -> originalSide.getCounterClockWise();
                case RIGHT -> originalSide.getClockWise();
                case BOTTOM -> Direction.UP;
                case TOP -> Direction.DOWN;
                case CENTER -> originalSide.getOpposite();
            };
        }
    }

    private static boolean isPositionAllowedByPlacementRestriction(BlockPos pos, Direction side)
    {
        boolean restrictionEnabled = FeatureToggle.TWEAK_PLACEMENT_RESTRICTION.getBooleanValue();
        boolean gridEnabled = FeatureToggle.TWEAK_PLACEMENT_GRID.getBooleanValue();

        if (restrictionEnabled == false && gridEnabled == false)
        {
            return true;
        }

        int gridSize = Configs.Generic.PLACEMENT_GRID_SIZE.getIntegerValue();
        PlacementRestrictionMode mode = (PlacementRestrictionMode) Configs.Generic.PLACEMENT_RESTRICTION_MODE.getOptionListValue();

        return isPositionAllowedByRestrictions(pos, side, posFirst, sideFirst, restrictionEnabled, mode, gridEnabled, gridSize);
    }

    public static boolean isPositionAllowedByBreakingRestriction(BlockPos pos, Direction side)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;

        if (world != null && FeatureToggle.TWEAK_BLOCK_TYPE_BREAK_RESTRICTION.getBooleanValue())
        {
            BlockState state = world.getBlockState(pos);

            if (BLOCK_TYPE_BREAK_RESTRICTION.isAllowed(state.getBlock()) == false)
            {
                MessageOutputType type = (MessageOutputType) Configs.Generic.BLOCK_TYPE_BREAK_RESTRICTION_WARN.getOptionListValue();

                if (type == MessageOutputType.MESSAGE)
                {
                    InfoUtils.showGuiOrInGameMessage(Message.MessageType.WARNING, "tweakeroo.message.warning.block_type_break_restriction");
                }
                else if (type == MessageOutputType.ACTIONBAR)
                {
                    InfoUtils.printActionbarMessage("tweakeroo.message.warning.block_type_break_restriction");
                }

                return false;
            }
        }

        boolean restrictionEnabled = FeatureToggle.TWEAK_BREAKING_RESTRICTION.getBooleanValue();
        boolean gridEnabled = FeatureToggle.TWEAK_BREAKING_GRID.getBooleanValue();

        if (restrictionEnabled == false && gridEnabled == false)
        {
            return true;
        }

        int gridSize = Configs.Generic.BREAKING_GRID_SIZE.getIntegerValue();
        PlacementRestrictionMode mode = (PlacementRestrictionMode) Configs.Generic.BREAKING_RESTRICTION_MODE.getOptionListValue();

        return posFirstBreaking == null || isPositionAllowedByRestrictions(pos, side, posFirstBreaking, sideFirstBreaking, restrictionEnabled, mode, gridEnabled, gridSize);
    }

    private static boolean isPositionAllowedByRestrictions(BlockPos pos, Direction side,
                                                           BlockPos posFirst, Direction sideFirst, boolean restrictionEnabled, PlacementRestrictionMode mode, boolean gridEnabled, int gridSize)
    {
        if (gridEnabled)
        {
            if ((Math.abs(pos.getX() - posFirst.getX()) % gridSize) != 0 ||
                (Math.abs(pos.getY() - posFirst.getY()) % gridSize) != 0 ||
                (Math.abs(pos.getZ() - posFirst.getZ()) % gridSize) != 0)
            {
                return false;
            }
        }

        if (restrictionEnabled)
        {
            return switch (mode)
            {
                case COLUMN -> isNewPositionValidForColumnMode(pos, posFirst, sideFirst);
                case DIAGONAL -> isNewPositionValidForDiagonalMode(pos, posFirst, sideFirst);
                case FACE -> isNewPositionValidForFaceMode(pos, side, sideFirst);
                case LAYER -> isNewPositionValidForLayerMode(pos, posFirst, sideFirst);
                case LINE -> isNewPositionValidForLineMode(pos, posFirst, sideFirst);
                case PLANE -> isNewPositionValidForPlaneMode(pos, posFirst, sideFirst);
            };
        }
        else
        {
            return true;
        }
    }

    private static BlockPos getPlacementPositionForTargetedPosition(Level world, BlockPos pos, Direction side, BlockPlaceContext useContext)
    {
        if (canPlaceBlockIntoPosition(world, pos, useContext))
        {
            return pos;
        }

        return pos.relative(side);
    }

    @SuppressWarnings({"deprecation"})
    private static boolean canPlaceBlockIntoPosition(Level world, BlockPos pos, BlockPlaceContext useContext)
    {
        BlockState state = world.getBlockState(pos);
        // FIXME - state.getFluidState().equals(Fluids.EMPTY.getDefaultState()) -- could work
        return state.canBeReplaced(useContext) || state.liquid() || state.canBeReplaced();
    }

    private static boolean isNewPositionValidForColumnMode(BlockPos posNew, BlockPos posFirst, Direction sideFirst)
    {
        Direction.Axis axis = sideFirst.getAxis();

        return switch (axis)
        {
            case X -> posNew.getY() == posFirst.getY() && posNew.getZ() == posFirst.getZ();
            case Y -> posNew.getX() == posFirst.getX() && posNew.getZ() == posFirst.getZ();
            case Z -> posNew.getX() == posFirst.getX() && posNew.getY() == posFirst.getY();
        };
    }

    private static boolean isNewPositionValidForDiagonalMode(BlockPos posNew, BlockPos posFirst, Direction sideFirst)
    {
        Direction.Axis axis = sideFirst.getAxis();
        BlockPos relativePos = posNew.subtract(posFirst);

        return switch (axis)
        {
            case X -> posNew.getX() == posFirst.getX() && Math.abs(relativePos.getY()) == Math.abs(relativePos.getZ());
            case Y -> posNew.getY() == posFirst.getY() && Math.abs(relativePos.getX()) == Math.abs(relativePos.getZ());
            case Z -> posNew.getZ() == posFirst.getZ() && Math.abs(relativePos.getX()) == Math.abs(relativePos.getY());
        };
    }

    private static boolean isNewPositionValidForFaceMode(BlockPos posNew, Direction side, Direction sideFirst)
    {
        return side == sideFirst;
    }

    private static boolean isNewPositionValidForLayerMode(BlockPos posNew, BlockPos posFirst, Direction sideFirst)
    {
        int height = Configs.Generic.RESTRICTION_LAYER_HEIGHT.getIntegerValue();

        if (height > 0)
        {
            int diff = posNew.getY() - posFirst.getY() + 1;

            return diff > 0 && diff <= height;
        }
        else if (height < 0)
        {
            int diff = posFirst.getY() - posNew.getY() + 1;

            return diff > 0 && diff <= -height;
        }

        return true;
    }

    private static boolean isNewPositionValidForLineMode(BlockPos posNew, BlockPos posFirst, Direction sideFirst)
    {
        Direction.Axis axis = sideFirst.getAxis();

        return switch (axis)
        {
            case X ->
                    posNew.getX() == posFirst.getX() && (posNew.getY() == posFirst.getY() || posNew.getZ() == posFirst.getZ());
            case Y ->
                    posNew.getY() == posFirst.getY() && (posNew.getX() == posFirst.getX() || posNew.getZ() == posFirst.getZ());
            case Z ->
                    posNew.getZ() == posFirst.getZ() && (posNew.getX() == posFirst.getX() || posNew.getY() == posFirst.getY());
        };
    }

    private static boolean isNewPositionValidForPlaneMode(BlockPos posNew, BlockPos posFirst, Direction sideFirst)
    {
        Direction.Axis axis = sideFirst.getAxis();

        return switch (axis)
        {
            case X -> posNew.getX() == posFirst.getX();
            case Y -> posNew.getY() == posFirst.getY();
            case Z -> posNew.getZ() == posFirst.getZ();
        };
    }

    /*
    @Nullable
    private static Direction getPlayerMovementDirection(PlayerEntitySP player)
    {
        double dx = player.posX - playerPosLast.x;
        double dy = player.posY - playerPosLast.y;
        double dz = player.posZ - playerPosLast.z;
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);

        if (Math.max(Math.max(ax, az), ay) < 0.001)
        {
            return null;
        }

        if (ax > az)
        {
            if (ax > ay)
            {
                return dx > 0 ? Direction.EAST : Direction.WEST;
            }
            else
            {
                return dy > 0 ? Direction.UP : Direction.DOWN;
            }
        }
        else
        {
            if (az > ay)
            {
                return dz > 0 ? Direction.SOUTH : Direction.NORTH;
            }
            else
            {
                return dy > 0 ? Direction.UP : Direction.DOWN;
            }
        }
    }

    @Nullable
    private static Hand getHandWithItem(ItemStack stack, PlayerEntitySP player)
    {
        if (InventoryUtils.areStacksEqualIgnoreDurability(player.getHeldItemMainhand(), stackFirst))
        {
            return Hand.MAIN;
        }

        if (InventoryUtils.areStacksEqualIgnoreDurability(player.getHeldItemOffhand(), stackFirst))
        {
            return Hand.OFF;
        }

        return null;
    }
    */

    public static boolean shouldSkipSlotSync(int slotNumber, ItemStack newStack)
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        AbstractContainerMenu container = player != null ? player.containerMenu : null;

        if (Configs.Generic.SLOT_SYNC_WORKAROUND.getBooleanValue() &&
                FeatureToggle.TWEAK_PICK_BEFORE_PLACE.getBooleanValue() == false &&
                container != null && container == player.inventoryMenu &&
                (slotNumber == 45 || (slotNumber - 36) == player.getInventory().getSelectedSlot()))
        {
            if (mc.options.keyUse.isDown() &&
                    (Configs.Generic.SLOT_SYNC_WORKAROUND_ALWAYS.getBooleanValue() ||
                            FeatureToggle.TWEAK_FAST_BLOCK_PLACEMENT.getBooleanValue() ||
                            FeatureToggle.TWEAK_FAST_RIGHT_CLICK.getBooleanValue()))
            {
                return true;
            }

            return mc.options.keyAttack.isDown() && FeatureToggle.TWEAK_FAST_LEFT_CLICK.getBooleanValue();
        }

        return false;
    }
}
