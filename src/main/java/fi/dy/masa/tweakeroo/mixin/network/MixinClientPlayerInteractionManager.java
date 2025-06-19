package fi.dy.masa.tweakeroo.mixin.network;

import org.objectweb.asm.Opcodes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import fi.dy.masa.tweakeroo.util.InventoryUtils;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager
{
    @Shadow @Final private MinecraftClient client;
    @Shadow private int blockBreakingCooldown;

    @Shadow public abstract ActionResult interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);

    @Inject(method = "interactItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V"),
            cancellable = true)
    private void onProcessRightClickFirst(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        if (CameraUtils.shouldPreventPlayerInputs() ||
            PlacementTweaks.onProcessRightClickPre(player, hand))
        {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
        }
    }

    @Inject(method = "interactItem",
            at = @At("TAIL"))
    private void onProcessRightClickPost(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        if (cir.getReturnValue().isAccepted())
        {
            PlacementTweaks.onProcessRightClickPost(player, hand);
        }
    }

    @Inject(method = "interactEntity(" +
                     "Lnet/minecraft/entity/player/PlayerEntity;" +
                     "Lnet/minecraft/entity/Entity;" +
                     "Lnet/minecraft/util/Hand;" +
                     ")Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void onRightClickMouseOnEntityPre1(PlayerEntity player, Entity target, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        if (CameraUtils.shouldPreventPlayerInputs() ||
            PlacementTweaks.onProcessRightClickPre(player, hand))
        {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactEntityAtLocation(" +
                     "Lnet/minecraft/entity/player/PlayerEntity;" +
                     "Lnet/minecraft/entity/Entity;" +
                     "Lnet/minecraft/util/hit/EntityHitResult;" +
                     "Lnet/minecraft/util/Hand;" +
                     ")Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void onRightClickMouseOnEntityPre2(PlayerEntity player, Entity target, EntityHitResult trace, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        if (CameraUtils.shouldPreventPlayerInputs() ||
            PlacementTweaks.onProcessRightClickPre(player, hand))
        {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void preventEntityAttacksInFreeCameraMode(PlayerEntity player, Entity target, CallbackInfo ci)
    {
        if (CameraUtils.shouldPreventPlayerInputs())
        {
            ci.cancel();
        }
        else if (FeatureToggle.TWEAK_ENTITY_TYPE_ATTACK_RESTRICTION.getBooleanValue() &&
                 MiscTweaks.isEntityAllowedByAttackingRestriction(target.getType()) == false)
        {
            ci.cancel();
        }
        else if (FeatureToggle.TWEAK_WEAPON_SWITCH.getBooleanValue())
        {
            InventoryUtils.trySwitchToWeapon(target);
        }
    }

    @Inject(method = "attackBlock",
            slice = @Slice(from = @At(value = "FIELD", ordinal = 0,
                                      target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakingBlock:Z",
                                      opcode = Opcodes.GETFIELD)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getBlockState(" +
                                                "Lnet/minecraft/util/math/BlockPos;" +
                                                ")Lnet/minecraft/block/BlockState;",
                     ordinal = 0))
    private void onClickBlockPre(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir)
    {
        if (this.client.player != null && this.client.world != null)
        {
            if (FeatureToggle.TWEAK_TOOL_SWITCH.getBooleanValue())
            {
                InventoryUtils.trySwitchToEffectiveTool(pos);
            }

            PlacementTweaks.cacheStackInHand(Hand.MAIN_HAND);
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void handleBreakingRestriction1(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue() || CameraUtils.shouldPreventPlayerInputs() ||
            PlacementTweaks.isPositionAllowedByBreakingRestriction(pos, side) == false)
        {
            cir.setReturnValue(false);
        }
        else
        {
            InventoryUtils.trySwapCurrentToolIfNearlyBroken();
        }
    }

    // can't just inject into breakBlock's RETURN directly anymore since it's now synced by sendSequencedPacket
    @Inject(method = "attackBlock",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;" +
                              "sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"
            ))
    private void handleBreakReplaceInAttack(BlockPos targetPos, Direction side, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_BREAK_REPLACE.getBooleanValue() &&
            this.client.world != null && this.client.player != null)
        {
            if (this.client.world.getBlockState(targetPos).isAir()) {
                BlockHitResult blockHitResult = new BlockHitResult(targetPos.toCenterPos(), side, targetPos, false);
                for (Hand hand : Hand.values())
                {
                    ItemStack stack = this.client.player.getStackInHand(hand);
                    if (stack != null && stack.getItem() instanceof BlockItem
                        && this.interactBlock(this.client.player, hand, blockHitResult).isAccepted()
                    )
                    {
                        // set a cooldown of 1 tick for survival mode instant mining
                        if (!this.client.player.getAbilities().creativeMode)
                        {
                            this.blockBreakingCooldown = 1;
                        }
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "updateBlockBreakingProgress",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;" +
                              "sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"
            ))
    private void handleBreakReplaceInUpdate(BlockPos targetPos, Direction side, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_BREAK_REPLACE.getBooleanValue() &&
            this.client.world != null && this.client.player != null)
        {
            if (this.client.world.getBlockState(targetPos).isAir()) {
                BlockHitResult blockHitResult = new BlockHitResult(targetPos.toCenterPos(), side, targetPos, false);
                for (Hand hand : Hand.values())
                {
                    ItemStack stack = this.client.player.getStackInHand(hand);
                    if (stack != null && stack.getItem() instanceof BlockItem
                        && this.interactBlock(this.client.player, hand, blockHitResult).isAccepted())
                    {
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true) // MCP: onPlayerDamageBlock
    private void handleBreakingRestriction2(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir)
    {
        if (Configs.Disable.DISABLE_BLOCK_BREAK_COOLDOWN.getBooleanValue())
            //&& this.client.player.isCreative() == false)
        {
            this.blockBreakingCooldown = 0;
        }

        if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue() || CameraUtils.shouldPreventPlayerInputs() ||
            PlacementTweaks.isPositionAllowedByBreakingRestriction(pos, side) == false)
        {
            cir.setReturnValue(true);
        }
        else
        {
            InventoryUtils.trySwapCurrentToolIfNearlyBroken();
        }
    }

    @Inject(method = "hasLimitedAttackSpeed", at = @At("HEAD"), cancellable = true)
    private void overrideLimitedAttackSpeed(CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_FAST_LEFT_CLICK.getBooleanValue())
        {
            cir.setReturnValue(false);
        }
    }
}
