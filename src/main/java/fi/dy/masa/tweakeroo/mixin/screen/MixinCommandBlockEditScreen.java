package fi.dy.masa.tweakeroo.mixin.screen;

import java.util.Collections;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.util.MiscUtils;

@Mixin(CommandBlockEditScreen.class)
public abstract class MixinCommandBlockEditScreen extends AbstractCommandBlockEditScreen
{
    @Shadow @Final private CommandBlockEntity autoCommandBlock;
    @Shadow private CycleButton<CommandBlockEntity.Mode> modeButton;
    @Shadow private CycleButton<Boolean> conditionalButton;
    @Shadow private CycleButton<Boolean> autoexecButton;

    @Unique private EditBox textFieldName;
    @Unique private CycleButton<Boolean> buttonUpdateExec;
    @Unique private boolean updateExecValue;
    @Unique private String lastName = "";

    @Inject(method = "init", at = @At("RETURN"))
    private void addExtraFields(CallbackInfo ci)
    {
        if (FeatureToggle.TWEAK_COMMAND_BLOCK_EXTRA_FIELDS.getBooleanValue())
        {
            int x1 = this.width / 2 - 152;
            int x2 = x1 + 204;
            int y = 158;
            int width = 200;

            if (this.minecraft == null || this.minecraft.player == null)
            {
                return;
            }

            // Move the vanilla buttons a little bit tighter, otherwise the large GUI scale is a mess
            this.modeButton.setY(y);
            this.conditionalButton.setY(y);
            this.autoexecButton.setY(y);

            y += 46;
            this.doneButton.setY(y);
            this.cancelButton.setY(y);

            Component str = Component.translatable("tweakeroo.gui.button.misc.command_block.set_name");
            int widthBtn = this.font.width(str) + 10;

            y = 181;
            this.textFieldName = new EditBox(this.font, x1, y, width, 20, Component.nullToEmpty(""));
            this.textFieldName.setValue(this.autoCommandBlock.getCommandBlock().getName().getString());
            this.addWidget(this.textFieldName);
            final EditBox tf = this.textFieldName;
            final BlockPos pos = this.autoCommandBlock.getBlockPos();

            Button.Builder builder = Button.builder(str, (button) ->
            {
                String name = tf.getValue();
                name = String.format("{\"CustomName\":\"{\\\"text\\\":\\\"%s\\\"}\"}", name);
                this.minecraft.player.connection.sendCommand(String.format("data merge block %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), name));
            });

            builder.pos(x2, y).size(widthBtn, 20);
            this.addRenderableWidget(builder.build());

            this.updateExecValue = MiscUtils.getUpdateExec(this.autoCommandBlock);

            Component strOn = Component.translatable("tweakeroo.gui.button.misc.command_block.update_execution.on");
            Component strOff = Component.translatable("tweakeroo.gui.button.misc.command_block.update_execution.off");
            Component strLooping = Component.translatable("tweakeroo.gui.button.misc.command_block.update_execution.looping");
            width = this.font.width(strOff) + 10;

            this.buttonUpdateExec = CycleButton.booleanBuilder(strOn, strOff, this.updateExecValue)
                                    .displayOnlyValue()
                                    .create(x2 + widthBtn + 4, y, width, 20, strLooping, (button, val) ->
            {
                this.updateExecValue = val;
                MiscUtils.setUpdateExec(this.autoCommandBlock, this.updateExecValue);

                String cmd = String.format("data merge block %d %d %d {\"UpdateLastExecution\":%s}",
                        pos.getX(), pos.getY(), pos.getZ(), this.updateExecValue ? "1b" : "0b");
                this.minecraft.player.connection.sendCommand(cmd);
            });

            this.addRenderableWidget(this.buttonUpdateExec);
        }
    }

    // This is needed because otherwise the name updating is delayed by "one GUI opening" >_>
    @Override
    public void tick()
    {
        super.tick();

        if (this.textFieldName != null)
        {
            String currentName = this.autoCommandBlock.getCommandBlock().getName().getString();

            if (currentName.equals(this.lastName) == false)
            {
                this.textFieldName.setValue(currentName);
                this.lastName = currentName;
            }
        }

        if (this.buttonUpdateExec != null)
        {
            boolean updateExec = MiscUtils.getUpdateExec(this.autoCommandBlock);

            if (this.updateExecValue != updateExec)
            {
                this.updateExecValue = updateExec;
                Component str = getDisplayStringForCurrentStatus(this.updateExecValue);
                this.buttonUpdateExec.setMessage(str);
                this.buttonUpdateExec.setWidth(this.font.width(str) + 10);
            }
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics drawContext, int mouseX, int mouseY, float partialTicks)
    {
        super.render(drawContext, mouseX, mouseY, partialTicks);

        if (this.textFieldName != null)
        {
            this.textFieldName.render(drawContext, mouseX, mouseY, partialTicks);
        }

        if (this.buttonUpdateExec != null && this.buttonUpdateExec.isHovered())
        {
            String hover = "tweakeroo.gui.button.misc.command_block.hover.update_execution";
            RenderUtils.drawHoverText(GuiContext.fromGuiGraphics(drawContext), mouseX, mouseY, Collections.singletonList(StringUtils.translate(hover)));
        }
    }

    @Unique
    private static Component getDisplayStringForCurrentStatus(boolean updateExecValue)
    {
        String translationKey = "tweakeroo.gui.button.misc.command_block.update_execution";
        boolean isCurrentlyOn = ! updateExecValue;
        String strStatus = "malilib.gui.label_colored." + (isCurrentlyOn ? "on" : "off");
        return Component.translatable(translationKey, StringUtils.translate(strStatus));
    }
}
