package fi.dy.masa.tweakeroo.util;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Orientation;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class OrientationUtils
{
    public static Orientation flipFacing(Orientation orig)
    {
        Direction facing = orig.getFacing();
        Direction rot = orig.getRotation();
        Direction adjusted = facing.getOpposite();

        return Orientation.byDirections(adjusted, rot);
    }

    public static int getFacingIndex(ItemStack stackIn, Direction facing)
    {
        Tweakeroo.debugLog("getFacingIndex: facing: [{}]", facing.getName());

        if (stackIn.getItem() instanceof BlockItem blockItem)
        {
            BlockState defaultState = blockItem.getBlock().getDefaultState();

            if (defaultState.contains(Properties.ORIENTATION))
            {
                List<Orientation> list = Arrays.stream(Orientation.values()).toList();

                for (int i = 0; i < list.size(); i++)
                {
                    Orientation o = list.get(i);

                    Tweakeroo.debugLog("getFacingIndex[{}]: name: [{}]", i, o.asString());

                    if (o.getFacing().equals(facing))
                    {
                        /*
                        if (facing.equals(Direction.UP) || facing.equals(Direction.DOWN))
                        {
                            // North Rotation.
                            i++;
                        }
                         */

                        return i;
                    }
                }
            }
        }

        return -1;
    }

    public static Orientation flipRotation(Orientation orig)
    {
        Direction facing = orig.getFacing();
        Direction rot = orig.getRotation();
        Direction adjusted = rot.getOpposite();

        return Orientation.byDirections(facing, adjusted);
    }

    public static Orientation rotateFacingCW(Orientation orig, Direction.Axis axis)
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

    public static Orientation rotateRotationCW(Orientation orig, Direction.Axis axis)
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

    public static Orientation rotateFacingCCW(Orientation orig, Direction.Axis axis)
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

    public static Orientation rotateRotationCCW(Orientation orig, Direction.Axis axis)
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
