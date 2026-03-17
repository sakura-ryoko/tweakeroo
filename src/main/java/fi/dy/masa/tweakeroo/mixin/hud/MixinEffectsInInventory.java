package fi.dy.masa.tweakeroo.mixin.hud;

import java.util.Collection;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = EffectsInInventory.class, priority = 1001)
public abstract class MixinEffectsInInventory
{
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_disableStatusEffectRendering1(GuiGraphicsExtractor graphics, Collection<MobEffectInstance> activeEffects, int x0, int yStep, int mouseX, int mouseY, int maxWidth, CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_INVENTORY_EFFECTS.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
