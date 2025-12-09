package fi.dy.masa.tweakeroo.mixin.hud;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

@Mixin(value = Gui.class, priority = 1001)
public abstract class MixinInGameHud
{
    @Shadow @Final private PlayerTabOverlay tabList;
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderTabList",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;setVisible(Z)V",
                     ordinal = 1, shift = At.Shift.AFTER))
    private void tweakeroo_alwaysRenderPlayerList(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_PLAYER_LIST_ALWAYS_ON.getBooleanValue() && this.minecraft.level != null)
        {
            Scoreboard scoreboard = this.minecraft.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);

            this.tabList.setVisible(true);
            this.tabList.render(context, context.guiWidth(), scoreboard, objective);
        }
    }

    @Inject(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/scores/Objective;)V",
            at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableScoreboardRendering(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_SCOREBOARD_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableStatusEffectHudRendering(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_STATUS_EFFECT_HUD.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
