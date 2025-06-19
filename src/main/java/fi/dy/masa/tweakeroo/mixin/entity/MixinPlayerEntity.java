package fi.dy.masa.tweakeroo.mixin.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.DataManager;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity
{
    @Shadow protected abstract boolean clipAtLedge();

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType_1, World world_1)
    {
        super(entityType_1, world_1);
    }

    @Inject(method = "method_30263", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_restore_1_15_2_sneaking(CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_SNEAK_1_15_2.getBooleanValue())
        {
            cir.setReturnValue(this.isOnGround());
        }
    }

    @Redirect(method = "adjustMovementForSneaking", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/entity/player/PlayerEntity;clipAtLedge()Z", ordinal = 0))
    private boolean tweakeroo_fakeSneaking(PlayerEntity entity)
    {
        if (FeatureToggle.TWEAK_FAKE_SNEAKING.getBooleanValue() && ((Object) this) instanceof ClientPlayerEntity)
        {
            return true;
        }

        return this.clipAtLedge();
    }

    @Inject(method = "getBlockInteractionRange", at = @At("RETURN"), cancellable = true)
    private void tweakeroo_overrideBlockReachDistance(CallbackInfoReturnable<Double> cir)
    {
        if (FeatureToggle.TWEAK_BLOCK_REACH_OVERRIDE.getBooleanValue())
        {
            if (MinecraftClient.getInstance().isIntegratedServerRunning() || Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue() < cir.getReturnValue())
            {
                cir.setReturnValue(Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue());
            }
            else
            {
                if (DataManager.getInstance().hasCarpetServer())
                {
                    // When using Carpet server, the server-side reach check might be disabled.
                    cir.setReturnValue(Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue());
                }
                else
                {
                    // Calculate a "safe" range for servers
                    double rangeRealMax = cir.getReturnValue() + 1.0;
                    cir.setReturnValue(Math.min(Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue(), rangeRealMax));
                }
            }
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void tweakeroo_overrideEntityReachDistance(CallbackInfoReturnable<Double> cir)
    {
        if (FeatureToggle.TWEAK_ENTITY_REACH_OVERRIDE.getBooleanValue())
        {
            if (MinecraftClient.getInstance().isIntegratedServerRunning())
            {
                cir.setReturnValue(Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue());
            }
            else
            {
                // Calculate a "safe" range for servers
                double rangeRealMax = cir.getReturnValue() + 1.0;
                cir.setReturnValue(Math.min(Configs.Generic.ENTITY_REACH_DISTANCE.getDoubleValue(), rangeRealMax));
            }
        }
    }
}
