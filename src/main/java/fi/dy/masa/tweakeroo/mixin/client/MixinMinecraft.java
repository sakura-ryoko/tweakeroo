package fi.dy.masa.tweakeroo.mixin.client;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.platform.InputConstants;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.MiscTweaks;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.tweaks.RenderTweaks;
import fi.dy.masa.tweakeroo.util.IMinecraftClientInvoker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMinecraftClientInvoker
{
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public ClientLevel level;
    @Shadow @Nullable public Screen screen;
    @Shadow @Final public Options options;
    @Shadow private int rightClickDelay;
    @Shadow protected int missTime;

    @Shadow private boolean startAttack() {return false;}
    @Shadow private void startUseItem() {}

    @Override
    public void tweakeroo_setItemUseCooldown(int value)
    {
        this.rightClickDelay = value;
    }

    @Override
    public boolean tweakeroo_invokeDoAttack()
    {
        return this.startAttack();
    }

    @Override
    public void tweakeroo_invokeDoItemUse()
    {
        this.startUseItem();
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void onGameLoop(boolean renderWorld, CallbackInfo ci)
    {
        if (this.player != null && this.level != null)
        {
            MiscTweaks.onGameLoop((Minecraft) (Object) this);
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onLeftClickMouse(CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue())
        {
            RenderTweaks.select(false);
            cir.cancel();
            return;
        }
    }

    /**
     * Copied From Tweak Fork by Andrew54757
     */
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void onRightClickMouse(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_AREA_SELECTOR.getBooleanValue())
        {
            RenderTweaks.select(true);
            ci.cancel();
            return;
        }
    }

    @Inject(method = "startAttack", at = {
            @At(value = "INVOKE",
                target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"),
            @At(value = "INVOKE",
                target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z")
    })
    private void onLeftClickMousePre(CallbackInfoReturnable<Boolean> cir)
    {
        PlacementTweaks.onLeftClickMousePre();
    }

    @Inject(method = "startAttack", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void onLeftClickMousePost(CallbackInfoReturnable<Boolean> cir)
    {
        PlacementTweaks.onLeftClickMousePost();
    }

    @Redirect(method = "startUseItem()V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult onProcessRightClickBlock(
            MultiPlayerGameMode controller,
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult hitResult)
    {
        return PlacementTweaks.onProcessRightClickBlock(controller, player, this.level, hand, hitResult);
    }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void onProcessKeybindsPre(CallbackInfo ci)
    {
        if (this.screen == null)
        {
            if (FeatureToggle.TWEAK_HOLD_ATTACK.getBooleanValue())
            {
                // Opening a GUI sets the cooldown to 10000, and it won't have a chance
                // to get reset normally when this tweak is active.
                if (this.missTime >= 10000)
                {
                    this.missTime = 0;
                }

                KeyMapping.set(InputConstants.getKey(this.options.keyAttack.saveString()), true);
            }

            if (FeatureToggle.TWEAK_HOLD_USE.getBooleanValue())
            {
                KeyMapping.set(InputConstants.getKey(this.options.keyUse.saveString()), true);
            }
        }
    }
}
