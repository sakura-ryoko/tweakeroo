package fi.dy.masa.tweakeroo.command;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import fi.dy.masa.malilib.interfaces.IClientCommandListener;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.data.CameraPresetManager;
import fi.dy.masa.tweakeroo.util.CameraPreset;
import fi.dy.masa.tweakeroo.util.CameraUtils;

@ApiStatus.Experimental
public class FcCommand implements IClientCommandListener
{
	private final String PREFIX = Reference.MOD_ID+".message.free_cam.preset";

	@Override
	public String getCommand()
	{
		return "#fc";
	}

	@Override
	public boolean execute(List<String> args, MinecraftClient mc)
	{
		List<String> list = new ArrayList<>(args);      // Copy it first
		list.removeFirst();
		Sub sub = Sub.fromString(list.removeFirst());

		if (sub != null)
		{
			if (sub.needsArgs() && list.isEmpty())
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(  PREFIX+"_not_enough_args_given"));
				return true;
			}

			return switch (sub.getName().toLowerCase())
			{
				case "add" -> this.executeAdd(list, mc);
				case "set" -> this.executeSet(list, mc);
				case "del" -> this.executeDel(list, mc);
				case "del_all" -> this.executeDelAll(list, mc);
				case "list" -> this.executeList(list, mc);
				case "rename" -> this.executeRename(list, mc);
				case "recall" -> this.executeRecall(list, mc);
				case "cycle" -> this.executeCycle(list, mc);
				case "help" -> this.executeHelp(list, mc);
				default -> throw new IllegalStateException("Unexpected value: " + sub.getName());
			};
		}

		mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_invalid_operation"));
		return true;
	}

	private boolean executeAdd(List<String> args, MinecraftClient mc)
	{
		if (mc.world != null && mc.getCameraEntity() != null)
		{
			RegistryKey<World> dimKey = mc.world.getRegistryKey();
			Entity camera = mc.getCameraEntity();
			final int id = CameraPresetManager.getInstance().getNextId(-1);
			String name = !args.isEmpty() ? CameraUtils.fixPresetName(args.toString()) : "Preset "+id;
			CameraPreset newPreset = new CameraPreset(id, name, dimKey.getValue(), camera.getEntityPos(), camera.getYaw(), camera.getPitch());

			if (CameraUtils.addPreset(newPreset))
			{
				InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_added", newPreset.toShortString()));
			}
			else
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_already_in_use"));
			}
		}

		return true;
	}

	private boolean executeSet(List<String> args, MinecraftClient mc)
	{
		if (mc.world != null && mc.getCameraEntity() != null)
		{
			int id;

			try
			{
				id = Integer.parseInt(args.getFirst());

				if (CameraPresetManager.getInstance().hasId(id))
				{
					RegistryKey<World> dimKey = mc.world.getRegistryKey();
					Entity camera = mc.getCameraEntity();
					CameraPreset oldPreset = CameraPresetManager.getInstance().get(id);

					if (oldPreset != null)
					{
						CameraPreset newPreset = new CameraPreset(id, oldPreset.getName(), dimKey.getValue(), camera.getEyePos(), camera.getYaw(), camera.getPitch());

						CameraUtils.updatePreset(newPreset);
						InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_updated", newPreset.toShortString()));
					}
				}
				else
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_not_found", String.format("%02d", id)));
				}
			}
			catch (Exception err)
			{
				Tweakeroo.LOGGER.error("FcCommand#set(): Exception; {}", err.getLocalizedMessage());
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_invalid", args.getFirst()));
			}
		}

		return true;
	}

	private boolean executeDel(List<String> args, MinecraftClient mc)
	{
		int id;

		try
		{
			if (!args.isEmpty())
			{
				id = Integer.parseInt(args.getFirst());

				if (CameraPresetManager.getInstance().hasId(id))
				{
					CameraPreset oldPreset = CameraPresetManager.getInstance().get(id);

					if (CameraUtils.deletePreset(oldPreset))
					{
						InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX + "_deleted", oldPreset.toShortString()));
					}
				}
				else
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX + "_not_found", String.format("%02d", id)));
				}
			}
			else
			{
				if (mc.world != null && mc.getCameraEntity() != null)
				{
					CameraPreset oldPreset = CameraPresetManager.getInstance().getAtPosition(mc.getCameraEntity());

					if (CameraUtils.deletePresetAtPosition(mc) && oldPreset != null)
					{
						InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX + "_deleted", oldPreset.toShortString()));
					}
				}
			}
		}
		catch (Exception err)
		{
			Tweakeroo.LOGGER.error("FcCommand#del(): Exception; {}", err.getLocalizedMessage());
			mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_invalid", args.getFirst()));
		}

		return true;
	}

	private boolean executeDelAll(List<String> args, MinecraftClient mc)
	{
		if (mc.world != null)
		{
			RegistryKey<World> dimKey = mc.world.getRegistryKey();

			if (CameraUtils.deleteAllPresets(dimKey))
			{
				InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_deleted_all_dim", dimKey.getValue().toString()));
			}
		}

		return true;
	}

	private boolean executeList(List<String> args, MinecraftClient mc)
	{
		if (mc.world != null)
		{
			RegistryKey<World> dimKey = mc.world.getRegistryKey();
			List<CameraPreset> list = CameraPresetManager.getInstance().toList(dimKey);

			if (list.isEmpty())
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_list_empty"));
			}
			else
			{
				for (CameraPreset entry : list)
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_list", entry.toShortString()));
				}
			}
		}

		return true;
	}

	private boolean executeRename(List<String> args, MinecraftClient mc)
	{
		int id;

		try
		{
			id = Integer.parseInt(args.getFirst());

			if (CameraPresetManager.getInstance().hasId(id))
			{
				args.removeFirst();

				if (!args.isEmpty())
				{
					CameraPreset oldPreset = CameraPresetManager.getInstance().get(id);
					String newName = CameraUtils.fixPresetName(args.toString());

					if (oldPreset != null)
					{
						if (newName.isEmpty())
						{
							newName = "Preset " + oldPreset.getId();
						}

						if (CameraUtils.renamePreset(oldPreset, newName))
						{
							InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX + "_renamed", String.format("%02d", id), oldPreset.getName(), newName));
						}
					}
				}
				else
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX + "_not_enough_args_given"));
				}
			}
			else
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX + "_not_found", String.format("%02d", id)));
			}
		}
		catch (Exception err)
		{
			Tweakeroo.LOGGER.error("FcCommand#rename(): Exception; {}", err.getLocalizedMessage());
			mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_invalid", args.getFirst()));
		}

		return true;
	}

	private boolean executeRecall(List<String> args, MinecraftClient mc)
	{
		int id;

		try
		{
			id = Integer.parseInt(args.getFirst());

			if (CameraPresetManager.getInstance().hasId(id))
			{
				CameraPreset preset = CameraPresetManager.getInstance().get(id);

				if (preset != null && mc.world != null)
				{
					if (mc.world.getRegistryKey().getValue().equals(preset.getDim()))
					{
						if (CameraUtils.recallPreset(preset, mc))
						{
							InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX + "_recalled", FeatureToggle.TWEAK_FREE_CAMERA.getPrettyName(), String.format("%02d", preset.getId()), preset.getName()));
						}
						else
						{
							mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX + "_matches_camera", String.format("%02d", preset.getId())));
						}
					}
					else
					{
						mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX + "_wrong_dimension", String.format("%02d", preset.getId()), preset.getName()));
					}
				}
			}
			else
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_not_found", String.format("%02d", id)));
			}
		}
		catch (Exception err)
		{
			Tweakeroo.LOGGER.error("FcCommand#recall(): Exception; {}", err.getLocalizedMessage());
			mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_invalid", args.getFirst()));
		}

		return true;
	}

	private boolean executeCycle(List<String> args, MinecraftClient mc)
	{
//		mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_not_implemented", args.getFirst()));

		if (mc.world != null)
		{
			RegistryKey<World> dimKey = mc.world.getRegistryKey();
			CameraPreset preset = null;
			boolean exception = false;
			int id;

			if (args.isEmpty())
			{
				preset = CameraPresetManager.getInstance().cycle(dimKey);
			}
			else
			{
				try
				{
					id = Integer.parseInt(args.getFirst());

					if (CameraPresetManager.getInstance().hasId(id))
					{
						preset = CameraPresetManager.getInstance().get(id);
						CameraPresetManager.getInstance().setLastPreset(id);
					}
				}
				catch (Exception err)
				{
					Tweakeroo.LOGGER.error("FcCommand#cycle(): Exception; {}", err.getLocalizedMessage());
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_invalid", args.getFirst()));
					exception = true;
				}
			}

			if (preset != null)
			{
				if (CameraUtils.recallPreset(preset, mc))
				{
					InfoUtils.printActionbarMessage(StringUtils.translate(PREFIX+"_recalled", FeatureToggle.TWEAK_FREE_CAMERA.getPrettyName(), String.format("%02d", preset.getId()), preset.getName()));
				}
				else
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_matches_camera", String.format("%02d", preset.getId())));
				}
			}
			else if (!exception)
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_cycle_not_found"));
			}
		}

		return true;
	}

	private boolean executeHelp(List<String> args, MinecraftClient mc)
	{
		final String prefix = PREFIX+"_help";

		if (!args.isEmpty())
		{
			Sub sub = Sub.fromString(args.getFirst());

			if (sub != null)
			{
				String key = sub.getName();

				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(prefix));
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(prefix+"."+key));

				if (!sub.getAlias().isEmpty())
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(prefix+"_alias", sub.getAlias().toString()));
				}
			}
			else
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(PREFIX+"_invalid_operation"));
			}
		}
		else
		{
			mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(prefix));

			for (Sub entry : Sub.values())
			{
				String key = entry.getName();

				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(prefix+"."+key));

				if (!entry.getAlias().isEmpty())
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText(prefix+"_alias", entry.getAlias().toString()));
				}
			}
		}

		return true;
	}

	public enum Sub
	{
		ADD     ("add",     false, List.of("a", "new")),
		SET     ("set",     true,  List.of("s", "update", "upd")),
		DEL     ("del",     true,  List.of("d", "delete")),
		DEL_ALL ("del_all", false, List.of("da", "del-all")),
		LIST    ("list",    false, List.of("l", "lst")),
		RENAME  ("rename",  true,  List.of("n", "ren")),
		RECALL  ("recall",  true,  List.of("r", "rec", "goto", "go")),
		CYCLE   ("cycle",   false, List.of("c", "cyc")),
		HELP    ("help",    false, List.of("h", "hlp"));

		private final String name;
		private final boolean needsArgs;
		private final List<String> alias;

		Sub(String name, boolean needsArgs, List<String> alias)
		{
			this.name = name;
			this.needsArgs = needsArgs;
			this.alias = alias;
		}

		public String getName()
		{
			return this.name;
		}

		public boolean needsArgs()
		{
			return this.needsArgs;
		}

		public List<String> getAlias()
		{
			return this.alias;
		}

		public static @Nullable Sub fromString(String s)
		{
			for (Sub entry : values())
			{
				if (entry.getName().equalsIgnoreCase(s))
				{
					return entry;
				}

				for (String alias : entry.getAlias())
				{
					if (alias.equalsIgnoreCase(s))
					{
						return entry;
					}
				}
			}

			return null;
		}
	}
}
