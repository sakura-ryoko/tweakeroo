package fi.dy.masa.tweakeroo.mixin.entity;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{
    @Shadow public abstract Hand getActiveHand();
	@Unique private boolean wasGammaOverriden = false;

    private MixinLivingEntity(EntityType<?> type, World worldIn)
    {
        super(type, worldIn);
    }

    // TODO 1.21.2+ - it seems that Mojang fixed this.
    /*
    @Redirect(method = "method_61417", at = @At(value = "FIELD", ordinal = 1,
            target = "Lnet/minecraft/world/World;isClient:Z"))
    private boolean fixElytraLanding()
    {
        return this.getWorld().isClient && (Configs.Fixes.ELYTRA_FIX.getBooleanValue() == false || ((Object) this instanceof ClientPlayerEntity) == false);
    }
     */

    @Inject(method = "tickStatusEffects", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/entity/data/DataTracker;get(Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;"),
            cancellable = true)
    private void tweakeroo_removeOwnPotionEffects(CallbackInfo ci)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (Configs.Disable.DISABLE_FP_EFFECT_PARTICLES.getBooleanValue() &&
            ((Object) this) == mc.player &&
            mc.options.getPerspective() == Perspective.FIRST_PERSON)
        {
            ci.cancel();
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;isGliding()Z"))
    private void tweakeroo_applyCustomDeceleration(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_CUSTOM_FLY_DECELERATION.getBooleanValue() &&
            ((Entity) this) == MinecraftClient.getInstance().player)
        {
            MiscUtils.handlePlayerDeceleration();
        }
    }

    @Inject(method = "consumeItem", at = @At("RETURN"))
    private void tweakeroo_onItemConsumed(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue())
        {
            if ((Object) this instanceof PlayerEntity player)
            {
                PlacementTweaks.onProcessRightClickPost(player, this.getActiveHand());
            }
        }
    }

	// Save and restore the Gamma Override while Night Vision is activated.
	@Inject(method = "onStatusEffectApplied", at = @At("HEAD"))
	private void tweakeroo$onStatusEffectApplied(StatusEffectInstance effect, Entity source, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue() &&
			effect.getEffectType() == StatusEffects.NIGHT_VISION)
		{
			MiscUtils.toggleGammaOverrideWithMessage();
			this.wasGammaOverriden = true;
		}
	}

	@Inject(method = "onStatusEffectUpgraded", at = @At("HEAD"))
	private void tweakeroo$onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, Entity source, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue() &&
			effect.getEffectType() == StatusEffects.NIGHT_VISION)
		{
			MiscUtils.toggleGammaOverrideWithMessage();
			this.wasGammaOverriden = true;
		}
	}

	@Inject(method = "onStatusEffectsRemoved", at = @At("HEAD"))
	private void tweakeroo$onStatusEffectRemoved(Collection<StatusEffectInstance> effects, CallbackInfo ci)
	{
		if (this.wasGammaOverriden)
		{
			for (StatusEffectInstance entry : effects)
			{
				if (entry.getEffectType() == StatusEffects.NIGHT_VISION)
				{
					if (!FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue())
					{
						MiscUtils.toggleGammaOverrideWithMessage();
					}

					this.wasGammaOverriden = false;
					break;
				}
			}
		}
	}
}
