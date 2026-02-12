package fi.dy.masa.tweakeroo.compat.mixin;

import java.util.List;
import java.util.Set;
import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin;

import fi.dy.masa.tweakeroo.Tweakeroo;

public class TweakerooMixinConfigPlugin extends RestrictiveMixinConfigPlugin
{
	@Override
	protected void onRestrictionCheckFailed(String mixinClassName, String reason)
	{
		Tweakeroo.LOGGER.warn("Disabled mixin '{}' due to: '{}'", mixinClassName, reason);
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	@Override
	public List<String> getMixins()
	{
		return null;
	}
}
