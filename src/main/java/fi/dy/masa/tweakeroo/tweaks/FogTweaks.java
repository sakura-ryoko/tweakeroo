package fi.dy.masa.tweakeroo.tweaks;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.biome.Biome;

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

    public boolean shouldBlockDimensionOrBossFog()
    {
        return Configs.Disable.DISABLE_SKY_DARKNESS.getBooleanValue();
    }

    public boolean shouldBlockAtmosphericFog()
    {
        return Configs.Disable.DISABLE_RENDER_DISTANCE_FOG.getBooleanValue();
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

    public void tweakLavaFog(FogData data, Entity cameraEntity, float viewDistance, RenderTickCounter tickCounter)
    {
        if (FeatureToggle.TWEAK_LAVA_VISIBILITY.getBooleanValue())
        {
            if (data.environmentalStart == 0.25f)
            {
                data.environmentalStart = 0.0f;
            }

            final float adjusted = RenderUtils.getLavaFogDistance(cameraEntity, data.environmentalEnd);

            if (data.environmentalEnd != adjusted)
            {
                data.environmentalEnd = adjusted;
            }
        }
    }

    public void tweakAtmosphericFog(FogData data, Entity cameraEntity, float viewDistance, RenderTickCounter tickCounter)
    {
    }

    public float tweakAtmosphericRainFog(ClientWorld instance, float v)
    {
        return v;
    }

    public boolean tweakAtmosphericBiomePrecipitation(Biome biome)
    {
        return biome.hasPrecipitation();
    }
}
