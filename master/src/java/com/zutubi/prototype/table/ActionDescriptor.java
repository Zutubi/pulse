package com.zutubi.prototype.table;

import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ActionDescriptor
{
    private static final Logger LOG = Logger.getLogger(ActionDescriptor.class);

    private ActionManager actionManager;

    public ActionDescriptor(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public List<String> getActions(Object instance)
    {
        List<String> actions = new LinkedList<String>();
        try
        {
            actions.addAll(actionManager.getActions((Configuration) instance, true));
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }

        return actions;
    }
}
