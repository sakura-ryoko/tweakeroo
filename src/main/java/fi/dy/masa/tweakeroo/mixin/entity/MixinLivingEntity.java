package fi.dy.masa.tweakeroo.mixin.entity;

import java.util.Collection;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{
    @Shadow public abstract InteractionHand getUsedItemHand();
	@Unique private boolean wasGammaOverriden = false;

    private MixinLivingEntity(EntityType<?> type, Level worldIn)
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

    @Inject(method = "tickEffects", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/network/syncher/SynchedEntityData;get(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;"),
            cancellable = true)
    private void tweakeroo_removeOwnPotionEffects(CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();

        if (Configs.Disable.DISABLE_FP_EFFECT_PARTICLES.getBooleanValue() &&
            ((Object) this) == mc.player &&
            mc.options.getCameraType() == CameraType.FIRST_PERSON)
        {
            ci.cancel();
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private void tweakeroo_applyCustomDeceleration(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_CUSTOM_FLY_DECELERATION.getBooleanValue() &&
            ((Entity) this) == Minecraft.getInstance().player)
        {
            MiscUtils.handlePlayerDeceleration();
        }
    }

    @Inject(method = "completeUsingItem", at = @At("RETURN"))
    private void tweakeroo_onItemConsumed(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue())
        {
            if ((Object) this instanceof Player player)
            {
                PlacementTweaks.onProcessRightClickPost(player, this.getUsedItemHand());
            }
        }
    }

	// Save and restore the Gamma Override while Night Vision is activated.
	@Inject(method = "onEffectAdded", at = @At("HEAD"))
	private void tweakeroo$onStatusEffectApplied(MobEffectInstance effect, Entity source, CallbackInfo ci)
	{
		if (!this.level().isClientSide()) return;

		if (FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue() &&
			effect.getEffect() == MobEffects.NIGHT_VISION)
		{
			// Only warn once.  People aren't children.
			if (!Configs.Internal.SILENCE_NIGHT_VISION_WARN.getBooleanValue())
			{
				InfoUtils.showInGameMessage(Message.MessageType.WARNING, "tweakeroo.message.gamma_override.night_vision_warn");
				Configs.Internal.SILENCE_NIGHT_VISION_WARN.setBooleanValue(true);
			}

//			MiscUtils.toggleGammaOverrideWithMessage();
			this.wasGammaOverriden = true;
		}
	}

//	@Inject(method = "onEffectUpdated", at = @At("HEAD"))
//	private void tweakeroo$onStatusEffectUpgraded(MobEffectInstance effect, boolean reapplyEffect, Entity source, CallbackInfo ci)
//	{
//		if (FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue() &&
//			effect.getEffect() == MobEffects.NIGHT_VISION)
//		{
//			InfoUtils.showInGameMessage(Message.MessageType.WARNING, "tweakeroo.message.gamma_override.night_vision_warn");
////			MiscUtils.toggleGammaOverrideWithMessage();
//			this.wasGammaOverriden = true;
//		}
//	}

	@Inject(method = "onEffectsRemoved", at = @At("TAIL"))
	private void tweakeroo$onStatusEffectRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci)
	{
		if (this.wasGammaOverriden || FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue())
		{
			for (MobEffectInstance entry : effects)
			{
				if (entry.getEffect() == MobEffects.NIGHT_VISION)
				{
					if (FeatureToggle.TWEAK_GAMMA_OVERRIDE.getBooleanValue())
					{
						FeatureToggle.TWEAK_GAMMA_OVERRIDE.setDisabledNoCallback();
					}

					MiscUtils.toggleGammaOverrideWithMessage(true);
					this.wasGammaOverriden = false;
					break;
				}
			}
		}
	}
}
