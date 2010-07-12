package com.zutubi.pulse.master.xwork.actions.ajax;

import com.opensymphony.xwork.ActionSupport;

import java.util.Collection;
import java.util.Map;

/**
 * An object that exposes an actions errors, messages and
 * fields to a JSON serialiser.
 *
 * @see com.zutubi.pulse.master.webwork.dispatcher.FlexJsonResult
 */
public class ActionResult
{
    private ActionSupport action;

    public ActionResult(ActionSupport action)
    {
        this.action = action;
    }

    public Collection getActionErrors()
    {
        return action.getActionErrors();
    }

    public Collection getActionMessages()
    {
        return action.getActionMessages();
    }

    public Map getFieldErrors()
    {
        return action.getFieldErrors();
    }
}
