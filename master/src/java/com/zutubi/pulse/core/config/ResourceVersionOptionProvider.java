package com.zutubi.pulse.core.config;

import com.zutubi.prototype.MapOptionProvider;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.*;

/**
 * An option provider for selecting a default resource version from an
 * existing resource's versions.
 */
public class ResourceVersionOptionProvider extends MapOptionProvider
{
    public Map<String, String> getMap(Object instance, String path, TypeProperty property)
    {
        List<String> resourceVersions = new LinkedList<String>(((Resource) instance).getVersions().keySet());
        Collections.sort(resourceVersions, new Sort.StringComparator());

        Map<String, String> versions = new HashMap<String, String>(resourceVersions.size() + 1);
        versions.put("", "[none]");
        for(String version: resourceVersions)
        {
            versions.put(version, version);
        }
        
        return versions;
    }
}
