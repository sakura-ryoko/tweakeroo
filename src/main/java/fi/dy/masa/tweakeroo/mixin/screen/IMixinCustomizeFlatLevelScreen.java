package fi.dy.masa.tweakeroo.mixin.screen;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.CustomizeFlatLevelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomizeFlatLevelScreen.class)
public interface IMixinCustomizeFlatLevelScreen
{
    @Accessor("parent")
    CreateWorldScreen tweakeroo_getCreateWorldParent();
}
