package fi.dy.masa.tweakeroo.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
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
                Optional<RegistryEntry.Reference<EntityType<?>>> opt = Registries.ENTITY_TYPE.getEntry(Identifier.tryParse(name));

                if (opt.isPresent())
                {
                    set.add(opt.get().value());
                    continue;
                }
            }
            catch (Exception ignore) {}

            Tweakeroo.logger.warn("Invalid entity name in a black- or whitelist: '{}'", name);
        }
    }
}
