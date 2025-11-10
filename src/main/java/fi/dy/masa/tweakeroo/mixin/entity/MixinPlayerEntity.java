package fi.dy.masa.tweakeroo.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.DataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends LivingEntity
{
    @Shadow protected abstract boolean isStayingOnGroundSurface();
    @Shadow public abstract boolean isAlwaysTicking();

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType_1, Level world_1)
    {
        super(entityType_1, world_1);
    }

    @Inject(method = "isAboveGround", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_restore_1_15_2_sneaking(CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_SNEAK_1_15_2.getBooleanValue())
        {
            cir.setReturnValue(this.onGround());
        }
    }

    @Redirect(method = "maybeBackOffFromEdge", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/world/entity/player/Player;isStayingOnGroundSurface()Z", ordinal = 0))
    private boolean tweakeroo_fakeSneaking(Player entity)
    {
        if (FeatureToggle.TWEAK_FAKE_SNEAKING.getBooleanValue() && ((Object) this) instanceof LocalPlayer)
        {
            return true;
        }

        return this.isStayingOnGroundSurface();
    }

    @Inject(method = "blockInteractionRange", at = @At("RETURN"), cancellable = true)
    private void tweakeroo_overrideBlockReachDistance(CallbackInfoReturnable<Double> cir)
    {
        if (FeatureToggle.TWEAK_BLOCK_REACH_OVERRIDE.getBooleanValue())
        {
            if (Minecraft.getInstance().hasSingleplayerServer() || Configs.Generic.BLOCK_REACH_DISTANCE.getDoubleValue() < cir.getReturnValue())
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

    @Inject(method = "entityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void tweakeroo_overrideEntityReachDistance(CallbackInfoReturnable<Double> cir)
    {
        if (FeatureToggle.TWEAK_ENTITY_REACH_OVERRIDE.getBooleanValue())
        {
            if (Minecraft.getInstance().hasSingleplayerServer())
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
