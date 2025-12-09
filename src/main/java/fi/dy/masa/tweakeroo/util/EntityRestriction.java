package fi.dy.masa.tweakeroo.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class EntityRestriction extends UsageRestriction<EntityType<?>>
{
    @Override
    protected void setValuesForList(Set<EntityType<?>> set, List<String> names)
    {
        for (String name : names)
        {
            try
            {
				Identifier id = Identifier.tryParse(name);

				if (id != null)
				{
					Optional<Holder.Reference<EntityType<?>>> opt = BuiltInRegistries.ENTITY_TYPE.get(id);

					if (opt.isPresent())
					{
						set.add(opt.get().value());
						continue;
					}
				}
            }
            catch (Exception ignore) {}

            Tweakeroo.LOGGER.warn("Invalid entity name in a black- or whitelist: '{}'", name);
        }
    }
}
