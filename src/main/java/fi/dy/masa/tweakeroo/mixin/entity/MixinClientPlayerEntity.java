package fi.dy.masa.tweakeroo.mixin.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
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
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.function.Predicate;

@Mixin(value = ClientPlayerEntity.class, priority = 1001)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity
{
    @Shadow public Input input;
    @Shadow protected int ticksLeftToDoubleTapSprint;
    @Shadow public float lastNauseaIntensity;
    @Shadow public float nauseaIntensity;
    @Shadow private boolean falling;

    @Unique private float realNauseaIntensity;
    @Unique private ItemStack autoSwitchElytraChestplate = ItemStack.EMPTY;

    private MixinClientPlayerEntity(ClientWorld world, GameProfile profile)
    {
        super(world, profile);
    }

    @Inject(method = "tickNausea", at = @At("HEAD"))
    private void tweakeroo_disableNauseaEffectPre(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_NAUSEA_EFFECT.getBooleanValue())
        {
            this.nauseaIntensity = this.realNauseaIntensity;
        }
    }

    @Inject(method = "tickNausea", at = @At(value = "TAIL"))
    private void tweakeroo_disableNauseaEffectPost(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_NAUSEA_EFFECT.getBooleanValue())
        {
            // This is used to set the value to the correct value for the duration of the
            // updateNausea() method, so that the portal sound plays correctly only once.
            this.realNauseaIntensity = this.nauseaIntensity;
            this.lastNauseaIntensity = 0.0f;
            this.nauseaIntensity = 0.0f;
        }
    }

    @Inject(method = "tickMovement",
            at = @At(value = "FIELD",
					 target = "Lnet/minecraft/client/network/ClientPlayerEntity;falling:Z",
					 opcode = Opcodes.PUTFIELD))
    private void tweakeroo_overrideSprint(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_PERMANENT_SPRINT.getBooleanValue() &&
            ! this.isSprinting() && ! this.isUsingItem() && this.input.hasForwardMovement() &&
            (this.getHungerManager().getFoodLevel() > 6.0F || this.getAbilities().allowFlying) &&
            ! this.hasStatusEffect(StatusEffects.BLINDNESS) && ! this.isTouchingWater())
        {
            this.setSprinting(true);
        }
    }

    @Redirect(method = "shouldStopSprinting", at = @At(value = "FIELD",
													   target = "Lnet/minecraft/client/network/ClientPlayerEntity;horizontalCollision:Z",
													   opcode = Opcodes.GETFIELD))
    private boolean tweakeroo_overrideCollidedHorizontally(ClientPlayerEntity player)
    {
        if (Configs.Disable.DISABLE_WALL_UNSPRINT.getBooleanValue())
        {
            return false;
        }

        return player.horizontalCollision;
    }

    @Inject(method = "tickMovement",
            /*
            slice = @Slice(from = @At(value = "FIELD",
                                      target = "Lnet/minecraft/client/option/GameOptions;sprintKey:Lnet/minecraft/client/option/KeyBinding;")),
             */
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, ordinal = 2, shift = At.Shift.AFTER,
                     target = "Lnet/minecraft/client/network/ClientPlayerEntity;ticksLeftToDoubleTapSprint:I"))
    private void tweakeroo_disableDoubleTapSprint(CallbackInfo ci)
    {
        if (Configs.Disable.DISABLE_DOUBLE_TAP_SPRINT.getBooleanValue())
        {
            this.ticksLeftToDoubleTapSprint = 0;
        }
    }

    @Inject(method = "tickMovement",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;checkGliding()Z"))
    private void tweakeroo_onFallFlyingCheckChestSlot(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_AUTO_SWITCH_ELYTRA.getBooleanValue())
        {
            // this.checkGliding()
            if (!this.isOnGround() && !this.hasVehicle() && this.glidingTicks == 0 && !this.isInFluid() && !this.isClimbing() && !this.hasStatusEffect(StatusEffects.LEVITATION))
            {
                if (!this.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) ||
                    this.getEquippedStack(EquipmentSlot.CHEST).getDamage() > this.getEquippedStack(EquipmentSlot.CHEST).getMaxDamage() - 10)
                {
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

    @Inject(method = "onTrackedDataSet(Lnet/minecraft/entity/data/TrackedData;)V", at = @At("RETURN"))
    private void tweakeroo_onStopFlying(TrackedData<?> data, CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_AUTO_SWITCH_ELYTRA.getBooleanValue())
        {
            if (FLAGS.equals(data) && this.falling)
            {
                if (!this.isGliding() && this.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA))
                {
                    if (!this.autoSwitchElytraChestplate.isEmpty() && !this.autoSwitchElytraChestplate.isOf(Items.ELYTRA))
                    {
                        if (this.playerScreenHandler.getCursorStack().isEmpty())
                        {
                            int targetSlot = InventoryUtils.findSlotWithItem(this.playerScreenHandler, this.autoSwitchElytraChestplate, true, false);

                            if (targetSlot >= 0)
                            {
                                InventoryUtils.swapItemToEquipmentSlot(this, EquipmentSlot.CHEST, targetSlot);
                                this.autoSwitchElytraChestplate = ItemStack.EMPTY;
                            }
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

	@ModifyArg(method = "method_76763",
			   at = @At(value = "INVOKE",
						target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
	private static Predicate<Entity> tweakeroo_overrideTargetedEntityCheck(Predicate<Entity> predicate)
	{
		if (Configs.Disable.DISABLE_DEAD_MOB_TARGETING.getBooleanValue())
		{
			predicate = predicate.and((entityIn) -> (entityIn instanceof LivingEntity) == false || ((LivingEntity) entityIn).getHealth() > 0f);
		}

		MinecraftClient mc = MinecraftClient.getInstance();

		if ((FeatureToggle.TWEAK_HANGABLE_ENTITY_BYPASS.getBooleanValue() && mc.player != null
			 && mc.player.isSneaking() == Configs.Generic.HANGABLE_ENTITY_BYPASS_INVERSE.getBooleanValue()))
		{
			predicate = predicate.and((entityIn) -> (entityIn instanceof AbstractDecorationEntity) == false);
		}

		return predicate;
	}
}
