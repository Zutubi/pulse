package com.zutubi.pulse.core.config;

import com.zutubi.tove.MapOption;
import com.zutubi.tove.MapOptionProvider;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.*;

/**
 * An option provider for selecting a default resource version from an
 * existing resource's versions.
 */
public class ResourceVersionOptionProvider extends MapOptionProvider
{
    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        // There is always an 'empty' option.
        return null;
    }

    public Map<String, String> getMap(Object instance, String parentPath, TypeProperty property)
    {
        List<String> resourceVersions = new LinkedList<String>();
        if(instance != null)
        {
            resourceVersions.addAll(((Resource) instance).getVersions().keySet());
        }
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
