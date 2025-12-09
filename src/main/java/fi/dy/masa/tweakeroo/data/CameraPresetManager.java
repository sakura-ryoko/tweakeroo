package fi.dy.masa.tweakeroo.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.mojang.serialization.JsonOps;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.util.CameraPreset;

/**
 * A Camera Presets Data Manager
 */
public class CameraPresetManager
{
	private static final CameraPresetManager INSTANCE = new CameraPresetManager();

	public static CameraPresetManager getInstance()
	{
		return INSTANCE;
	}

	private final List<CameraPreset> presets;
	private CameraPreset selectedPreset;
	private int lastPreset;

	private CameraPresetManager()
	{
		this.presets = new ArrayList<>();
		this.lastPreset = -1;
		this.selectedPreset = null;
	}

	public @Nullable CameraPreset getSelectedPreset()
	{
		return this.selectedPreset;
	}

	public void setSelectedPreset(@Nullable CameraPreset preset)
	{
		this.selectedPreset = preset;
	}

	/**
	 * Return whether a Preset with getId() exists
	 *
	 * @param id ()
	 * @return (True|False)
	 */
	public boolean hasId(final int id)
	{
		AtomicBoolean bool = new AtomicBoolean(false);

		this.presets.forEach(
				(entry) ->
				{
					if (entry.getId() == id)
					{
						bool.set(true);
					}
				}
		);

		return bool.get();
	}

	/**
	 * Gets the next available Preset ID
	 * @param start (Starting ID to check)
	 * @return (The Next Free ID)
	 */
	public int getNextId(int start)
	{
		if (this.presets.isEmpty())
		{
			return 1;
		}

		int index = Math.max(start, 1);

		for (CameraPreset entry : this.presets)
		{
			if (entry.getId() == index)
			{
				index++;
			}
		}

		if (this.hasId(index))
		{
			return this.getNextId(index);
		}

		return index;
	}

	/**
	 * Return whether a preset position has been previously saved
	 *
	 * @param other ()
	 * @return (True|False)
	 */
	public boolean hasPosition(@Nonnull CameraPreset other)
	{
		AtomicBoolean bool = new AtomicBoolean(false);

		this.presets.forEach(
				(ent) ->
				{
					if (ent.equals(other))
					{
						bool.set(true);
					}
				}
		);

		return bool.get();
	}

	/**
	 * Add a new Preset.  Return if the record as added.
	 *
	 * @param preset ()
	 * @return (True|False)
	 */
	public boolean add(CameraPreset preset)
	{
		return this.add(preset, true);
	}

	/**
	 * Add a new Preset with an optional message.  Return if the record as added.
	 *
	 * @param preset  ()
	 * @param message (True|False)
	 * @return (True|False)
	 */
	public boolean add(CameraPreset preset, boolean message)
	{
		if (!this.hasId(preset.getId()))
		{
			if (this.hasPosition(preset))
			{
				if (message)
				{
					Tweakeroo.LOGGER.error("CameraPresetCache: Error adding new preset [{}]; A matching position already exists.", preset.getId());
				}

				return false;
			}

			this.presets.add(preset);
			this.presets.sort(Comparator.comparingInt(CameraPreset::getId));

			if (message)
			{
				Tweakeroo.LOGGER.info("CameraPresetCache: Added new preset [{}/{}]", preset.getId(), preset.getName());
			}

			return true;
		}

		return false;
	}

	/**
	 * Get a Preset by ID, or NULL.
	 *
	 * @param id ()
	 * @return ()
	 */
	public @Nullable CameraPreset get(final int id)
	{
		for (CameraPreset entry : this.presets)
		{
			if (entry.getId() == id)
			{
				return entry;
			}
		}

		return null;
	}

	/**
	 * Gets any Presets located at the Camera position.
	 * @param camera ()
	 * @return ()
	 */
	public @Nullable CameraPreset getAtPosition(@Nonnull Entity camera)
	{
		for (CameraPreset entry : this.presets)
		{
			if (entry.equals(camera))
			{
				return entry;
			}
		}

		return null;
	}

	/**
	 * Remove a preset by ID.  Return if one was removed.
	 *
	 * @param id ()
	 * @return (True|False)
	 */
	public boolean remove(final int id)
	{
		return this.remove(id, true);
	}

	/**
	 * Remove a preset by ID, with a message.  Return if one was removed.
	 *
	 * @param id      ()
	 * @param message (True|False)
	 * @return (True|False)
	 */
	public boolean remove(final int id, boolean message)
	{
		CameraPreset oldPreset = this.get(id);

		if (oldPreset != null)
		{
			this.presets.remove(oldPreset);

			if (message)
			{
				Tweakeroo.LOGGER.info("CameraPresetCache: Removed preset [{}/{}]", oldPreset.getId(), oldPreset.getName());
			}

			return true;
		}

		return false;
	}

	/**
	 * Update a preset by removing one by ID, and replacing it.
	 * @param preset ()
	 * @return (True|False)
	 */
	public boolean update(@Nonnull CameraPreset preset)
	{
		return this.update(preset, true);
	}

	/**
	 * Update a preset by removing one by ID, and replacing it.
	 * @param preset ()
	 * @param message (True|False)
	 * @return (True|False)
	 */
	public boolean update(@Nonnull CameraPreset preset, boolean message)
	{
		this.remove(preset.getId(), message);
		return this.add(preset, message);
	}

	/**
	 * Return if the presets are Empty.
	 *
	 * @return (True|False)
	 */
	public boolean isEmpty() {return this.presets.isEmpty();}

	/**
	 * Return the size of the presets;
	 *
	 * @return ()
	 */
	public int size() {return this.presets.size();}

	/**
	 * Return a sorted List
	 *
	 * @return ()
	 */
	public List<CameraPreset> toList()
	{
		List<CameraPreset> list = new ArrayList<>(this.presets);

		if (!list.isEmpty())
		{
			list.sort(Comparator.comparingInt(CameraPreset::getId));
		}

		return list;
	}

	/**
	 * Return if all presets for a particular dimension is empty
	 *
	 * @param worldKey (dim)
	 * @return (True|False)
	 */
	public boolean isEmpty(@Nonnull ResourceKey<Level> worldKey)
	{
		return this.toList(worldKey).isEmpty();
	}

	/**
	 * Return the size of the preset list for a particular dimension
	 *
	 * @param worldKey (dim)
	 * @return ()
	 */
	public int size(@Nonnull ResourceKey<Level> worldKey)
	{
		return this.toList(worldKey).size();
	}

	/**
	 * Return a sorted list for a particular dimension.
	 *
	 * @param worldKey (dim)
	 * @return ()
	 */
	public List<CameraPreset> toList(@Nonnull ResourceKey<Level> worldKey)
	{
		Identifier dim = worldKey.identifier();
		List<CameraPreset> list = new ArrayList<>();

		for (CameraPreset entry : this.presets)
		{
			if (entry != null && entry.getDim().equals(dim))
			{
				list.add(entry);
			}
		}

		list.sort(Comparator.comparingInt(CameraPreset::getId));

		return list;
	}

	/**
	 * Return the last obtained Preset ID. (-1 means none were obtained)
	 *
	 * @return (id)
	 */
	public int getLastPreset()
	{
		return this.lastPreset;
	}

	/**
	 * Set the lastPreset Value manually.
	 *
	 * @param id ()
	 */
	public void setLastPreset(final int id)
	{
		this.lastPreset = id;
	}

	/**
	 * Cycle the Camera Presets; starting with the first entry in the same Dimension; and then any subsequent entries
	 *
	 * @param worldKey (dim)
	 * @return ()
	 */
	public @Nullable CameraPreset cycle(@Nonnull ResourceKey<Level> worldKey)
	{
		Identifier dim = worldKey.identifier();
		boolean getNext = this.lastPreset == -1;

		for (CameraPreset entry : this.presets)
		{
			if (entry != null && entry.getDim().equals(dim))
			{
				if (getNext)
				{
					if (entry.getId() == this.lastPreset)
					{
						// Stop recursion to the same preset.
						CameraPreset first = this.getFirst(worldKey);

						if (first != null)
						{
							if (first.equals(entry))
							{
								return null;
							}

							this.lastPreset = first.getId();
							return first;
						}
					}

					this.lastPreset = entry.getId();
					return entry;
				}
				else if (entry.getId() == this.lastPreset)
				{
					getNext = true;
				}
			}
		}

		if (this.lastPreset != -1)
		{
			CameraPreset last = this.getLast(worldKey);

			if (last != null)
			{
				if (last.getId() == this.lastPreset)
				{
					this.lastPreset = -1;
					return this.getFirst(worldKey);
				}

				this.lastPreset = last.getId();
				return last;
			}
		}

		return null;
	}

	/**
	 * Get the first Camera Preset for a given Dimension
	 *
	 * @param worldKey (dim)
	 * @return ()
	 */
	public @Nullable CameraPreset getFirst(@Nonnull ResourceKey<Level> worldKey)
	{
		Identifier dim = worldKey.identifier();

		for (CameraPreset entry : this.presets)
		{
			if (entry != null && entry.getDim().equals(dim))
			{
				this.lastPreset = entry.getId();
				return entry;
			}
		}

		return null;
	}

	/**
	 * Get the Last Camera Preset for a given Dimension
	 *
	 * @param worldKey (dim)
	 * @return ()
	 */
	public @Nullable CameraPreset getLast(@Nonnull ResourceKey<Level> worldKey)
	{
		Identifier dim = worldKey.identifier();
		CameraPreset last = null;

		for (CameraPreset entry : this.presets)
		{
			if (entry != null && entry.getDim().equals(dim))
			{
				last = entry;
			}
		}

		if (last != null)
		{
			this.lastPreset = last.getId();
		}

		return last;
	}

	/**
	 * Clear all presets in a given Dimension
	 *
	 * @param worldKey (dim)
	 */
	public void clear(@Nonnull ResourceKey<Level> worldKey)
	{
		this.clear(worldKey, true);
	}

	/**
	 * Clear all presets in a given Dimension; with a message toggle
	 *
	 * @param worldKey (dim)
	 * @param message  (True|False)
	 */
	public void clear(@Nonnull ResourceKey<Level> worldKey, boolean message)
	{
		Identifier dim = worldKey.identifier();
		List<CameraPreset> list = new ArrayList<>(this.presets);

		for (CameraPreset entry : list)
		{
			if (entry != null && entry.getDim().equals(dim))
			{
				this.presets.remove(entry);

				if (message)
				{
					Tweakeroo.LOGGER.info("CameraPresetCache: Clear preset [{}/{}]", entry.getId(), entry.getName());
				}
			}
		}
	}

	/**
	 * Clear all Presets
	 */
	public void clear()
	{
		this.clear(true);
	}

	/**
	 * Clear all presets; with a message
	 *
	 * @param message (True|False)
	 */
	public void clear(boolean message)
	{
		this.presets.clear();

		if (message)
		{
			Tweakeroo.LOGGER.warn("CameraPresetCache: All presets cleared.");
		}
	}

	/**
	 * Write all presets to a Json Array as an Element
	 *
	 * @return (element)
	 */
	public @Nullable JsonElement toJson()
	{
		JsonObject obj = new JsonObject();

		List<CameraPreset> sorted = this.toList();

		if (sorted.isEmpty())
		{
			return null;
		}

		JsonArray arr = new JsonArray();

		for (CameraPreset entry : sorted)
		{
			CameraPreset.CODEC.encodeStart(JsonOps.INSTANCE, entry).resultOrPartial().ifPresent(arr::add);
		}

		if (arr.size() > 0)
		{
			obj.add("list", arr);
		}

		return obj;
	}

	/**
	 * Load all stored Presets from a Json Array
	 *
	 * @param ele ()
	 */
	public void fromJson(JsonElement ele)
	{
		try
		{
			if (ele.isJsonObject())
			{
				JsonObject obj = ele.getAsJsonObject();

				if (JsonUtils.hasArray(obj, "list"))
				{
					JsonArray arr = obj.get("list").getAsJsonArray();

					this.presets.clear();

					for (int i = 0; i < arr.size(); i++)
					{
						CameraPreset entry = CameraPreset.CODEC.parse(JsonOps.INSTANCE, arr.get(i)).getOrThrow();

						if (entry != null)
						{
							this.add(entry, false);
						}
					}
				}
				// Empty / Invalid
			}
		}
		catch(Exception err)
		{
			Tweakeroo.LOGGER.error("CameraPresetCache#fromJson(): Exception deserializing Camera Presets; {}", err.getLocalizedMessage());
		}
	}
}
