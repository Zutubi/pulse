package com.zutubi.pulse.plugins.update;

import com.zutubi.pulse.plugins.PluginException;

import java.util.List;

/**
 */
public interface FeatureManager
{
    List<Feature> getAllFeatures();
    Feature getFeature(String id);
    Feature installFeature(Site site, String id, String version) throws PluginException;
    void uninstallFeature(Feature feature);
    
}
