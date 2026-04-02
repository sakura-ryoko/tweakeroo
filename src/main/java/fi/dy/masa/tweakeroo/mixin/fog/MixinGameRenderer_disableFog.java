package fi.dy.masa.tweakeroo.mixin.fog;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.joml.Vector4f;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import fi.dy.masa.malilib.compat.ModIds;
import fi.dy.masa.malilib.util.MathUtils;
import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = GameRenderer.class, priority = 900)
@Restriction(conflict = @Condition(value = ModIds.sodium))
public abstract class MixinGameRenderer_disableFog
{
    @Shadow @Final private FogRenderer fogRenderer;
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private GameRenderState gameRenderState;

    @WrapOperation(method = "extractCamera",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/fog/FogRenderer;setupFog(Lnet/minecraft/client/Camera;ILnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lnet/minecraft/client/renderer/fog/FogData;"))
    private FogData tweakeroo_yeetWorldFog1(FogRenderer instance, Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, Operation<FogData> original)
    {
        if (Configs.Disable.DISABLE_ALL_TERRAIN_FOG.getBooleanValue())
        {
            final FogData fog = new FogData();
            int distAdj = 16;

            if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
            {
                final int viewDistance = this.minecraft.options.getEffectiveRenderDistance();
                final float blocksDistance = MathUtils.max(512.0F, this.gameRenderState.optionsRenderState.renderDistance);

                // 42 is the answer :)
                distAdj = (int) (blocksDistance / viewDistance);
            }

            // Fetch the Real Fog Color
            fog.color = new Vector4f(0.0F);
            ((IMixinFogRenderer) this.fogRenderer).tweakeroo_computeFogColor(camera, deltaTracker.getGameTimeDeltaPartialTick(false),
                                                                             level, renderDistanceInChunks * distAdj,
                                                                             darkenWorldAmount, fog.color);
            fog.environmentalStart = Float.MAX_VALUE - 4.0F;
            fog.environmentalEnd = Float.MAX_VALUE;
            fog.renderDistanceStart = Float.MAX_VALUE - 4.0F;
            fog.renderDistanceEnd = Float.MAX_VALUE;
//            fog.skyEnd = Float.MAX_VALUE;
//            fog.cloudEnd = Float.MAX_VALUE;

            return fog;
        }

        return original.call(instance, camera, renderDistanceInChunks, deltaTracker, darkenWorldAmount, level);
    }

    @WrapOperation(method = "renderLevel",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/fog/FogRenderer;getBuffer(Lnet/minecraft/client/renderer/fog/FogRenderer$FogMode;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;",
                            ordinal = 0))
    private GpuBufferSlice tweakeroo_yeetWorldFog2(FogRenderer instance, FogRenderer.FogMode mode,
                                                  Operation<GpuBufferSlice> original)
    {
        if (Configs.Disable.DISABLE_ALL_TERRAIN_FOG.getBooleanValue())
        {
            return instance.getBuffer(FogRenderer.FogMode.NONE);
        }

        return original.call(instance, mode);
    }
}
