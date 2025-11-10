package fi.dy.masa.tweakeroo.mixin.network;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import fi.dy.masa.tweakeroo.util.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinClientPlayerInteractionManager
{
    @Shadow @Final private Minecraft minecraft;
    @Shadow private int destroyDelay;

    @Shadow public abstract InteractionResult useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult);

    @Inject(method = "useItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;ensureHasSentCarriedItem()V"),
            cancellable = true)
    private void tweakeroo_onProcessRightClickFirst(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (CameraUtils.shouldPreventPlayerInputs() ||
            PlacementTweaks.onProcessRightClickPre(player, hand))
        {
            cir.setReturnValue(InteractionResult.PASS);
            cir.cancel();
        }
    }

    @Inject(method = "useItem",
            at = @At("TAIL"))
    private void tweakeroo_onProcessRightClickPost(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (cir.getReturnValue().consumesAction())
        {
            PlacementTweaks.onProcessRightClickPost(player, hand);
        }
    }

    @Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void tweakeroo_onRightClickMouseOnEntityPre1(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (CameraUtils.shouldPreventPlayerInputs() ||
            PlacementTweaks.onProcessRightClickPre(player, hand))
        {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void tweakeroo_onRightClickMouseOnEntityPre2(Player player, Entity target, EntityHitResult trace, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (CameraUtils.shouldPreventPlayerInputs() ||
            PlacementTweaks.onProcessRightClickPre(player, hand))
        {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_preventEntityAttacksInFreeCameraMode(Player player, Entity target, CallbackInfo ci)
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

    @Inject(method = "startDestroyBlock",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                     ordinal = 2))
    private void tweakeroo_onClickBlockPre(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir)
    {
        if (this.minecraft.player != null && this.minecraft.level != null)
        {
            if (FeatureToggle.TWEAK_TOOL_SWITCH.getBooleanValue())
            {
                InventoryUtils.trySwitchToEffectiveTool(pos);
            }

            PlacementTweaks.cacheStackInHand(InteractionHand.MAIN_HAND);
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_handleBreakingRestriction1(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir)
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
    @Inject(method = "startDestroyBlock",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"
            ))
    private void tweakeroo_handleBreakReplaceInAttack(BlockPos targetPos, Direction side, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_BREAK_REPLACE.getBooleanValue() &&
			this.minecraft.level != null && this.minecraft.player != null)
        {
            if (this.minecraft.level.getBlockState(targetPos).isAir()) {
                BlockHitResult blockHitResult = new BlockHitResult(targetPos.getCenter(), side, targetPos, false);
                for (InteractionHand hand : InteractionHand.values())
                {
                    ItemStack stack = this.minecraft.player.getItemInHand(hand);
                    if (stack != null && stack.getItem() instanceof BlockItem
                        && this.useItemOn(this.minecraft.player, hand, blockHitResult).consumesAction()
                    )
                    {
                        // set a cooldown of 1 tick for survival mode instant mining
                        if (!this.minecraft.player.getAbilities().instabuild)
                        {
                            this.destroyDelay = 1;
                        }
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "continueDestroyBlock",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"
            ))
    private void tweakeroo_handleBreakReplaceInUpdate(BlockPos targetPos, Direction side, CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_BREAK_REPLACE.getBooleanValue() &&
			this.minecraft.level != null && this.minecraft.player != null)
        {
            if (this.minecraft.level.getBlockState(targetPos).isAir()) {
                BlockHitResult blockHitResult = new BlockHitResult(targetPos.getCenter(), side, targetPos, false);
                for (InteractionHand hand : InteractionHand.values())
                {
                    ItemStack stack = this.minecraft.player.getItemInHand(hand);
                    if (stack != null && stack.getItem() instanceof BlockItem
                        && this.useItemOn(this.minecraft.player, hand, blockHitResult).consumesAction())
                    {
                        return;
                    }
                }
            }
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true) // MCP: onPlayerDamageBlock
    private void tweakeroo_handleBreakingRestriction2(BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir)
    {
        if (Configs.Disable.DISABLE_BLOCK_BREAK_COOLDOWN.getBooleanValue())
            //&& this.client.player.isCreative() == false)
        {
            this.destroyDelay = 0;
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

    @Inject(method = "hasMissTime", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_overrideLimitedAttackSpeed(CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_FAST_LEFT_CLICK.getBooleanValue())
        {
            cir.setReturnValue(false);
        }
    }
}
