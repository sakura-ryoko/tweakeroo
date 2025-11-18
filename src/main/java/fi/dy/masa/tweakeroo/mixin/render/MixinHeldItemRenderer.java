package fi.dy.masa.tweakeroo.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.tweakeroo.config.Configs;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer
{
    @Redirect(method = "updateHeldItems()V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getHandEquippingProgress(F)F"))
    public float tweakeroo_redirectedGetCooledAttackStrength(ClientPlayerEntity player, float adjustTicks)
    {
        return Configs.Disable.DISABLE_ITEM_SWITCH_COOLDOWN.getBooleanValue() ? 1.0F : player.getAttackCooldownProgress(adjustTicks);
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void tweakeroo_preventOffhandRendering(AbstractClientPlayerEntity player, float tickProgress, float pitch,
												   Hand hand, float swingProgress, ItemStack item,
												   float equipProgress, MatrixStack matrices,
												   OrderedRenderCommandQueue orderedRenderCommandQueue,
												   int light, CallbackInfo ci)
    {
        if (hand == Hand.OFF_HAND && Configs.Disable.DISABLE_OFFHAND_RENDERING.getBooleanValue())
        {
            ci.cancel();
        }
    }
}
