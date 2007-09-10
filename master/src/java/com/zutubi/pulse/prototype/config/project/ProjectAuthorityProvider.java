package com.zutubi.pulse.prototype.config.project;

import com.zutubi.prototype.ListOptionProvider;
import com.zutubi.prototype.type.TypeProperty;

import java.util.Arrays;
import java.util.List;

/**
 * Provides options for the available project authorities, which includes
 * built in authorities like read and write as well as authorities mapped to
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
        // FIXME Need to add actions here
        return Arrays.asList("administer", "cancel build", "read", "view source", "write");
    }
}
