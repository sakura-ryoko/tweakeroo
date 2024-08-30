package fi.dy.masa.tweakeroo.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import fi.dy.masa.malilib.util.restrictions.UsageRestriction;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class PotionRestriction extends UsageRestriction<StatusEffect>
{
    @Override
    protected void setValuesForList(Set<StatusEffect> set, List<String> names)
    {
        for (String name : names)
        {
            Identifier rl = null;

            try
            {
                rl = Identifier.tryParse(name);
            }
            catch (Exception ignored) { }

            //StatusEffect effect = rl != null ? Registries.STATUS_EFFECT.get(rl) : null;
            Optional<RegistryEntry.Reference<StatusEffect>> opt = Registries.STATUS_EFFECT.getEntry(rl);

            if (opt.isPresent())
            {
                set.add(opt.get().value());
            }
            else
            {
                Tweakeroo.logger.warn("Invalid potion effect name '{}'", name);
            }
        }
    }
}
