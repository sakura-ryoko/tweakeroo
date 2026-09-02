package fi.dy.masa.tweakeroo;

import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.util.StringUtils;

public class Reference
{
    public static final String MOD_ID = "tweakeroo";
    public static final String MOD_NAME = "Tweakeroo";
    public static final String MOD_VERSION = StringUtils.getModVersionString(MOD_ID);
    public static final String MOD_TYPE = "fabric";
    public static final String MOD_STRING = MOD_ID+"-"+MOD_TYPE+"-"+MaLiLibReference.MC_VERSION+"-"+MOD_VERSION;
}
