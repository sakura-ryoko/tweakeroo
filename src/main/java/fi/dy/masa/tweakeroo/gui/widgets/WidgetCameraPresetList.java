package fi.dy.masa.tweakeroo.gui.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetSearchBar;
import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.util.KeyCodes;
import fi.dy.masa.tweakeroo.data.CameraPresetManager;
import fi.dy.masa.tweakeroo.gui.GuiCameraPresetEditor;
import fi.dy.masa.tweakeroo.util.CameraPreset;

public class WidgetCameraPresetList extends WidgetListBase<CameraPreset, WidgetCameraPresetEntry>
{
	private final GuiCameraPresetEditor parent;
	private List<CameraPreset> presets;
	private final ResourceKey<Level> dimKey;
	private static int lastScrollbarPosition;
	private boolean scrollbarRestored;

	public WidgetCameraPresetList(int x, int y, int width, int height,
								  @Nullable ResourceKey<Level> dimKey,
	                              @Nullable GuiCameraPresetEditor parent)
	{
		super(x, y, width, height, parent);
		this.parent = parent;
		this.widgetSearchBar = new WidgetSearchBar(x + 2, y + 8, width - 14, 14, 0, MaLiLibIcons.SEARCH, LeftRight.LEFT);
		this.widgetSearchBar.setZLevel(1);
		this.dimKey = dimKey;
		this.browserEntriesOffsetY = 17;
		this.browserEntryHeight = 22;
		this.presets = new ArrayList<>();
		this.updatePresets();
	}

	private void updatePresets()
	{
		this.presets.clear();

		if (this.dimKey == null || this.parent.shouldShowAll())
		{
			this.presets = CameraPresetManager.getInstance().toList();
		}
		else
		{
			this.presets = CameraPresetManager.getInstance().toList(this.dimKey);
		}
	}

	public GuiCameraPresetEditor getPresetEditorGui()
	{
		return this.parent;
	}

	@Override
	public boolean onKeyTyped(KeyEvent input)
	{
		if (input.key() == KeyCodes.KEY_ESCAPE)
		{
			this.parent.onClose();
		}

		return super.onKeyTyped(input);
	}

	@Override
	public void drawContents(GuiContext ctx, int mouseX, int mouseY, float partialTicks)
	{
		super.drawContents(ctx, mouseX, mouseY, partialTicks);
		lastScrollbarPosition = this.scrollBar.getValue();
	}

	@Override
	protected void offsetSelectionOrScrollbar(int amount, boolean changeSelection)
	{
		super.offsetSelectionOrScrollbar(amount, changeSelection);
		lastScrollbarPosition = this.scrollBar.getValue();
	}

	@Override
	protected Collection<CameraPreset> getAllEntries()
	{
		return this.presets;
	}

	@Override
	protected List<String> getEntryStringsForFilter(CameraPreset entry)
	{
		List<String> list = new ArrayList<>();

		if (entry != null)
		{
			list.add(Integer.toString(entry.getId()));
			list.add(entry.getName().toLowerCase());
			list.add(entry.getDim().getPath().toLowerCase());

			return list;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	@Override
	protected void refreshBrowserEntries()
	{
		super.refreshBrowserEntries();

		if (this.scrollbarRestored == false && lastScrollbarPosition <= this.scrollBar.getMaxValue())
		{
			// This needs to happen after the setMaxValue() has been called in reCreateListEntryWidgets()
			this.scrollBar.setValue(lastScrollbarPosition);
			this.scrollbarRestored = true;
			this.reCreateListEntryWidgets();
		}
	}

	@Override
	protected void reCreateListEntryWidgets()
	{
		this.updatePresets();
		super.reCreateListEntryWidgets();
	}

	@Override
	protected WidgetCameraPresetEntry createListEntryWidget(int x, int y, int listIndex, boolean isOdd,
	                                                        CameraPreset entry)
	{
		return new WidgetCameraPresetEntry(x, y, this.browserEntryWidth,
		                                   this.getBrowserEntryHeightFor(entry),
		                                   isOdd, entry, listIndex, this);
	}
}
