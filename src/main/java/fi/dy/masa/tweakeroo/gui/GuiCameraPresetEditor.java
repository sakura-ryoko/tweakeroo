package fi.dy.masa.tweakeroo.gui;

import org.jetbrains.annotations.Nullable;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.data.CameraPresetManager;
import fi.dy.masa.tweakeroo.gui.widgets.WidgetCameraPresetEntry;
import fi.dy.masa.tweakeroo.gui.widgets.WidgetCameraPresetList;
import fi.dy.masa.tweakeroo.util.CameraPreset;
import fi.dy.masa.tweakeroo.util.CameraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class GuiCameraPresetEditor  extends GuiListBase<CameraPreset, WidgetCameraPresetEntry, WidgetCameraPresetList>
									implements ISelectionListener<CameraPreset>
{
	private ResourceKey<Level> dimKey;
	private boolean showAll;

	public GuiCameraPresetEditor()
	{
		super(10, 44);
		this.title = StringUtils.translate("tweakeroo.gui.title.camera_preset_editor");

		if (this.mc.level != null)
		{
			this.dimKey = this.mc.level.dimension();
			this.showAll = false;
		}
		else
		{
			this.dimKey = null;
			this.showAll = true;
		}
	}

	@Override
	protected int getBrowserWidth()
	{
		return this.getScreenWidth() - 20;
	}

	@Override
	protected int getBrowserHeight()
	{
		return this.getScreenHeight() - 80;
	}

	@Override
	public void initGui()
	{
		super.initGui();

		int x = 12;
		int y = 24;

		x += this.createButton(x, y, -1, ButtonListener.Type.SHOW) + 4;

		int posX = this.getBrowserWidth() - 22;
		posX -= ButtonListener.Type.CLEAR.getDisplayName().length() + 15;
		posX -= this.createButton(posX, y, -1, ButtonListener.Type.CLEAR) + 4;
		posX -= ButtonListener.Type.CREATE.getDisplayName().length() + 15;
		posX -= this.createButton(posX, y, -1, ButtonListener.Type.CREATE) + 4;
		y += 22;
	}

	private int createButton(int x, int y, int width, ButtonListener.Type type)
	{
		ButtonListener listener = new ButtonListener(type, this);
		String dimStr = this.showAll ?
		                StringUtils.translate("tweakeroo.gui.label.preset_gui.all") :
		                StringUtils.translate("tweakeroo.gui.label.preset_gui.dim");
		String label = type.getDisplayName(dimStr);

		if (width == -1)
		{
			width = this.getStringWidth(label) + 10;
		}

		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, label);

		if (type == ButtonListener.Type.SHOW)
		{
			button.setHoverStrings("tweakeroo.gui.button.hover.preset_gui.show");
		}
//		else if (type == ButtonListener.Type.CREATE)
//		{
//			button.setHoverStrings("tweakeroo.gui.button.hover.preset_gui.create");
//		}
//		else if (type == ButtonListener.Type.CLEAR)
//		{
//			button.setHoverStrings("tweakeroo.gui.button.hover.preset_gui.clear");
//		}

		this.addButton(button, listener);

		return width + 2;
	}

	public boolean shouldShowAll()
	{
		return this.showAll;
	}

	private void toggleShowAll(boolean toggle)
	{
		if (this.mc.level == null)
		{
			this.dimKey = null;
			this.showAll = true;
		}
		else
		{
			this.showAll = toggle;
		}

		this.reCreateListWidget();
	}

	@Override
	protected WidgetCameraPresetList createListWidget(int listX, int listY)
	{
		return new WidgetCameraPresetList(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), this.dimKey, this);
	}

	@Override
	public void onSelectionChange(@Nullable CameraPreset entry)
	{
		CameraPreset old = CameraPresetManager.getInstance().getSelectedPreset();
		CameraPresetManager.getInstance().setSelectedPreset(old == entry ? null : entry);
	}

	private static class ButtonListener implements IButtonActionListener
	{
		private final GuiCameraPresetEditor parent;
		private final Type type;

		public ButtonListener(Type type, GuiCameraPresetEditor parent)
		{
			this.type = type;
			this.parent = parent;
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton)
		{
			if (this.type == Type.SHOW)
			{
				this.parent.toggleShowAll(!this.parent.showAll);
			}
			else if (this.type == Type.CLEAR)
			{
				if (this.parent.showAll)
				{
					CameraPresetManager.getInstance().clear();
					InfoUtils.showGuiMessage(Message.MessageType.SUCCESS, "tweakeroo.message.free_cam.preset_deleted_all");
				}
				else
				{
					CameraPresetManager.getInstance().clear(this.parent.dimKey);
					InfoUtils.showGuiMessage(Message.MessageType.SUCCESS, "tweakeroo.message.free_cam.preset_deleted_all_dim", this.parent.dimKey.identifier().toString());
				}
			}
			else if (this.type == Type.CREATE)
			{
				Minecraft mc = Minecraft.getInstance();

				if (mc.level != null && mc.getCameraEntity() != null)
				{
					Entity camera = mc.getCameraEntity();
					ResourceKey<Level> dimKey = mc.level.dimension();

					final int id = CameraPresetManager.getInstance().getNextId(-1);
					String name = "Preset "+id;
					CameraPreset newPreset = new CameraPreset(id, name, dimKey.identifier(), camera.position(), camera.getYRot(), camera.getXRot());

					if (CameraUtils.addPreset(newPreset))
					{
						InfoUtils.showGuiMessage(Message.MessageType.SUCCESS, "tweakeroo.message.free_cam.preset_added", newPreset.toShortString());
					}
					else
					{
						InfoUtils.showGuiMessage(Message.MessageType.ERROR, "tweakeroo.message.free_cam.preset_already_in_use");
					}
				}
			}

			if (this.parent.getListWidget() != null)
			{
				this.parent.getListWidget().refreshEntries();
			}

			this.parent.initGui();  // Re-Create
		}

		public enum Type
		{
			SHOW    ("tweakeroo.gui.button.preset_gui.show"),
			CLEAR   ("tweakeroo.gui.button.preset_gui.clear"),
			CREATE  ("tweakeroo.gui.button.preset_gui.create"),
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
}
