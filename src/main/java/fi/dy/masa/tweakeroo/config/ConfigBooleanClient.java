package fi.dy.masa.tweakeroo.config;

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.StringUtils;

import java.util.Objects;

public class ConfigBooleanClient extends ConfigBooleanHotkeyed
{
    public ConfigBooleanClient(String name, boolean defaultValue, String defaultHotkey)
    {
        this(name, defaultValue, defaultHotkey, name+" Comment!", StringUtils.splitCamelCase(name), name);
    }

    public ConfigBooleanClient(String name, boolean defaultValue, String defaultHotkey, String comment)
    {
        this(name, defaultValue, defaultHotkey, comment, StringUtils.splitCamelCase(name), name);
    }

    public ConfigBooleanClient(String name, boolean defaultValue, String defaultHotkey, String comment, String prettyName, String translatedName)
    {
        super(name, defaultValue, defaultHotkey, comment, prettyName, translatedName);
    }

    @Override
    public String getComment()
    {
//        String comment = super.getComment();
        String comment = StringUtils.getTranslatedOrFallback(Objects.requireNonNull(super.getComment()), super.getComment());
        if (comment == null)
        {
            return "";
        }

        return comment + "\n" + StringUtils.translate("tweakeroo.label.config_comment.single_player_only");
    }

    @Override
    public String getConfigGuiDisplayName()
    {
        return GuiBase.TXT_GOLD + super.getConfigGuiDisplayName() + GuiBase.TXT_RST;
    }
}
