package fi.dy.masa.tweakeroo.mixin.hud;

import java.util.Collection;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = StatusEffectsDisplay.class, priority = 1001)
public abstract class MixinStatusEffectsDisplay
{
    @Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableStatusEffectRendering1(DrawContext context, Collection<StatusEffectInstance> effects, int x, int height, int mouseX, int mouseY, int width, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_INVENTORY_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
