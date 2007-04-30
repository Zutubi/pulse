package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.ListOptionProvider;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.util.Sort;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * An option provider for selecting a default resource version from an
 * existing resource's versions.
 */
public class ResourceVersionOptionProvider extends ListOptionProvider
{
    public List<String> getOptions(Object instance, String path, TypeProperty property)
    {
        List<String> versions = new LinkedList<String>();
        versions.add("");
        if(instance != null)
        {
            versions.addAll(((Resource)instance).getVersions().keySet());
        }
        
        Collections.sort(versions, new Sort.StringComparator());
        return versions;
    }
}
