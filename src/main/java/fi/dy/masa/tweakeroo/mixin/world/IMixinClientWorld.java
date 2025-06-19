package fi.dy.masa.tweakeroo.mixin.world;

import java.util.Map;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientWorld.class)
public interface IMixinClientWorld
{
    @Invoker("getMapStates")
    Map<MapIdComponent, MapState> tweakeroo_getMapStates();
}
