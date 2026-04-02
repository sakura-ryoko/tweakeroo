package fi.dy.masa.tweakeroo.config;

import java.util.Objects;

import fi.dy.masa.malilib.MaLiLibFabricData;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.StringUtils;

public class ConfigBooleanModConflict extends ConfigBooleanHotkeyed
{
    private final String modConflict;

    public ConfigBooleanModConflict(String name, boolean defaultValue, String defaultHotkey, String conflict)
    {
        this(name, defaultValue, defaultHotkey, conflict, name+" Comment!", StringUtils.splitCamelCase(name), name);
    }

    public ConfigBooleanModConflict(String name, boolean defaultValue, String defaultHotkey, String conflict, String comment)
    {
        this(name, defaultValue, defaultHotkey, conflict, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigBooleanModConflict(String name, boolean defaultValue, String defaultHotkey, String conflict, String comment, String prettyName, String translatedName)
    {
        super(name, defaultValue, defaultHotkey, comment, prettyName, translatedName);
        this.modConflict = conflict;
    }

    @Override
    public String getComment()
    {
        String comment = StringUtils.getTranslatedOrFallback(Objects.requireNonNull(super.getComment()), super.getComment());

        if (comment == null)
        {
            return "";
        }

        if (MaLiLibFabricData.ALL_MOD_VERSIONS.containsKey(this.modConflict))
        {
            return comment + "\n" + StringUtils.translate("tweakeroo.label.config_comment.without_mod_only", this.modConflict);
        }

        return comment;
    }

    @Override
    public String getConfigGuiDisplayName()
    {
        if (MaLiLibFabricData.ALL_MOD_VERSIONS.containsKey(this.modConflict))
        {
            return GuiBase.TXT_RED + super.getConfigGuiDisplayName() + GuiBase.TXT_RST;
        }

        return super.getConfigGuiDisplayName();
    }
}
