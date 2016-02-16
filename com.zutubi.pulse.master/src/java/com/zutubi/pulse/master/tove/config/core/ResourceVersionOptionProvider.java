package com.zutubi.pulse.master.tove.config.core;

import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.handler.MapOptionProvider;
import com.zutubi.util.Sort;

import java.util.*;

/**
 * An option provider for selecting a default resource version from an
 * existing resource's versions.
 */
public class ResourceVersionOptionProvider extends MapOptionProvider
{
    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        // There is always an 'empty' option.
        return null;
    }

    public Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        List<String> resourceVersions = new ArrayList<>();
        if (context.getExistingInstance() != null)
        {
            resourceVersions.addAll(((ResourceConfiguration) context.getExistingInstance()).getVersions().keySet());
        }
        Collections.sort(resourceVersions, new Sort.StringComparator());

        Map<String, String> versions = new HashMap<>(resourceVersions.size() + 1);
        versions.put("", "[none]");
        for(String version: resourceVersions)
        {
            versions.put(version, version);
        }
        
        return versions;
    }
}
