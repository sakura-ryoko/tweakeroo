package fi.dy.masa.tweakeroo.mixin.hud;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = StatusEffectsDisplay.class, priority = 1001)
public abstract class MixinStatusEffectsDisplay
{
    @Shadow private @Nullable StatusEffectInstance hoveredStatusEffect;

    @Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableStatusEffectRendering1(DrawContext context, int mouseX, int mouseY, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_INVENTORY_EFFECTS.getBooleanValue())
        {
            this.hoveredStatusEffect = null;
            ci.cancel();
        }
    }
}
