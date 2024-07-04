package fi.dy.masa.tweakeroo.util;

import net.minecraft.block.enums.Orientation;
import net.minecraft.util.math.Direction;

public class OrientationUtils
{
    public Orientation flipFacing(Orientation orig)
    {
        Direction facing = orig.getFacing();
        Direction rot = orig.getRotation();
        Direction adjusted = facing.getOpposite();

        return Orientation.byDirections(adjusted, rot);
    }

    public Orientation flipRotation(Orientation orig)
    {
        Direction facing = orig.getFacing();
        Direction rot = orig.getRotation();
        Direction adjusted = rot.getOpposite();

        return Orientation.byDirections(facing, adjusted);
    }

    public Orientation rotateFacingCW(Orientation orig, Direction.Axis axis)
    {
        Direction facing = orig.getFacing();
        Direction rot = orig.getRotation();

        try
        {
            Direction adjusted = facing.rotateClockwise(axis);
            return Orientation.byDirections(adjusted, rot);
        }
        catch (Exception ignored)
        {
            return orig;
        }
    }

    public Orientation rotateRotationCW(Orientation orig, Direction.Axis axis)
    {
        Direction facing = orig.getFacing();
        Direction rot = orig.getRotation();

        try
        {
            Direction adjusted = rot.rotateClockwise(axis);
            return Orientation.byDirections(facing, adjusted);
        }
        catch (Exception ignored)
        {
            return orig;
        }
    }

    public Orientation rotateFacingCCW(Orientation orig, Direction.Axis axis)
    {
        Direction facing = orig.getFacing();
        Direction rot = orig.getRotation();

        try
        {
            Direction adjusted = facing.rotateCounterclockwise(axis);
            return Orientation.byDirections(adjusted, rot);
        }
        catch (Exception ignored)
        {
            return orig;
        }
    }

    public Orientation rotateRotationCCW(Orientation orig, Direction.Axis axis)
    {
        Direction facing = orig.getFacing();
        Direction rot = orig.getRotation();

        try
        {
            Direction adjusted = rot.rotateCounterclockwise(axis);
            return Orientation.byDirections(facing, adjusted);
        }
        catch (Exception ignored)
        {
            return orig;
        }
    }
}
