package fi.dy.masa.tweakeroo.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.interfaces.IMessageConsumer;
import fi.dy.masa.malilib.util.StringUtils;

public class CameraPreset
{
	public static final Codec<CameraPreset> CODEC = RecordCodecBuilder.create(
			inst -> inst.group(
					PrimitiveCodec.INT.fieldOf("id").forGetter(get -> get.id),
					PrimitiveCodec.STRING.fieldOf("name").forGetter(get -> get.name),
					Identifier.CODEC.fieldOf("dim").forGetter(get -> get.dim),
					Vec3.CODEC.fieldOf("pos").forGetter(get -> get.pos),
					PrimitiveCodec.FLOAT.fieldOf("yaw").forGetter(get -> get.yaw),
					PrimitiveCodec.FLOAT.fieldOf("pitch").forGetter(get -> get.pitch)
			).apply(inst, CameraPreset::new)
	);
	public static final CameraPreset EMPTY = new CameraPreset(-1, "EMPTY", Level.OVERWORLD.identifier(), Vec3.ZERO, 0.0f, 0.0f);
	private final int id;
	private String name;
	private final Identifier dim;
	private Vec3 pos;
	private float yaw;
	private float pitch;

	public CameraPreset(final int id, String name, Identifier dim, Vec3 pos, float yaw, float pitch)
	{
		this.id = id;
		this.name = name;
		this.dim = dim;
		this.pos = pos;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public int getId()
	{
		return this.id;
	}

	public String getName()
	{
		return this.name;
	}

	public Identifier getDim()
	{
		return this.dim;
	}

	public Vec3 getPos()
	{
		return this.pos;
	}

	public float getYaw()
	{
		return this.yaw;
	}

	public float getPitch()
	{
		return this.pitch;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPos(Vec3 pos, float yaw, float pitch)
	{
		this.pos = pos;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public boolean renamePreset(String newName, @Nullable IMessageConsumer feedback)
	{
		String oldName = this.name;
		this.setName(newName);

		if (feedback != null)
		{
			feedback.addMessage(Message.MessageType.SUCCESS, "tweakeroo.message.free_cam.preset_renamed", oldName, newName);
		}

		return true;
	}

	/**
	 * Format this as a String.
	 * @return ()
	 */
	@Override
	public @NotNull String toString()
	{
		return "CameraPreset["+
				"{id=\""+this.id+"\"}"+
				",{name=\""+this.name+"\"}"+
				",{dim=\""+this.dim.toString()+"\"}"+
				",{pos=\""+this.pos.toString()+"\"}"+
				",{yaw=\""+this.yaw+"\"}"+
				",{pitch=\""+this.pitch+"\"}"+
				"]";
	}

	/**
	 * Standard 'equals' that ignores a presets' name and id
	 *
	 * @param o (Preset|Camera Entity)
	 * @return (True|False)
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof CameraPreset other)
		{
			// Should match the exact position.
			return  this.dim.equals(other.dim) &&
					this.pos.equals(other.pos) &&
					this.yaw == other.yaw &&
					this.pitch == other.pitch;
		}
		else if (o instanceof Entity camera)
		{
			// Should match a relative position.  Need to dial this in.
			return   this.dim.equals(camera.level().dimension().identifier()) &&
					(this.pos.closerThan(camera.getEyePosition(), 0.75d, 0.75d) ||        // 3/4-block offset ?
					 this.pos.closerThan(camera.position(), 0.75d, 0.75d)) &&
					 Math.abs(this.yaw - camera.getYRot()) < 35.0f &&        // 35 deg offset ?
					 Math.abs(this.pitch - camera.getXRot()) < 35.0f;
		}

		return false;
	}

	public String toIdName()
	{
		return String.format("%02d", this.id)+", "+this.name;
	}

	public List<String> getWidgetHoverLines()
	{
		List<String> lines = new ArrayList<>();

		lines.add(StringUtils.translate("tweakeroo.gui.hover.camera_preset.id", String.format("%02d", this.id)));
		lines.add(StringUtils.translate("tweakeroo.gui.hover.camera_preset.name", this.name));
		lines.add(StringUtils.translate("tweakeroo.gui.hover.camera_preset.dim", this.dim.toString()));
		lines.add(StringUtils.translate("tweakeroo.gui.hover.camera_preset.pos", String.format("%.3f", this.pos.x), String.format("%.3f", this.pos.y), String.format("%.3f", this.pos.z)));
		lines.add(StringUtils.translate("tweakeroo.gui.hover.camera_preset.yaw_pitch", String.format("%.3f", this.yaw), String.format("%.3f", this.pitch)));

		return lines;
	}

	/**
	 * Format this in a less complex format.
	 * @return ()
	 */
	public String toShortString()
	{
		return  "[#"+this.toIdName()+"]"+
				" "+this.dim.getPath()+":"+
				" ("+BlockPos.containing(this.pos.x, this.pos.y, this.pos.z).toShortString()+")";
	}

	public String toShortStringStyled()
	{
		BlockPos pos = BlockPos.containing(this.pos.x, this.pos.y, this.pos.z);

		return  "[§b#"+String.format("%02d", this.id)+"§r, §a"+this.name+"§r]"+
				" §e"+this.dim.getPath()+":§r"+
				" ("+pos.getX()+", "+pos.getY()+", "+pos.getZ()+")";
	}
}
