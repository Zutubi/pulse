package com.zutubi.pulse.prototype.config.user;

import com.zutubi.prototype.ListOptionProvider;
import com.zutubi.prototype.type.TypeProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides the list of possible default pages: the page the user is taken to
 * when the login or click the "pulse" link.
 */
public class DefaultActionOptionProvider extends ListOptionProvider
{
    private static final List<String> options = new LinkedList<String>();

    static
    {
        options.add("welcome");
        options.add("dashboard");
        options.add("projects");
    }

    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        return options;
    }
}
