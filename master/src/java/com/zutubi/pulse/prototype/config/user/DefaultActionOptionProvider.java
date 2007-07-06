package com.zutubi.pulse.prototype.config.user;

import com.zutubi.prototype.ListOptionProvider;
import com.zutubi.prototype.type.TypeProperty;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class DefaultActionOptionProvider extends ListOptionProvider
{
    private static final List<String> options = new LinkedList<String>();
    {
        options.add("welcome");
        options.add("dashboard");
        options.add("projects");
    }
    
    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        return options;
    }
}
