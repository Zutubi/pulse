package com.zutubi.prototype.table;

import com.zutubi.prototype.actions.ConfigurationActions;
import com.zutubi.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class ActionDescriptor
{
    private static final Logger LOG = Logger.getLogger(ActionDescriptor.class);

    private List<String> defaultActions = new LinkedList<String>();
    private ConfigurationActions configurationActions;

    public ActionDescriptor()
    {
    }

    public void setConfigurationActions(ConfigurationActions configurationActions)
    {
        this.configurationActions = configurationActions;
    }

    public void addDefaultAction(String action)
    {
        defaultActions.add(action);
    }

    public List<String> getActions(Object instance)
    {
        List<String> actions = new LinkedList<String>();
        actions.addAll(defaultActions);
        try
        {
            actions.addAll(configurationActions.getActions(instance));
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
        return actions;
    }
}
