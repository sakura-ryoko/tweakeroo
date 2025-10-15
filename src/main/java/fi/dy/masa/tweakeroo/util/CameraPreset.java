package fi.dy.masa.tweakeroo.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@ApiStatus.Experimental
public record CameraPreset(int id, String name, Identifier dim, Vec3d pos, float yaw, float pitch)
{
	public static final Codec<CameraPreset> CODEC = RecordCodecBuilder.create(
			inst -> inst.group(
					PrimitiveCodec.INT.fieldOf("id").forGetter(CameraPreset::id),
					PrimitiveCodec.STRING.fieldOf("name").forGetter(CameraPreset::name),
					Identifier.CODEC.fieldOf("dim").forGetter(CameraPreset::dim),
					Vec3d.CODEC.fieldOf("pos").forGetter(CameraPreset::pos),
					PrimitiveCodec.FLOAT.fieldOf("yaw").forGetter(CameraPreset::yaw),
					PrimitiveCodec.FLOAT.fieldOf("pitch").forGetter(CameraPreset::pitch)
			).apply(inst, CameraPreset::new)
	);
	public static final CameraPreset EMPTY = new CameraPreset(-1, "EMPTY", World.OVERWORLD.getValue(), Vec3d.ZERO, 0.0f, 0.0f);

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
			return   this.dim.equals(camera.getEntityWorld().getRegistryKey().getValue()) &&
					(this.pos.isWithinRangeOf(camera.getEyePos(), 0.75d, 0.75d) ||        // 3/4-block offset ?
					 this.pos.isWithinRangeOf(camera.getEntityPos(), 0.75d, 0.75d)) &&
					 Math.abs(this.yaw - camera.getYaw()) < 35.0f &&        // 35 deg offset ?
					 Math.abs(this.pitch - camera.getPitch()) < 35.0f;
		}

		return false;
	}

	/**
	 * Format this in a less complex format.
	 * @return ()
	 */
	public String toShortString()
	{
		return "[#"+String.format("%02d", this.id)+", "+this.name+"]"+
				" "+this.dim.getPath()+":"+
				" ("+BlockPos.ofFloored(this.pos.x, this.pos.y, this.pos.z).toShortString()+")";
	}
}
