package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.project.TestFilterContext;

/**
 * Simple ajax action to store a user's test filter preference for a build in
 * their session.
 */
public class SetTestFilterAction extends ActionSupport
{
    private long buildId;
    private String filter;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public String execute()
    {
        TestFilterContext.setFilterForBuild(buildId, filter);
        return SUCCESS;
    }
}