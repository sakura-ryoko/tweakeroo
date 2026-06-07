package fi.dy.masa.tweakeroo.mixin.world;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.tweakeroo.config.Configs;

@Mixin(value = ClientLevel.class, priority = 1005)
public abstract class MixinClientLevel_disableWeatherEffects extends Level
{
	protected MixinClientLevel_disableWeatherEffects(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates)
	{
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@Inject(method = "tickWeatherEffects", at = @At("HEAD"), cancellable = true)
	private void tweakeroo_cancelParticlesAndSounds(CallbackInfo ci)
	{
		if (Configs.Disable.DISABLE_RAIN_EFFECTS.getBooleanValue())
		{
			ci.cancel();
		}
	}
}
