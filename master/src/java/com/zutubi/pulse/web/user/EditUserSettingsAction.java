package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.DefaultAction;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class EditUserSettingsAction extends UserActionSupport
{
    private List<String> defaultActions;
    private String defaultAction;
    private boolean refreshEnabled = false;
    private int refreshInterval;
    private int tailLines;
    private int tailInterval;

    public List<String> getDefaultActions()
    {
        if (defaultActions == null)
        {
            defaultActions = new LinkedList<String>();
            defaultActions.add(DefaultAction.DASHBOARD_ACTION);
            defaultActions.add(DefaultAction.WELCOME_ACTION);
        }

        return defaultActions;
    }

    public String getDefaultAction()
    {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction)
    {
        this.defaultAction = defaultAction;
    }

    public boolean getRefreshEnabled()
    {
        return refreshEnabled;
    }

    public void setRefreshEnabled(boolean refreshEnabled)
    {
        this.refreshEnabled = refreshEnabled;
    }

    public int getRefreshInterval()
    {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval)
    {
        this.refreshInterval = refreshInterval;
    }

    public int getTailLines()
    {
        return tailLines;
    }

    public void setTailLines(int tailLines)
    {
        this.tailLines = tailLines;
    }

    public int getTailInterval()
    {
        return tailInterval;
    }

    public void setTailInterval(int tailInterval)
    {
        this.tailInterval = tailInterval;
    }

    public String doInput() throws Exception
    {
        // load user details.
        User user = getUser();
        refreshInterval = user.getRefreshInterval();
        refreshEnabled = refreshInterval != User.REFRESH_DISABLED;
        if (!refreshEnabled)
        {
            refreshInterval = 60;
        }

        tailLines = user.getTailLines();
        tailInterval = user.getTailRefreshInterval();
        defaultAction = user.getDefaultAction();

        return super.doInput();
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        if (refreshEnabled && refreshInterval <= 0)
        {
            addFieldError("refreshInterval", "refresh interval must be a positive number");
        }
    }

    public String execute() throws Exception
    {
        // load user details.
        User persistentUser = super.getUser();
        if (refreshEnabled)
        {
            persistentUser.setRefreshInterval(refreshInterval);
        }
        else
        {
            persistentUser.setRefreshInterval(User.REFRESH_DISABLED);
        }

        persistentUser.setTailLines(tailLines);
        persistentUser.setTailRefreshInterval(tailInterval);
        persistentUser.setDefaultAction(defaultAction);
        getUserManager().save(persistentUser);
        return SUCCESS;
    }
}
