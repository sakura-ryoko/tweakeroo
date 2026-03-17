package fi.dy.masa.tweakeroo.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;

@Mixin(Player.class)
public abstract class MixinPlayer_fakeSneak extends LivingEntity
{
    @Shadow protected abstract boolean isStayingOnGroundSurface();
    @Shadow public abstract boolean isAlwaysTicking();

    protected MixinPlayer_fakeSneak(EntityType<? extends LivingEntity> entityType_1, Level world_1)
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

    @WrapOperation(method = "maybeBackOffFromEdge", at = @At(value = "INVOKE",
                                                             target = "Lnet/minecraft/world/entity/player/Player;isStayingOnGroundSurface()Z",
                                                             ordinal = 0))
    private boolean tweakeroo_fakeSneaking(Player instance, Operation<Boolean> original)
    {
        if (FeatureToggle.TWEAK_FAKE_SNEAKING.getBooleanValue() && ((Object) this) instanceof LocalPlayer)
        {
            return true;
        }

        return original.call(instance);
    }
}
