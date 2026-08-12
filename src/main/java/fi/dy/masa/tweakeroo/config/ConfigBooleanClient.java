package fi.dy.masa.tweakeroo.config;

import java.util.Objects;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.StringUtils;

public class ConfigBooleanClient extends ConfigBooleanHotkeyed
{
    private final boolean withServux;

    public ConfigBooleanClient(String name, boolean defaultValue, String defaultHotkey, boolean withServux)
    {
        this(name, defaultValue, defaultHotkey, withServux, "", StringUtils.splitCamelCase(name), name);
    }

    public ConfigBooleanClient(String name, boolean defaultValue, String defaultHotkey, boolean withServux, String comment)
    {
        this(name, defaultValue, defaultHotkey, withServux, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigBooleanClient(String name, boolean defaultValue, String defaultHotkey, boolean withServux, String comment, String prettyName, String translatedName)
    {
        super(name, defaultValue, defaultHotkey, comment, prettyName, translatedName);
        this.withServux = withServux;
    }

    @Override
    public String getComment()
    {
        if (this.comment == null || this.comment.isEmpty())
        {
            return this.getSinglePlayerOrServux();
        }

        String comment = StringUtils.getTranslatedOrFallback(Objects.requireNonNull(super.getComment()), super.getComment());

        if (comment == null || comment.isEmpty())
        {
            return this.getSinglePlayerOrServux();
        }

        return comment + "\n" + this.getSinglePlayerOrServux();
    }

    private String getSinglePlayerOrServux()
    {
        return this.withServux
               ? StringUtils.translate("tweakeroo.label.config_comment.single_player_or_servux")
               : StringUtils.translate("tweakeroo.label.config_comment.single_player_only");
    }

    @Override
    public String getConfigGuiDisplayName()
    {
        return GuiBase.TXT_GOLD + super.getConfigGuiDisplayName() + GuiBase.TXT_RST;
    }

    @Override
    public ConfigBooleanClient apply(String key)
    {
        return (ConfigBooleanClient) super.apply(key);
    }
}
