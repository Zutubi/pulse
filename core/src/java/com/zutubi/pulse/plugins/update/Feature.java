package com.zutubi.pulse.plugins.update;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * A feature is a grouping of plugins and other features.  Features are used
 * to organise plugins into logical units for installation.
 *
 * Currently only the default "packaged" type of feature is supported.
 */
public class Feature
{
    private String id;
    private String version;
    private String label;
    private String providerName;
    private URL image;
    private String description;
    private String copyright;
    private String license;
    private UpdateSiteReference updateSite;
    private List<DiscoverySiteReference> discoverySites = new LinkedList<DiscoverySiteReference>();
    private List<FeatureInclusion> inclusions = new LinkedList<FeatureInclusion>();
    private List<PluginReference> plugins = new LinkedList<PluginReference>();
    private List<FeatureRequirement> requirements = new LinkedList<FeatureRequirement>();


    public Feature(String id, String version, String label, String description, String providerName, URL image)
    {
        this.id = id;
        this.version = version;
        this.label = label;
        this.description = description;
        this.providerName = providerName;
        this.image = image;
    }

    public String getId()
    {
        return id;
    }

    public String getVersion()
    {
        return version;
    }

    public String getLabel()
    {
        return label;
    }

    public String getProviderName()
    {
        return providerName;
    }

    public URL getImage()
    {
        return image;
    }

    public String getDescription()
    {
        return description;
    }

    public String getCopyright()
    {
        return copyright;
    }

    public void setCopyright(String copyright)
    {
        this.copyright = copyright;
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public UpdateSiteReference getUpdateSite()
    {
        return updateSite;
    }

    public void setUpdateSite(UpdateSiteReference updateSite)
    {
        this.updateSite = updateSite;
    }

    public List<DiscoverySiteReference> getDiscoverySites()
    {
        return discoverySites;
    }

    public void addDiscoverySite(DiscoverySiteReference site)
    {
        discoverySites.add(site);
    }

    public List<FeatureInclusion> getInclusions()
    {
        return inclusions;
    }

    public void addInclusion(FeatureInclusion inclusion)
    {
        inclusions.add(inclusion);
    }

    public List<PluginReference> getPlugins()
    {
        return plugins;
    }

    public void addPlugin(PluginReference plugin)
    {
        plugins.add(plugin);
    }

    public List<FeatureRequirement> getRequirements()
    {
        return requirements;
    }

    public void addRequirement(FeatureRequirement requirement)
    {
        requirements.add(requirement);
    }
}
