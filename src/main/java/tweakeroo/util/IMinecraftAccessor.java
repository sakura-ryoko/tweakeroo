package tweakeroo.util;

public interface IMinecraftAccessor
{
    void setRightClickDelayTimer(int value);

    void resetLeftClickCooldown();

    void leftClickMouseAccessor();

    void rightClickMouseAccessor();
}
