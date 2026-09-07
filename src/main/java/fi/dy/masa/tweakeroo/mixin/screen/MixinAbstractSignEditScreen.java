package fi.dy.masa.tweakeroo.mixin.screen;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.IGuiEditSign;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(AbstractSignEditScreen.class)
public abstract class MixinAbstractSignEditScreen extends Screen implements IGuiEditSign
{
    protected MixinAbstractSignEditScreen(Component textComponent)
    {
        super(textComponent);
    }

    @Shadow @Final protected SignBlockEntity sign;
    @Mutable @Final @Shadow private SignText.Mutable text;
    @Mutable @Shadow @Final private String[] messages;
    @Shadow @Final private SignTextSlot slot;

    @Override
    public SignBlockEntity tweakeroo$getTile()
    {
        return this.sign;
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void storeText(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_SIGN_COPY.getBooleanValue())
        {
            MiscUtils.copyTextFromSign(this.sign, this.slot);
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void preventGuiOpen(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_SIGN_COPY.getBooleanValue())
        {
            MiscUtils.applyPreviousTextToSign(this.sign, ((AbstractSignEditScreen) (Object) this), this.slot);
        }

        if (Configs.Disable.DISABLE_SIGN_GUI.getBooleanValue())
        {
            // Update the keybind state, because opening a GUI resets them all.
            // Also, KeyBinding.updateKeyBindState() only works for keyboard keys
            KeyMapping keybind = Minecraft.getInstance().options.keyUse;
            InputConstants.Key input = InputConstants.getKey(keybind.saveString());

            if (input != null)
            {
                KeyMapping.set(input, KeybindMulti.isKeyDown(KeybindMulti.getKeyCode(keybind)));
            }

            GuiBase.openGui(null);
        }
    }

    @Override
    public void tweakeroo$applyText(SignText text)
    {
        List<Component> list = text.getMessages(false);
        SignText.Mutable mutable = text.asMutable();

        for (int i = 0; i < 4; i++)
        {
            String entry = i < list.size() ? list.get(i).getString() : "";
            this.messages[i] = list.get(i).getString();
            mutable.setLine(i, Component.literal(entry));
        }

        this.text = mutable;
        this.sign.setText(this.text.asImmutable(), this.slot);
    }
}
