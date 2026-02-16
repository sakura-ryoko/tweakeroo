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

import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;

@Mixin(value = LocalPlayer.class)
public abstract class MixinLocalPlayer_common extends AbstractClientPlayer
{
    @Shadow public ClientInput input;
    @Shadow protected int sprintTriggerTime;
    @Shadow public float oPortalEffectIntensity;
    @Shadow public float portalEffectIntensity;

    @Unique private float realNauseaIntensity;

    private MixinLocalPlayer_common(ClientLevel world, GameProfile profile)
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
