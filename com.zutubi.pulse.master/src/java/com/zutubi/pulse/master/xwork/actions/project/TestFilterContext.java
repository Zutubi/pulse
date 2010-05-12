package com.zutubi.pulse.master.xwork.actions.project;

import com.opensymphony.xwork.ActionContext;
import com.zutubi.util.StringUtils;

/**
 * Manages storage and retrieval of the filtering state for test result pages.
 * The user's session is used to store their preference per-build.
 */
public class TestFilterContext
{
    public static final String FILTER_TESTS_KEY_PREFIX = "pulse.filterTests.";

    private static String getSessionKey(long buildId)
    {
        return FILTER_TESTS_KEY_PREFIX + buildId;
    }

    /**
     * Returns true if successful tests should be filtered out for the given
     * build.
     * 
     * @param buildId id of the build to check
     * @return true if test filtering is enabled for the given build
     */
    public static boolean isFilterEnabledForBuild(long buildId)
    {
        return StringUtils.stringSet((String) ActionContext.getContext().getSession().get(getSessionKey(buildId)));
    }

    /**
     * Sets the test filter for the given build.
     * 
     * @param buildId id of the build to update
     * @param filter  new value for the filter
     */
    @SuppressWarnings({"unchecked"})
    public static void setFilterForBuild(long buildId, String filter)
    {
        ActionContext.getContext().getSession().put(getSessionKey(buildId), filter);
    }
}
