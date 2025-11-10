package fi.dy.masa.tweakeroo.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.tweaks.PlacementTweaks;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{
    @Shadow public abstract InteractionHand getUsedItemHand();

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
    private void removeOwnPotionEffects(CallbackInfo ci)
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
    private void onItemConsumed(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_HAND_RESTOCK.getBooleanValue())
        {
            if ((Object) this instanceof Player player)
            {
                PlacementTweaks.onProcessRightClickPost(player, this.getUsedItemHand());
            }
        }
    }
}
