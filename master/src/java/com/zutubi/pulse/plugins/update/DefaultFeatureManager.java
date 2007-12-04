package com.zutubi.pulse.plugins.update;

import com.zutubi.pulse.plugins.Plugin;
import com.zutubi.pulse.plugins.PluginException;
import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.plugins.Version;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class DefaultFeatureManager implements FeatureManager
{
    private static final Logger LOG = Logger.getLogger(DefaultFeatureManager.class);
    
    private List<UpdateSite> sites;
    private List<Feature> features = new LinkedList<Feature>();
    private PluginManager pluginManager;

    public List<Feature> getAllFeatures()
    {
        return features;
    }

    public Feature getFeature(final String id)
    {
        return CollectionUtils.find(features, new Predicate<Feature>()
        {
            public boolean satisfied(Feature feature)
            {
                return feature.getManifest().getId().equals(id);
            }
        });
    }

    public UpdateSite getSite(final URL url)
    {
        return CollectionUtils.find(sites, new Predicate<UpdateSite>()
        {
            public boolean satisfied(UpdateSite updateSite)
            {
                return updateSite.getUrl().equals(url);
            }
        });
    }

    public Feature installFeature(Site site, String id, String version) throws PluginException
    {
        // Is this feature installed?  If so, we need to update?
        // TODO

        
        FeatureReference reference = site.getFeatureReference(id, version);
        if(reference == null)
        {
            throw new PluginException("Feature '" + id + "' version '" + version + "' does not exist on site.");
        }

        // Parse the manifest
        FeatureManifest manifest;
        URL url = reference.getUrl();
        FeatureParser parser = new FeatureParser();
        try
        {
            manifest = parser.parse(url, url.openStream());
        }
        catch (Exception e)
        {
            throw new PluginException("Unable to parse feature manifest: " + e.getMessage(), e);
        }

        // Are the requirements installed? If not, abort.
        for(FeatureRequirement requirement: manifest.getRequirements())
        {
            if(!requirementMet(requirement))
            {
                throw new PluginException("Requirement '" + requirement.toString() + "' not met.");
            }
        }
        
        // Install each included feature
        for(FeatureInclusion inclusion: manifest.getInclusions())
        {
            Feature alreadyInstalled = getFeature(inclusion.getId());
            if(alreadyInstalled != null)
            {
                if(!alreadyInstalled.getManifest().getVersion().equals(inclusion.getVersion()))
                {
                    // Need to update this feature
                    // TODO
                }
            }
        }

        // Install each included plugin

        // How do we provide feedback?  First, want to compute what needs to
        // be done for communication with the user.
        return null;
    }

    private boolean requirementMet(FeatureRequirement requirement)
    {
        if(requirement.isFeature())
        {
            Feature f = getFeature(requirement.getId());
            return f != null && requirement.satisfied(f.getManifest().getVersion());
        }
        else
        {
            Plugin p = pluginManager.getPlugin(requirement.getId());
            Version v = p.getVersion();
            try
            {
                return p != null && requirement.satisfied(v);
            }
            catch(IllegalArgumentException e)
            {
                LOG.warning("Unable to parse version '" + v.toString() + "' for plugin '" + p.getName() + "': " + e.getMessage());
                return false;
            }
        }
    }

    public void uninstallFeature(Feature feature)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
