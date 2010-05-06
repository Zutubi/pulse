package com.zutubi.pulse.master.xwork.actions.ajax;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.StringUtils;

/**
 * Simple ajax action to store a user's test filter preference for a build in
 * their session.
 */
public class SetTestFilterAction extends ActionSupport
{
    public static final String FILTER_TESTS_KEY_PREFIX = "pulse.filterTests.";

    private long buildId;
    private String filter;

    private static String getSessionKey(long buildId)
    {
        return FILTER_TESTS_KEY_PREFIX + buildId;
    }

    public static boolean filterTestsForBuild(long buildId)
    {
        return StringUtils.stringSet((String) ActionContext.getContext().getSession().get(getSessionKey(buildId)));
    }

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
        ActionContext.getContext().getSession().put(getSessionKey(buildId), filter);
        return SUCCESS;
    }
}