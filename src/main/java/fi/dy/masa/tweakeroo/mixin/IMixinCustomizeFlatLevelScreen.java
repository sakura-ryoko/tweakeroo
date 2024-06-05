package fi.dy.masa.tweakeroo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.CustomizeFlatLevelScreen;

@Mixin(CustomizeFlatLevelScreen.class)
public interface IMixinCustomizeFlatLevelScreen
{
    @Accessor("parent")
    CreateWorldScreen tweakeroo_getCreateWorldParent();
}
