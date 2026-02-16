package fi.dy.masa.tweakeroo.mixin.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.util.HandSlot;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.InventoryUtils;

@Mixin(value = LocalPlayer.class)
public abstract class MixinLocalPlayer_elytraSwap extends AbstractClientPlayer
{
	@Shadow private boolean wasFallFlying;

	@Unique private ItemStack autoSwitchElytraChestplate = ItemStack.EMPTY;
	@Unique private ItemStack autoSwitchFireworkHand = ItemStack.EMPTY;

	public MixinLocalPlayer_elytraSwap(ClientLevel clientLevel, GameProfile gameProfile)
	{
		super(clientLevel, gameProfile);
	}

	@Inject(method = "aiStep",
	        at = @At(value = "INVOKE",
	                 shift = At.Shift.BEFORE,
	                 target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z"
	        )
	)
	private void tweakeroo_onFallFlyingCheckChestSlot(CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_AUTO_SWITCH_ELYTRA.getBooleanValue() || FeatureToggle.TWEAK_AUTO_SWITCH_ROCKETS.getBooleanValue())
		{
			// this.checkGliding()
			if (!this.onGround() && !this.isPassenger() && this.fallFlyTicks == 0 && !this.isInLiquid() && !this.onClimbable() && !this.hasEffect(MobEffects.LEVITATION))
			{
				if (FeatureToggle.TWEAK_AUTO_SWITCH_ELYTRA.getBooleanValue())
				{
					if (!this.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) ||
							this.getItemBySlot(EquipmentSlot.CHEST).getDamageValue() > this.getItemBySlot(EquipmentSlot.CHEST).getMaxDamage() - 10)
					{
						this.autoSwitchElytraChestplate = this.getItemBySlot(EquipmentSlot.CHEST).copy();
						InventoryUtils.equipBestElytra(this);
					}
					else
					{
						// reset auto switch item if the feature is disabled.
						this.autoSwitchElytraChestplate = ItemStack.EMPTY;
					}
				}
				if (FeatureToggle.TWEAK_AUTO_SWITCH_ROCKETS.getBooleanValue())
				{
					EquipmentSlot handSlot = fi.dy.masa.malilib.util.InventoryUtils.getHandSlot((HandSlot) Configs.Generic.UTILITY_HAND_SLOT.getOptionListValue()).asEquipmentSlot();

					if (!this.getItemBySlot(handSlot).is(Items.FIREWORK_ROCKET))
					{
						this.autoSwitchFireworkHand = this.getItemBySlot(handSlot).copy();
						InventoryUtils.equipBestFlightRockets(this);
					}
					else
					{
						// reset auto switch item if the feature is disabled.
						this.autoSwitchFireworkHand = ItemStack.EMPTY;
					}
				}
			}
		}
		else
		{
			// reset auto switch item if the feature is disabled.
			this.autoSwitchElytraChestplate = ItemStack.EMPTY;
			this.autoSwitchFireworkHand = ItemStack.EMPTY;
		}
	}

	@Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("RETURN"))
	private void tweakeroo_onStopFlying(EntityDataAccessor<?> data, CallbackInfo ci)
	{
		if (FeatureToggle.TWEAK_AUTO_SWITCH_ELYTRA.getBooleanValue() || FeatureToggle.TWEAK_AUTO_SWITCH_ROCKETS.getBooleanValue())
		{
			if (DATA_SHARED_FLAGS_ID.equals(data) && this.wasFallFlying && !this.isFallFlying())
			{
				if (FeatureToggle.TWEAK_AUTO_SWITCH_ELYTRA.getBooleanValue())
				{
					if (this.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA))
					{
						if (!this.autoSwitchElytraChestplate.isEmpty() && !this.autoSwitchElytraChestplate.is(Items.ELYTRA))
						{
							if (this.inventoryMenu.getCarried().isEmpty())
							{
								InventoryUtils.swapElytraFromChest(this, this.autoSwitchElytraChestplate);
								this.autoSwitchElytraChestplate = ItemStack.EMPTY;
							}
						}
						else
						{
							// if cached previous item is empty, try to swap back to the default chest plate.
							InventoryUtils.swapElytraAndChestPlate(this);
						}
					}
				}
				if (FeatureToggle.TWEAK_AUTO_SWITCH_ROCKETS.getBooleanValue())
				{
					InteractionHand hand = fi.dy.masa.malilib.util.InventoryUtils.getHandSlot((HandSlot) Configs.Generic.UTILITY_HAND_SLOT.getOptionListValue());

					if (this.getItemBySlot(hand.asEquipmentSlot()).is(Items.FIREWORK_ROCKET))
					{
						if (!this.autoSwitchFireworkHand.isEmpty() && !this.autoSwitchFireworkHand.is(Items.FIREWORK_ROCKET))
						{
							if (this.inventoryMenu.getCarried().isEmpty())
							{
								InventoryUtils.swapFlightRocketsFromHand(this, hand, this.autoSwitchFireworkHand);
								this.autoSwitchFireworkHand = ItemStack.EMPTY;
							}
						}
					}
				}
			}
		}
	}
}
