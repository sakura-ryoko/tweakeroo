package fi.dy.masa.tweakeroo.mixin.hud;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(value = ChatComponent.class, priority = 1100)
public abstract class MixinChatHud
{
    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
                    at = @At("HEAD"), argsOnly = true)
    private Component tweakeroo_addMessageTimestamp(Component componentIn, Component parameterMessage, MessageSignature data, GuiMessageTag indicator)
    {
        if (FeatureToggle.TWEAK_CHAT_TIMESTAMP.getBooleanValue())
        {
            MutableComponent newComponent = Component.literal(MiscUtils.getChatTimestamp() + " ");
            newComponent.append(componentIn);
            return newComponent;
        }

        return componentIn;
    }

	// 1.21.10:
	// method_71992(Lnet/minecraft/client/gui/GuiGraphics;IFFIIIIILnet/minecraft/client/GuiMessage$Line;IF)V // ARGB;color
	// 25w46a:
	// method_75802(IILnet/minecraft/client/gui/hud/ChatHud$Backend;IFLnet/minecraft/client/gui/hud/ChatHudLine$Visible;IF)V
	//
    // INVOKEVIRTUAL Bytecode Mixin
    @WrapOperation(method = "method_75802(IILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IFLnet/minecraft/client/GuiMessage$Line;IF)V",
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
