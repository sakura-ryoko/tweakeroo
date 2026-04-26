package fi.dy.masa.tweakeroo.mixin.fog;

import org.joml.Vector4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FogRenderer.class)
public interface IMixinFogRenderer
{
	@Invoker("computeFogColor")
	Vector4f tweakeroo_computeFogColor(final Camera camera, final float partialTicks, final ClientLevel level, final int renderDistance, final float darkenWorldAmount);
}
