package fi.dy.masa.tweakeroo.mixin.entity;

import org.objectweb.asm.Opcodes;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.InventoryUtils;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(value = LocalPlayer.class, priority = 1001)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayer
{
    @Shadow public ClientInput input;
    @Shadow protected int sprintTriggerTime;
    @Shadow public float oPortalEffectIntensity;
    @Shadow public float portalEffectIntensity;
    @Shadow private boolean wasFallFlying;

    @Unique private float realNauseaIntensity;
    @Unique private ItemStack autoSwitchElytraChestplate = ItemStack.EMPTY;

    private MixinClientPlayerEntity(ClientLevel world, GameProfile profile)
    {
        super(world, profile);
    }

    @Inject(method = "handlePortalTransitionEffect", at = @At("HEAD"))
    private void tweakeroo_disableNauseaEffectPre(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_NAUSEA_EFFECT.getBooleanValue())
        {
            this.portalEffectIntensity = this.realNauseaIntensity;
        }
    }

    @Inject(method = "handlePortalTransitionEffect", at = @At(value = "TAIL"))
    private void tweakeroo_disableNauseaEffectPost(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_NAUSEA_EFFECT.getBooleanValue())
        {
            // This is used to set the value to the correct value for the duration of the
            // updateNausea() method, so that the portal sound plays correctly only once.
            this.realNauseaIntensity = this.portalEffectIntensity;
            this.oPortalEffectIntensity = 0.0f;
            this.portalEffectIntensity = 0.0f;
        }
    }

    @Inject(method = "aiStep",
            at = @At(value = "FIELD",
					 target = "Lnet/minecraft/client/player/LocalPlayer;wasFallFlying:Z",
					 opcode = Opcodes.PUTFIELD))
    private void tweakeroo_overrideSprint(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_PERMANENT_SPRINT.getBooleanValue() &&
            ! this.isSprinting() && ! this.isUsingItem() && this.input.hasForwardImpulse() &&
            (this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly) &&
            ! this.hasEffect(MobEffects.BLINDNESS) && ! this.isInWater())
        {
            this.setSprinting(true);
        }
    }

    @Redirect(method = "shouldStopRunSprinting", at = @At(value = "FIELD",
													   target = "Lnet/minecraft/client/player/LocalPlayer;horizontalCollision:Z",
													   opcode = Opcodes.GETFIELD))
    private boolean tweakeroo_overrideCollidedHorizontally(LocalPlayer player)
    {
        if (Configs.Disable.DISABLE_WALL_UNSPRINT.getBooleanValue())
        {
            return false;
        }

        return player.horizontalCollision;
    }

    @Inject(method = "aiStep",
            /*
            slice = @Slice(from = @At(value = "FIELD",
                                      target = "Lnet/minecraft/client/option/GameOptions;sprintKey:Lnet/minecraft/client/option/KeyBinding;")),
             */
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 2, shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/player/LocalPlayer;sprintTriggerTime:I"))
    private void tweakeroo_disableDoubleTapSprint(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_DOUBLE_TAP_SPRINT.getBooleanValue())
        {
            this.sprintTriggerTime = 0;
        }
    }

    @Inject(method = "aiStep",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z"))
    private void tweakeroo_onFallFlyingCheckChestSlot(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_AUTO_SWITCH_ELYTRA.getBooleanValue())
        {
            // this.checkGliding()
            if (!this.onGround() && !this.isPassenger() && this.fallFlyTicks == 0 && !this.isInLiquid() && !this.onClimbable() && !this.hasEffect(MobEffects.LEVITATION))
            {
                if (!this.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) ||
                    this.getItemBySlot(EquipmentSlot.CHEST).getDamageValue() > this.getItemBySlot(EquipmentSlot.CHEST).getMaxDamage() - 10)
                {
                    this.autoSwitchElytraChestplate = this.getItemBySlot(EquipmentSlot.CHEST).copy();
                    InventoryUtils.equipBestElytra(this);
                }
            }
        }
        else
        {
            // reset auto switch item if the feature is disabled.
            this.autoSwitchElytraChestplate = ItemStack.EMPTY;
        }
    }

    @Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("RETURN"))
    private void tweakeroo_onStopFlying(EntityDataAccessor<?> data, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_AUTO_SWITCH_ELYTRA.getBooleanValue())
        {
            if (DATA_SHARED_FLAGS_ID.equals(data) && this.wasFallFlying)
            {
                if (!this.isFallFlying() && this.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA))
                {
                    if (!this.autoSwitchElytraChestplate.isEmpty() && !this.autoSwitchElytraChestplate.is(Items.ELYTRA))
                    {
                        if (this.inventoryMenu.getCarried().isEmpty())
                        {
                            int targetSlot = InventoryUtils.findSlotWithItem(this.inventoryMenu, this.autoSwitchElytraChestplate, true, false);

                            if (targetSlot >= 0)
                            {
                                InventoryUtils.swapItemToEquipmentSlot(this, EquipmentSlot.CHEST, targetSlot);
                            }
                            else
                            {
                                // cached item not found, try to swap to the default chest plate.
                                InventoryUtils.swapElytraAndChestPlate(this);
                            }

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
        }
    }

	@ModifyArg(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
			   at = @At(value = "INVOKE",
						target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"))
	private static Predicate<Entity> tweakeroo_overrideTargetedEntityCheck(Predicate<Entity> predicate)
	{
		if (Configs.Disable.DISABLE_DEAD_MOB_TARGETING.getBooleanValue())
		{
			predicate = predicate.and((entityIn) -> (entityIn instanceof LivingEntity) == false || ((LivingEntity) entityIn).getHealth() > 0f);
		}

		Minecraft mc = Minecraft.getInstance();

		if ((FeatureToggle.TWEAK_HANGABLE_ENTITY_BYPASS.getBooleanValue() && mc.player != null
			 && mc.player.isShiftKeyDown() == Configs.Generic.HANGABLE_ENTITY_BYPASS_INVERSE.getBooleanValue()))
		{
			predicate = predicate.and((entityIn) -> (entityIn instanceof HangingEntity) == false);
		}

		return predicate;
	}
}
