package com.zutubi.pulse.core.config;

import com.zutubi.prototype.ListOptionProvider;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An option provider for selecting a default resource version from an
 * existing resource's versions.
 */
public class ResourceVersionOptionProvider extends ListOptionProvider
{
    public List<String> getOptions(Object instance, String path, TypeProperty property)
    {
        List<String> versions = new LinkedList<String>();
//        versions.add("");
        if(instance != null && instance instanceof Resource)
        {
            versions.addAll(((Resource)instance).getVersions().keySet());
        }
        
        Collections.sort(versions, new Sort.StringComparator());
        return versions;
    }
}
