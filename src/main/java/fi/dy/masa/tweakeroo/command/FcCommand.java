package fi.dy.masa.tweakeroo.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.interfaces.IClientCommandListener;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;
import fi.dy.masa.tweakeroo.data.CameraPresetCache;
import fi.dy.masa.tweakeroo.util.CameraPreset;

@ApiStatus.Experimental
public class FcCommand implements IClientCommandListener
{
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
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_not_enough_args_given"));
				return true;
			}

			return switch (sub.getName().toLowerCase())
			{
				case "add" -> this.executeAdd(list, mc);
				case "set" -> this.executeSet(list, mc);
				case "del" -> this.executeDel(list, mc);
				case "list" -> this.executeList(list, mc);
				case "recall" -> this.executeRecall(list, mc);
				case "cycle" -> this.executeCycle(list, mc);
				case "help" -> this.executeHelp(list, mc);
				default -> throw new IllegalStateException("Unexpected value: " + sub.getName());
			};
		}

		mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_invalid_operation"));
		return true;
	}

	private boolean executeAdd(List<String> args, MinecraftClient mc)
	{
		if (mc.world != null && mc.getCameraEntity() != null)
		{
			RegistryKey<World> dimKey = mc.world.getRegistryKey();
			Entity camera = mc.getCameraEntity();
			final int id = CameraPresetCache.getInstance().size() + 1;
			String name = !args.isEmpty() ? args.toString() : "Preset " + id;
			CameraPreset newPreset = new CameraPreset(id, name, dimKey.getValue(), camera.getEyePos(), camera.getYaw(), camera.getPitch());

			if (!CameraPresetCache.getInstance().hasPosition(newPreset))
			{
				CameraPresetCache.getInstance().add(newPreset);
				Tweakeroo.debugLog("FcCommand#add(): Added new preset: {}", newPreset.toShortString());
				InfoUtils.showInGameMessage(Message.MessageType.SUCCESS, "tweakeroo.message.free_cam.preset_added", newPreset.toShortString());
			}
			else
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_already_in_use"));
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

				if (CameraPresetCache.getInstance().hasId(id))
				{
					RegistryKey<World> dimKey = mc.world.getRegistryKey();
					Entity camera = mc.getCameraEntity();
					CameraPreset oldPreset = CameraPresetCache.getInstance().get(id);

					if (oldPreset != null)
					{
						CameraPreset newPreset = new CameraPreset(id, oldPreset.name(), dimKey.getValue(), camera.getEyePos(), camera.getYaw(), camera.getPitch());

						CameraPresetCache.getInstance().remove(id, false);
						CameraPresetCache.getInstance().add(newPreset, false);
						Tweakeroo.debugLog("FcCommand#set(): Updated preset: {}", newPreset.toShortString());
						InfoUtils.showInGameMessage(Message.MessageType.SUCCESS, "tweakeroo.message.free_cam.preset_updated", newPreset.toShortString());
					}
				}
				else
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_not_found", String.format("%02d", id)));
				}
			}
			catch (Exception err)
			{
				Tweakeroo.LOGGER.error("FcCommand#set(): Exception; {}", err.getLocalizedMessage());
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_invalid", args.getFirst()));
			}
		}

		return true;
	}

	private boolean executeDel(List<String> args, MinecraftClient mc)
	{
		int id;

		try
		{
			id = Integer.parseInt(args.getFirst());

			if (CameraPresetCache.getInstance().hasId(id))
			{
				CameraPreset oldPreset = CameraPresetCache.getInstance().get(id);

				if (oldPreset != null)
				{
					CameraPresetCache.getInstance().remove(id, false);
					Tweakeroo.debugLog("FcCommand#del(): Deleted preset: {}", oldPreset.toShortString());
					InfoUtils.showInGameMessage(Message.MessageType.INFO, "tweakeroo.message.free_cam.preset_deleted", oldPreset.toShortString());
				}
			}
			else
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_not_found", String.format("%02d", id)));
			}
		}
		catch (Exception err)
		{
			Tweakeroo.LOGGER.error("FcCommand#del(): Exception; {}", err.getLocalizedMessage());
			mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_invalid", args.getFirst()));
		}

		return true;
	}

	private boolean executeList(List<String> args, MinecraftClient mc)
	{
		if (mc.world != null)
		{
			RegistryKey<World> dimKey = mc.world.getRegistryKey();
			List<CameraPreset> list = CameraPresetCache.getInstance().toList(dimKey);

			if (list.isEmpty())
			{
				mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_list_empty"));
			}
			else
			{
				for (CameraPreset entry : list)
				{
					mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_list", entry.toShortString()));
				}
			}
		}

		return true;
	}

	private boolean executeRecall(List<String> args, MinecraftClient mc)
	{
		mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_not_implemented", args.getFirst()));
		return false;
	}

	private boolean executeCycle(List<String> args, MinecraftClient mc)
	{
		mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_not_implemented", args.getFirst()));
		return false;
	}

	private boolean executeHelp(List<String> args, MinecraftClient mc)
	{
		mc.inGameHud.getChatHud().addMessage(StringUtils.translateAsText("tweakeroo.message.free_cam.preset_help"));
		return true;
	}

	public enum Sub
	{
		ADD     ("add",     false, List.of("a")),
		SET     ("set",     true,  List.of("s", "update", "upd")),
		DEL     ("del",     true,  List.of("d", "delete")),
		LIST    ("list",    false, List.of("l", "lst")),
		RECALL  ("recall",  true,  List.of("r", "rec")),
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
