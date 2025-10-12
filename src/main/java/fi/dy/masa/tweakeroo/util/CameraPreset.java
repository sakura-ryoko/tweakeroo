package fi.dy.masa.tweakeroo.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
					PrimitiveCodec.FLOAT.fieldOf("pitch").forGetter(CameraPreset::yaw)
			).apply(inst, CameraPreset::new)
	);
	public static final CameraPreset EMPTY = new CameraPreset(-1, "EMPTY", World.OVERWORLD.getValue(), Vec3d.ZERO, 0.0f, 0.0f);

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

	// The "equals" doesn't need to check the id or name; only that the positions are equal.
	public boolean equals(CameraPreset other)
	{
		return  this.dim.equals(other.dim) &&
				this.pos.equals(other.pos) &&
				this.yaw == other.yaw &&
				this.pitch == other.pitch;
	}

	public String toShortString()
	{
		return "["+this.id+", "+this.name+"]"+
				" "+this.dim.getPath()+":"+
				" ("+BlockPos.ofFloored(this.pos.x, this.pos.y, this.pos.z).toShortString()+")";
	}
}
