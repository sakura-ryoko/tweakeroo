package fi.dy.masa.tweakeroo.mixin.hud;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(value = ChatComponent.class, priority = 1005)
public abstract class MixinChatComponent
{
    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
                    at = @At("HEAD"), argsOnly = true)
    private Component tweakeroo_addMessageTimestamp(Component value)
    {
        if (FeatureToggle.TWEAK_CHAT_TIMESTAMP.getBooleanValue())
        {
            MutableComponent newComponent = Component.literal(MiscUtils.getChatTimestamp() + " ");
            newComponent.append(value);
            return newComponent;
        }

        return value;
    }

    // INVOKESTATIC Bytecode Mixin (L2)
    @WrapOperation(method = "lambda$extractRenderState$1(IILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IFLnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V",
                   at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/util/ARGB;black(F)I",
                       ordinal = 0))
    private static int tweakeroo_overrideChatBackgroundColor(float alpha, Operation<Integer> original)
    {
        if (FeatureToggle.TWEAK_CHAT_BACKGROUND_COLOR.getBooleanValue())
        {
            return MiscUtils.getChatBackgroundColor(ARGB.black(alpha));
        }

	    return original.call(alpha);
    }
}
