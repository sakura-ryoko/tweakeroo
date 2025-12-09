package fi.dy.masa.tweakeroo.mixin.screen;

import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreateFlatWorldScreen.class)
public interface IMixinCustomizeFlatLevelScreen
{
    @Accessor("parent")
    CreateWorldScreen tweakeroo_getCreateWorldParent();
}
