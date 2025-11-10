package fi.dy.masa.tweakeroo.mixin.freecam;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(value = Player.class, priority = 1005)
public abstract class MixinPlayerEntity_freeCam extends LivingEntity
{
    protected MixinPlayerEntity_freeCam(EntityType<? extends LivingEntity> entityType_1, Level world_1)
    {
        super(entityType_1, world_1);
    }

    @Inject(method = "isSpectator", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_overrideIsSpectator(CallbackInfoReturnable<Boolean> cir)
    {
        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
            CameraUtils.getFreeCameraSpectator() &&
            (Player) (Object) this instanceof LocalPlayer)
        {
            cir.setReturnValue(true);
        }
    }
}
