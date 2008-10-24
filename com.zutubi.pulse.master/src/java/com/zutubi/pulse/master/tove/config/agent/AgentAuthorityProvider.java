package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.tove.handler.ListOptionProvider;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides options for the available agent authorities, which includes
 * built in authorities like write as well as authorities mapped to
 * actions.
 */
public class AgentAuthorityProvider extends ListOptionProvider
{
    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public List<String> getOptions(Object instance, String parentPath, TypeProperty property)
    {
        List<String> options = new LinkedList<String>();
        options.addAll(Arrays.asList(AccessManager.ACTION_ADMINISTER, AccessManager.ACTION_VIEW, AccessManager.ACTION_WRITE, AgentConfigurationActions.ACTION_DISABLE, AgentConfigurationActions.ACTION_PING));
        Collections.sort(options, new Sort.StringComparator());
        return options;
    }
}
