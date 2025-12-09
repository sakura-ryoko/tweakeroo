package fi.dy.masa.tweakeroo.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class PotionRestriction extends UsageRestriction<MobEffect>
{
    @Override
    protected void setValuesForList(Set<MobEffect> set, List<String> names)
    {
        for (String name : names)
        {
            Identifier rl = null;

            try
            {
                rl = Identifier.tryParse(name);
            }
            catch (Exception ignored) { }

			if (rl != null)
			{
				//StatusEffect effect = rl != null ? Registries.STATUS_EFFECT.get(rl) : null;
				Optional<Holder.Reference<MobEffect>> opt = BuiltInRegistries.MOB_EFFECT.get(rl);

				if (opt.isPresent())
				{
					set.add(opt.get().value());
				}
				else
				{
					Tweakeroo.LOGGER.warn("Invalid potion effect name '{}'", name);
				}
			}
        }
    }
}
