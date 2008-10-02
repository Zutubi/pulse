package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.web.DefaultAction;
import com.zutubi.tove.ListOptionProvider;
import com.zutubi.tove.type.TypeProperty;

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
        options.add(DefaultAction.WELCOME_ACTION);
        options.add(DefaultAction.DASHBOARD_ACTION);
        options.add(DefaultAction.BROWSE_ACTION);
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
