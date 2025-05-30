package fi.dy.masa.tweakeroo.tweaks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.renderer.RenderUtils;

public class FogTweaks
{
    public static final FogTweaks INSTANCE = new FogTweaks();

    public boolean shouldBlockWaterFog()
    {
        return FeatureToggle.TWEAK_FREE_CAMERA.getBooleanValue();
    }

    public boolean shouldBlockLavaFog()
    {
        return false;
    }

    public boolean shouldBlockPoweredSnowFog()
    {
        return false;
    }

    public boolean shouldBlockBlindnessFog()
    {
        return false;
    }

    public boolean shouldBlockDarknessFog()
    {
        return false;
    }

    private void dumpFogData(FogData data, String type)
    {
        System.out.printf("DUMP FOG DATA (Type: %s) -->\n", type);
        System.out.printf("RenderStart: [%.5f], End: [%.5f]\n", data.renderDistanceStart, data.renderDistanceEnd);
        System.out.printf("EnviroStart: [%.5f], End: [%.5f]\n", data.environmentalStart, data.environmentalEnd);
        System.out.printf("CloudEnd   : [%.5f], Sky: [%.5f]\n", data.cloudEnd, data.skyEnd);
        System.out.print ("END\n");
    }

    public void tweakDarknessFog(FogData data, Entity cameraEntity, float viewDistance, RenderTickCounter tickCounter)
    {
        if (FeatureToggle.TWEAK_DARKNESS_VISIBILITY.getBooleanValue())
        {
            // Trying not to make this too overpowered.
            // To me, no more than around 3.0f should suffice; and
            // I want to make this bound to some enchantment.
            // Could remove it by changing the adj value higher, but
            // that would be no fun; now would it?
            final float adj = data.skyEnd * 2.7F;

            data.environmentalStart = adj * 0.75F;
            data.environmentalEnd = adj;
            data.skyEnd = adj;
            data.cloudEnd = adj;
        }
    }

    public void tweakLavaFog(FogData data, Entity cameraEntity, float viewDistance, RenderTickCounter tickCounter)
    {
        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            if (data.environmentalStart == 0.25F)
            {
                data.environmentalStart = 0.0F;
            }

            final float adjusted = RenderUtils.calculateLiquidFogDistance(cameraEntity, data.environmentalEnd, false);

            if (data.environmentalEnd != adjusted)
            {
                data.environmentalEnd = adjusted;
            }
        }
    }

    public void tweakWaterFog(FogData data, Entity cameraEntity, float viewDistance, RenderTickCounter tickCounter)
    {
        if (FeatureToggle.TWEAK_WATER_VISIBILITY.getBooleanValue())
        {
            if (data.environmentalStart > 0.0F)
            {
                data.environmentalStart = -8.0F;
            }

            final float adjusted = RenderUtils.calculateLiquidFogDistance(cameraEntity, data.environmentalEnd, true);

            if (data.environmentalEnd != adjusted)
            {
                data.environmentalEnd = adjusted;
            }
        }
    }

    public void tweakAtmosphericFog(FogData data, Entity cameraEntity, float viewDistance, RenderTickCounter tickCounter, float fogMultiplier)
    {
        if (Configs.Disable.DISABLE_ATMOSPHERIC_FOG.getBooleanValue())
        {
            data.environmentalStart = -160.0F;
            data.environmentalEnd = data.cloudEnd;
        }
    }

    public float tweakSkyDarkness(ClientWorld.Properties instance)
    {
        return Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue() ? 1.0F : instance.getVoidDarknessRange();
    }

    public int tweakRenderDistanceFog_Distance(int multiplier)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            MinecraftClient mc = MinecraftClient.getInstance();

            final int viewDistance = mc.options.getClampedViewDistance();
            final float blocksDistance = Math.max(512.0F, mc.gameRenderer.getViewDistanceBlocks());

            // 42 is the answer :)
            return (int) (blocksDistance / viewDistance);
        }

        return multiplier;
    }

    public float tweakRenderDistanceFog_Clamp(float value, float min, float max)
    {
        if (Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue())
        {
            return min;
        }

        return MathHelper.clamp(value, min, max);
    }

    // todo - DISABLE_NETHER_FOG and DISABLE_BOSS_FOG has the same effect
//    public void tweakDimensionOrBossFog(FogData data, Entity cameraEntity, BlockPos cameraPos, ClientWorld world, float viewDistance, RenderTickCounter tickCounter)
//    {
//        if (Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue())
//        {
////            this.dumpFogData(data, "dim_boss");
//            data.environmentalStart = -160.0F;
//            data.environmentalEnd = 1024.0F;
//            data.skyEnd = 1024.0F;
//            data.cloudEnd = 1024.0F;
//        }
//    }
}
