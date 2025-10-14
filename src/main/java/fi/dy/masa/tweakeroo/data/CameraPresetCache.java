package fi.dy.masa.tweakeroo.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.serialization.JsonOps;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.util.CameraPreset;

/**
 * A Camera Presets Data Provider
 */
@ApiStatus.Experimental
public class CameraPresetCache
{
	private static final CameraPresetCache INSTANCE = new CameraPresetCache();

	public static CameraPresetCache getInstance() {return INSTANCE;}

	private final HashMap<Integer, CameraPreset> presets;
	private int lastPreset;

	private CameraPresetCache()
	{
		this.presets = new HashMap<>();
		this.lastPreset = -1;
	}

	/**
	 * Return whether a Preset with id() exists
	 *
	 * @param id ()
	 * @return (True|False)
	 */
	public boolean hasId(final int id)
	{
		AtomicBoolean bool = new AtomicBoolean(false);

		this.presets.forEach(
				(index, entry) ->
				{
					if (entry.id() == id || index == id)
					{
						bool.set(true);
					}
				}
		);

		return bool.get();
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
				(id, ent) ->
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
		if (!this.hasId(preset.id()))
		{
			if (this.hasPosition(preset))
			{
				if (message)
				{
					Tweakeroo.LOGGER.error("CameraPresetCache: Error adding new preset [{}]; A matching position already exists.", preset.id());
				}

				return false;
			}

			this.presets.put(preset.id(), preset);

			if (message)
			{
				Tweakeroo.LOGGER.info("CameraPresetCache: Added new preset [{}/{}]", preset.id(), preset.name());
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
		if (this.hasId(id))
		{
			return this.presets.get(id);
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
		for (CameraPreset entry : this.presets.values())
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
		CameraPreset oldPreset = this.presets.remove(id);

		if (oldPreset != null)
		{
			if (message)
			{
				Tweakeroo.LOGGER.info("CameraPresetCache: Removed preset [{}/{}]", oldPreset.id(), oldPreset.name());
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
		this.remove(preset.id(), message);
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
		List<CameraPreset> list = new ArrayList<>(this.presets.size());

		for (int i = 0; i < this.presets.size(); i++)
		{
			if (this.hasId(i))
			{
				list.set(i, this.presets.get(i));
			}
		}

		list.sort(Comparator.comparingInt(CameraPreset::id));

		return list;
	}

	/**
	 * Return if all presets for a particular dimension is empty
	 *
	 * @param worldKey (dim)
	 * @return (True|False)
	 */
	public boolean isEmpty(@Nonnull RegistryKey<World> worldKey)
	{
		return this.toList(worldKey).isEmpty();
	}

	/**
	 * Return the size of the preset list for a particular dimension
	 *
	 * @param worldKey (dim)
	 * @return ()
	 */
	public int size(@Nonnull RegistryKey<World> worldKey)
	{
		return this.toList(worldKey).size();
	}

	/**
	 * Return a sorted list for a particular dimension.
	 *
	 * @param worldKey (dim)
	 * @return ()
	 */
	public List<CameraPreset> toList(@Nonnull RegistryKey<World> worldKey)
	{
		Identifier dim = worldKey.getValue();
		List<CameraPreset> list = new ArrayList<>();

		for (CameraPreset entry : this.presets.values())
		{
			if (entry != null && entry.dim().equals(dim))
			{
				list.add(entry);
			}
		}

		list.sort(Comparator.comparingInt(CameraPreset::id));

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
	public @Nullable CameraPreset cycle(@Nonnull RegistryKey<World> worldKey)
	{
		Identifier dim = worldKey.getValue();
		boolean getNext = this.lastPreset == -1;

		for (CameraPreset entry : this.presets.values())
		{
			if (entry != null && entry.dim().equals(dim))
			{
				if (getNext)
				{
					if (entry.id() == this.lastPreset)
					{
						// Stop recursion to the same preset.
						CameraPreset first = this.getFirst(worldKey);

						if (first != null)
						{
							if (first.equals(entry))
							{
								return null;
							}

							this.lastPreset = first.id();
							return first;
						}
					}

					this.lastPreset = entry.id();
					return entry;
				}
				else if (entry.id() == this.lastPreset)
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
				if (last.id() == this.lastPreset)
				{
					this.lastPreset = -1;
					return this.getFirst(worldKey);
				}

				this.lastPreset = last.id();
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
	public @Nullable CameraPreset getFirst(@Nonnull RegistryKey<World> worldKey)
	{
		Identifier dim = worldKey.getValue();

		for (CameraPreset entry : this.presets.values())
		{
			if (entry != null && entry.dim().equals(dim))
			{
				this.lastPreset = entry.id();
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
	public @Nullable CameraPreset getLast(@Nonnull RegistryKey<World> worldKey)
	{
		Identifier dim = worldKey.getValue();
		CameraPreset last = null;

		for (CameraPreset entry : this.presets.values())
		{
			if (entry != null && entry.dim().equals(dim))
			{
				last = entry;
			}
		}

		if (last != null)
		{
			this.lastPreset = last.id();
		}

		return last;
	}

	/**
	 * Clear all presets in a given Dimension
	 *
	 * @param worldKey (dim)
	 */
	public void clear(@Nonnull RegistryKey<World> worldKey)
	{
		this.clear(worldKey, true);
	}

	/**
	 * Clear all presets in a given Dimension; with a message toggle
	 *
	 * @param worldKey (dim)
	 * @param message  (True|False)
	 */
	public void clear(@Nonnull RegistryKey<World> worldKey, boolean message)
	{
		Identifier dim = worldKey.getValue();

		for (CameraPreset entry : this.presets.values())
		{
			if (entry != null && entry.dim().equals(dim))
			{
				this.remove(entry.id(), message);
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

		return arr;
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
			if (ele.isJsonArray())
			{
				JsonArray arr = ele.getAsJsonArray();

				this.presets.clear();

				for (int i = 0; i < arr.size(); i++)
				{
					this.add(CameraPreset.CODEC.decode(JsonOps.INSTANCE, arr.get(i)).getOrThrow().getFirst(), false);
				}
			}
			// Empty / Invalid
		}
		catch (Exception err)
		{
			Tweakeroo.LOGGER.error("CameraPresetCache#fromJson(): Exception deserializing Camera Presets; {}", err.getLocalizedMessage());
		}
	}
}
