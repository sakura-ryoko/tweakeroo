package fi.dy.masa.tweakeroo.mixin.screen;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.platform.InputConstants;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.IGuiEditSign;
import fi.dy.masa.tweakeroo.util.MiscUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

@Mixin(AbstractSignEditScreen.class)
public abstract class MixinAbstractSignEditScreen extends Screen implements IGuiEditSign
{
    protected MixinAbstractSignEditScreen(Component textComponent)
    {
        super(textComponent);
    }

    @Shadow @Final protected SignBlockEntity sign;
    @Shadow private SignText text;
    @Shadow @Final private boolean isFrontText;
    @Shadow @Final private String[] messages;

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
            MiscUtils.copyTextFromSign(this.sign, this.isFrontText);
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void preventGuiOpen(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_SIGN_COPY.getBooleanValue())
        {
            MiscUtils.applyPreviousTextToSign(this.sign, ((AbstractSignEditScreen) (Object) this), this.isFrontText);
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
        this.text = text;

        for (int i = 0; i < this.messages.length; i++)
        {
            this.messages[i] = text.getMessage(i, false).getString();
        }
    }
}
