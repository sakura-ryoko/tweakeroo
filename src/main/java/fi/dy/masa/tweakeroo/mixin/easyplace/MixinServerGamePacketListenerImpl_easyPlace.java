package fi.dy.masa.tweakeroo.mixin.easyplace;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 990)
//@Restriction(conflict = @Condition(value = "litematica"))
public class MixinServerGamePacketListenerImpl_easyPlace
{
    // WrapOperation is safer than Redirect here
    @WrapOperation(method = "handleUseItemOn",
                   at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/phys/Vec3;subtract(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 tweakeroo_removeHitPosCheck(Vec3 instance, Vec3 vec3, Operation<Vec3> original)
    {
        if (Configs.Generic.ITEM_USE_PACKET_CHECK_BYPASS.getBooleanValue())
        {
            return Vec3.ZERO;
        }

        return instance.subtract(vec3);
    }
}
