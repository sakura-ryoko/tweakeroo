package fi.dy.masa.tweakeroo.mixin.entity;

import org.objectweb.asm.Opcodes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{
    @Shadow public abstract Hand getActiveHand();
	@Unique private boolean wasGammaOverriden = false;

    private MixinLivingEntity(EntityType<?> type, World worldIn)
    {
        super(type, worldIn);
    }

    @Redirect(method = "travel", at = @At(value = "FIELD", ordinal = 1,
                                          target = "Lnet/minecraft/world/World;isClient:Z", opcode = Opcodes.GETFIELD))
    private boolean fixElytraLanding(World world)
    {
        return world.isClient && (Configs.Fixes.ELYTRA_FIX.getBooleanValue() == false || ((Object) this instanceof ClientPlayerEntity) == false);
    }

    @Inject(method = "tickStatusEffects", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/entity/data/DataTracker;get(Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;"),
            cancellable = true)
    private void tweakeroo_removeOwnPotionEffects(CallbackInfo ci)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (Configs.Disable.DISABLE_FP_EFFECT_PARTICLES.getBooleanValue() &&
            ((Object) this) == mc.player && mc.options.getPerspective() == Perspective.FIRST_PERSON)
        {
            ci.cancel();
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;tickFallFlying()V"))
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

	@Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
	private void tweakeroo$onStatusEffectRemoved(StatusEffectInstance effect, CallbackInfo ci)
	{
		if (this.wasGammaOverriden)
		{
			if (effect.getEffectType() == StatusEffects.NIGHT_VISION)
			{
				if (!FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue())
				{
					MiscUtils.toggleGammaOverrideWithMessage();
				}

				this.wasGammaOverriden = false;
			}
		}
	}
}
