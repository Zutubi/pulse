package com.zutubi.pulse.prototype.config.project;

import com.zutubi.prototype.ListOptionProvider;
import com.zutubi.prototype.security.AccessManager;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides options for the available project authorities, which includes
 * built in authorities like view and write as well as authorities mapped to
 * actions.
 */
public class ProjectAuthorityProvider extends ListOptionProvider
{
    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        List<String> options = new LinkedList<String>();
        options.addAll(Arrays.asList(AccessManager.ACTION_ADMINISTER, AccessManager.ACTION_VIEW, AccessManager.ACTION_WRITE, ProjectConfigurationActions.ACTION_PAUSE, ProjectConfigurationActions.ACTION_TRIGGER, "cancel build", "view source"));
        Collections.sort(options, new Sort.StringComparator());
        return options;
    }
}
