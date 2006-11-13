package com.zutubi.plugins;

/**
 * <class-comment/>
 */
public class PluginInformation
{
    private String description;

    private String vendorName;
    private String vendorUrl;

    private String pluginVersion;

    private String minSupportedAppVersion;
    private String maxSupportedAppVersion;

    private String minJdkVersion;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getVendorName()
    {
        return vendorName;
    }

    public void setVendorName(String vendorName)
    {
        this.vendorName = vendorName;
    }

    public String getVendorUrl()
    {
        return vendorUrl;
    }

    public void setVendorURL(String vendorUrl)
    {
        this.vendorUrl = vendorUrl;
    }

    public String getPluginVersion()
    {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion)
    {
        this.pluginVersion = pluginVersion;
    }

    public String getMinSupportedAppVersion()
    {
        return minSupportedAppVersion;
    }

    public void setMinSupportedAppVersion(String minSupportedAppVersion)
    {
        this.minSupportedAppVersion = minSupportedAppVersion;
    }

    public String getMaxSupportedAppVersion()
    {
        return maxSupportedAppVersion;
    }

    public void setMaxSupportedAppVersion(String maxSupportedAppVersion)
    {
        this.maxSupportedAppVersion = maxSupportedAppVersion;
    }

    public String getMinJdkVersion()
    {
        return minJdkVersion;
    }

    public void setMinJdkVersion(String minJdkVersion)
    {
        this.minJdkVersion = minJdkVersion;
    }
}
