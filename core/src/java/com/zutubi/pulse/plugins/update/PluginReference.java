package com.zutubi.pulse.plugins.update;

/**
 * A reference from a feature to a plugin that is included in that feature.
 * @see Feature
 */
public class PluginReference
{
    private String id;
    private String version;
    /**
     * Approximate size of the plugin download, in KB.  Negative if not
     * known.
     */
    private int downloadSize = -1;
    /**
     * Approximate size of the plugin when installed, in KB.  Negative if not
     * known.
     */
    private int installSize = -1;
    private boolean unpack = true;

    public PluginReference(String id, String version, int downloadSize, int installSize, boolean unpack)
    {
        this.id = id;
        this.version = version;
        this.downloadSize = downloadSize;
        this.installSize = installSize;
        this.unpack = unpack;
    }

    public String getId()
    {
        return id;
    }

    public String getVersion()
    {
        return version;
    }

    public int getDownloadSize()
    {
        return downloadSize;
    }

    public int getInstallSize()
    {
        return installSize;
    }

    public boolean isUnpack()
    {
        return unpack;
    }
}
