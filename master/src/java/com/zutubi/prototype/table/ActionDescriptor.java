package com.zutubi.prototype.table;

import com.zutubi.prototype.actions.Actions;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class ActionDescriptor
{
    private List<String> defaultActions = new LinkedList<String>();

    private Class actionHandlerClass;
    private Actions actionSupport;

    public ActionDescriptor()
    {
    }

    public void addActionHandler(Class actionHandler, Actions actions)
    {
        this.actionHandlerClass = actionHandler;
        this.actionSupport = actions;
    }

    public void addDefaultAction(String action)
    {
        defaultActions.add(action);
    }

    public List<String> getActions(Object instance)
    {
        List<String> actions = new LinkedList<String>();
        actions.addAll(defaultActions);
        if (actionHandlerClass != null)
        {
            actions.addAll(actionSupport.getActions(actionHandlerClass, instance));
        }
        return actions;
    }
}
