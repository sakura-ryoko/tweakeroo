package fi.dy.masa.tweakeroo.mixin.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer
{
//    @Shadow @Final private MinecraftClient client;
//    @Unique float tickProg = 0f;

    @Redirect(method = "updateHeldItems()V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    public float tweakeroo_redirectedGetCooledAttackStrength(ClientPlayerEntity player, float adjustTicks)
    {
        return Configs.Disable.DISABLE_ITEM_SWITCH_COOLDOWN.getBooleanValue() ? 1.0F : player.getAttackCooldownProgress(adjustTicks);
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_preventOffhandRendering(AbstractClientPlayerEntity player, float tickDelta,
                                         float pitch, Hand hand, float swingProgress, ItemStack item,
                                         float equipProgress, MatrixStack matrices,
                                         VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
    {
        if (hand == Hand.OFF_HAND && Configs.Disable.DISABLE_OFFHAND_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }

    // fixme, doesn't work
//    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
//                at = @At("HEAD"))
//    private void tweakeroo_onRenderHandItem1(float tickProgress, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci)
//    {
//        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
//            Configs.Generic.FREE_CAMERA_PLAYER_RENDER_HANDS.getBooleanValue())
//        {
//            this.tickProg = tickProgress;
//        }
//    }
//
//    @ModifyVariable(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"), argsOnly = true)
//    private ClientPlayerEntity tweakeroo_modifyPlayerForHandRendering(ClientPlayerEntity player)
//    {
//        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
//            Configs.Generic.FREE_CAMERA_PLAYER_RENDER_HANDS.getBooleanValue())
//        {
//            return CameraEntity.getCamera();
//        }
//
//        return player;
//    }
//
//    @ModifyVariable(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"), argsOnly = true)
//    private int tweakeroo_modifyLightForHandRendering(int light)
//    {
//        if (FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue() &&
//            Configs.Generic.FREE_CAMERA_PLAYER_RENDER_HANDS.getBooleanValue())
//        {
//            return this.client.getEntityRenderDispatcher().getLight(CameraEntity.getCamera(), this.tickProg);
//        }
//
//        return light;
//    }
}
