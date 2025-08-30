package fi.dy.masa.tweakeroo.mixin.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(value = InGameHud.class, priority = 1001)
public abstract class MixinInGameHud
{
    @Shadow @Final private PlayerListHud playerListHud;
    @Shadow @Final private MinecraftClient client;

//    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE",
//                target = "Lnet/minecraft/client/gui/hud/InGameHud;shouldRenderCrosshair()Z", ordinal = 0), cancellable = true)
//    private void tweakeroo_overrideCursorRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
//    {
//        if (FeatureToggle.TWEAK_F3_CURSOR.getBooleanValue() && this.client.currentScreen == null)
//        {
//            RenderUtils.renderDirectionsCursor(context);
//            ci.cancel();
//        }
//    }

    @Inject(method = "renderPlayerList",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/hud/PlayerListHud;setVisible(Z)V",
                     ordinal = 1, shift = At.Shift.AFTER))
    private void tweakeroo_alwaysRenderPlayerList(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_PLAYER_LIST_ALWAYS_ON.getBooleanValue() && this.client.world != null)
        {
            Scoreboard scoreboard = this.client.world.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);

            this.playerListHud.setVisible(true);
            this.playerListHud.render(context, context.getScaledWindowWidth(), scoreboard, objective);
        }
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableScoreboardRendering(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_SCOREBOARD_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableStatusEffectHudRendering(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_STATUS_EFFECT_HUD.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
