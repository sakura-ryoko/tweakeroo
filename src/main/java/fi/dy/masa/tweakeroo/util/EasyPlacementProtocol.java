package fi.dy.masa.tweakeroo.util;

import javax.annotation.Nonnull;
import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;

public enum EasyPlacementProtocol implements IConfigOptionListEntry, StringRepresentable
{
    AUTO                ("auto",                  "tweakeroo.gui.label.easy_place_protocol.auto"),
    V3                  ("v3",                    "tweakeroo.gui.label.easy_place_protocol.v3"),
    V2                  ("v2",                    "tweakeroo.gui.label.easy_place_protocol.v2"),
    SLAB_ONLY           ("slabs_only",            "tweakeroo.gui.label.easy_place_protocol.slabs_only"),
    NONE                ("none",                  "tweakeroo.gui.label.easy_place_protocol.none");

    public static final StringRepresentable.EnumCodec<@NotNull EasyPlacementProtocol> CODEC = StringRepresentable.fromEnum(EasyPlacementProtocol::values);
    public static final StreamCodec<@NotNull ByteBuf, @NotNull EasyPlacementProtocol> PACKET_CODEC = ByteBufCodecs.STRING_UTF8.map(EasyPlacementProtocol::fromStringStatic, EasyPlacementProtocol::getSerializedName);
    public static final ImmutableList<@NotNull EasyPlacementProtocol> VALUES = ImmutableList.copyOf(values());

    private final String configString;
    private final String translationKey;

    EasyPlacementProtocol(String configString, String translationKey)
    {
        this.configString = configString;
        this.translationKey = translationKey;
    }

    @Override
    public String getStringValue()
    {
        return this.configString;
    }

    @Override
    public String getDisplayName()
    {
        return StringUtils.translate(this.translationKey);
    }

    @Override
    public @Nonnull String getSerializedName()
    {
        return this.configString;
    }

    @Override
    public IConfigOptionListEntry cycle(boolean forward)
    {
        int id = this.ordinal();

        if (forward)
        {
            if (++id >= values().length)
            {
                id = 0;
            }
        }
        else
        {
            if (--id < 0)
            {
                id = values().length - 1;
            }
        }

        return values()[id % values().length];
    }

    @Override
    public EasyPlacementProtocol fromString(String name)
    {
        return fromStringStatic(name);
    }

    public static EasyPlacementProtocol fromStringStatic(String name)
    {
        for (EasyPlacementProtocol val : VALUES)
        {
            if (val.configString.equalsIgnoreCase(name))
            {
                return val;
            }
        }

        return EasyPlacementProtocol.AUTO;
    }
}
