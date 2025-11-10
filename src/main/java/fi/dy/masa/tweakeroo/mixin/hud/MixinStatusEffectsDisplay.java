package fi.dy.masa.tweakeroo.mixin.hud;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;

@Mixin(value = EffectsInInventory.class, priority = 1001)
public abstract class MixinStatusEffectsDisplay
{
    @Shadow private @Nullable MobEffectInstance hoveredEffect;

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableStatusEffectRendering1(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_INVENTORY_EFFECTS.getBooleanValue())
        {
            this.hoveredEffect = null;
            ci.cancel();
        }
    }
}
