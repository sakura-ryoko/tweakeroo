package fi.dy.masa.tweakeroo.mixin.world;

import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientLevel.class)
public interface IMixinClientWorld
{
    @Invoker("getAllMapData")
    Map<MapId, MapItemSavedData> tweakeroo_getMapStates();
}
