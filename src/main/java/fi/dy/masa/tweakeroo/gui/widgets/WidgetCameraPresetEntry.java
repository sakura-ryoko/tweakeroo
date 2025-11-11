package fi.dy.masa.tweakeroo.gui.widgets;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.entity.Entity;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiTextInputFeedback;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import fi.dy.masa.malilib.interfaces.IStringConsumerFeedback;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.CameraPresetManager;
import fi.dy.masa.tweakeroo.util.CameraPreset;
import fi.dy.masa.tweakeroo.util.CameraUtils;

public class WidgetCameraPresetEntry extends WidgetListEntryBase<CameraPreset>
{
	private final WidgetCameraPresetList parent;
	private final CameraPreset preset;
	private final List<String> hoverLines;
	private final boolean isOdd;
	private final int buttonsStartX;

	public WidgetCameraPresetEntry(int x, int y, int width, int height,
	                               boolean isOdd,
	                               CameraPreset preset, int listIndex,
	                               WidgetCameraPresetList parent)
	{
		super(x, y, width, height, preset, listIndex);
		this.isOdd = isOdd;
		this.preset = preset;
		this.hoverLines = preset.getWidgetHoverLines();
		this.parent = parent;

		y += 8;
		int posX = x + width - 2;
		posX -= this.addButton(posX, y, ButtonListener.Type.REMOVE);
		posX -= this.addButton(posX, y, ButtonListener.Type.SET_HERE);
		posX -= this.addButton(posX, y, ButtonListener.Type.RENAME);
		posX -= this.addButton(posX, y, ButtonListener.Type.RECALL);
		this.buttonsStartX = posX;
	}

	protected int addButton(int x, int y, ButtonListener.Type type)
	{
		ButtonGeneric button = new ButtonGeneric(x, y, -1, true, type.getDisplayName());
		this.addButton(button, new ButtonListener(type, this));

		if (type == ButtonListener.Type.RECALL)
		{
			button.setHoverStrings("tweakeroo.gui.button.hover.preset_entry.recall");
		}
//		else if (type == ButtonListener.Type.RENAME)
//		{
//			button.setHoverStrings("tweakeroo.gui.button.hover.preset_entry.rename");
//		}
//		else if (type == ButtonListener.Type.SET_HERE)
//		{
//			button.setHoverStrings("tweakeroo.gui.button.hover.preset_entry.set_here");
//		}
//		else if (type == ButtonListener.Type.REMOVE)
//		{
//			button.setHoverStrings("tweakeroo.gui.button.hover.preset_entry.remove");
//		}

		return button.getWidth() + 2;
	}

	@Override
	public boolean canSelectAt(Click click)
	{
		return super.canSelectAt(click) && click.x() < this.buttonsStartX;
	}

	@Override
	public boolean onKeyTyped(KeyInput input)
	{
		if (input.key() == KeyCodes.KEY_ESCAPE)
		{
			this.parent.setParent(null);
			GuiBase.openGui(null);
		}

		return super.onKeyTyped(input);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, boolean selected)
	{
		boolean presetSelected = CameraPresetManager.getInstance().getSelectedPreset() == this.entry;
		int y = this.y + 7;

		// Draw a lighter background for the hovered and the selected entry
		if (selected || presetSelected || this.isMouseOver(mouseX, mouseY))
		{
			RenderUtils.drawRect(context, this.x, y, this.width, this.height, 0x70FFFFFF);
		}
		else if (this.isOdd)
		{
			RenderUtils.drawRect(context, this.x, y, this.width, this.height, 0x20FFFFFF);
		}
		// Draw a slightly lighter background for even entries
		else
		{
			RenderUtils.drawRect(context, this.x, y, this.width, this.height, 0x50FFFFFF);
		}

		if (presetSelected)
		{
			RenderUtils.drawOutline(context, this.x, y, this.width, this.height, 0xFFE0E0E0);
		}

		String line = this.preset.toShortStringStyled();
		this.drawString(context, this.x + 4, y + 7, 0xFFFFFFFF, line);
		super.render(context, mouseX, mouseY, selected);
	}

	@Override
	public void postRenderHovered(DrawContext context, int mouseX, int mouseY, boolean selected)
	{
		super.postRenderHovered(context, mouseX, mouseY, selected);

		if (mouseX >= this.x && mouseX < this.buttonsStartX && mouseY >= this.y && mouseY <= this.y + this.height)
		{
			RenderUtils.drawHoverText(context, mouseX, mouseY, this.hoverLines);
		}
	}

	private static class ButtonListener implements IButtonActionListener
	{
		private final Type type;
		private final WidgetCameraPresetEntry widget;

		public ButtonListener(Type type, WidgetCameraPresetEntry widget)
		{
			this.type = type;
			this.widget = widget;
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton)
		{
			if (this.widget.preset == null) return;
			MinecraftClient mc = MinecraftClient.getInstance();

			if (this.type == Type.RECALL)
			{
				CameraPreset preset = this.widget.preset;

				if (mc.world != null && mc.world.getRegistryKey().getValue().equals(preset.getDim()))
				{
					if (CameraUtils.recallPreset(preset, mc))
					{
						InfoUtils.showGuiMessage(Message.MessageType.INFO, 2500, "tweakeroo.message.free_cam.preset_recalled",
						                         FeatureToggle.TWEAK_FREE_CAMERA.getPrettyName(),
						                         String.format("%02d", preset.getId()), preset.getName());
					}
					else
					{
						InfoUtils.showGuiMessage(Message.MessageType.WARNING, "tweakeroo.message.free_cam.preset_matches_camera",
						                         String.format("%02d", preset.getId()));
					}
				}
				else
				{
					InfoUtils.showGuiMessage(Message.MessageType.ERROR, "tweakeroo.message.free_cam.preset_wrong_dimension",
					                         String.format("%02d", preset.getId()), preset.getName());
				}
			}
			else if (this.type == Type.RENAME)
			{
				String title = "tweakeroo.gui.title.camera_preset_rename";
				String name = this.widget.preset.getName();
				PresetRenamer renamer = new PresetRenamer(this.widget.preset, this.widget);
				GuiBase.openGui(new GuiTextInputFeedback(60, title, name, this.widget.parent.getPresetEditorGui(), renamer));
			}
			else if (this.type == Type.SET_HERE)
			{
				if (mc.getCameraEntity() != null)
				{
					Entity camera = mc.getCameraEntity();
					this.widget.preset.setPos(camera.getEntityPos(), camera.getYaw(), camera.getPitch());
					CameraPresetManager.getInstance().update(this.widget.preset);
					this.widget.parent.refreshEntries();
				}
			}
			else if (this.type == Type.REMOVE)
			{
				CameraPresetManager.getInstance().remove(this.widget.preset.getId());
				this.widget.parent.refreshEntries();
			}
		}

		public enum Type
		{
			RECALL      ("tweakeroo.gui.button.preset_entry.recall"),
			RENAME      ("tweakeroo.gui.button.preset_entry.rename"),
			SET_HERE    ("tweakeroo.gui.button.preset_entry.set_here"),
			REMOVE      ("tweakeroo.gui.button.preset_entry.remove"),
			;

			private final String translationKey;

			Type(String translationKey)
			{
				this.translationKey = translationKey;
			}

			public String getTranslationKey()
			{
				return this.translationKey;
			}

			public String getDisplayName(Object... args)
			{
				return StringUtils.translate(this.getTranslationKey(), args);
			}
		}
	}

	private static class PresetRenamer implements IStringConsumerFeedback
	{
		private final WidgetCameraPresetEntry widget;
		private final CameraPreset preset;

		public PresetRenamer(CameraPreset preset, WidgetCameraPresetEntry widget)
		{
			this.widget = widget;
			this.preset = preset;
		}

		@Override
		public boolean setString(String string)
		{
			if (string.isEmpty())
			{
				string = "Preset "+this.preset.getId();
			}

			String newName = CameraUtils.fixPresetName(string);
			boolean result = this.preset.renamePreset(newName, this.widget.parent);

			CameraPresetManager.getInstance().update(this.preset);
			this.widget.parent.refreshEntries();

			return result;
		}
	}
}
