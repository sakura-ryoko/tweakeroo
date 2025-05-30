package fi.dy.masa.tweakeroo.mixin.fog;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.tweaks.FogTweaks;

@Mixin(StatusEffectFogModifier.class)
public abstract class MixinStatusEffectFogModifier
{
    @Shadow public abstract RegistryEntry<StatusEffect> getStatusEffect();

    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_shouldBlockStatusEffectFog(CameraSubmersionType submersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir)
    {
        if (this.getStatusEffect() == StatusEffects.BLINDNESS && FogTweaks.INSTANCE.shouldBlockBlindnessFog())
        {
            cir.setReturnValue(false);
        }
        else if (this.getStatusEffect() == StatusEffects.DARKNESS && FogTweaks.INSTANCE.shouldBlockDarknessFog())
        {
            cir.setReturnValue(false);
        }
    }
}
