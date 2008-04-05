package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.prototype.config.LabelConfiguration;
import com.zutubi.util.RandomUtils;

import java.util.Hashtable;

/**
 * Acceptance tests for the users dashboard view.
 */
public class DashboardAcceptanceTest extends SeleniumTestBase
{
    private String user;
    private String userPath;
    private static final String SHOW_ALL_GROUPS = "showAllGroups";
    private static final String SHOW_ALL_PROJECTS = "showAllProjects";

    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        user = RandomUtils.randomString(10);
        userPath = xmlRpcHelper.insertTrivialUser(user);
        login(user, "");
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testShowGroups() throws Exception
    {
        setShowAllGroups(true);
        setShowAllProjects(true);

        String group = random + "-group";
        String project = random + "-project";

        String projectPath = xmlRpcHelper.insertTrivialProject(project, false);
        Hashtable label = xmlRpcHelper.createDefaultConfig(LabelConfiguration.class);
        label.put("label", group);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "labels"), label);

        DashboardPage dashboard = new DashboardPage(selenium, urls);
        dashboard.goTo();
        assertTrue(dashboard.isGroupPresent(group));
        assertTrue(dashboard.isGroupedProjectPresent(group, project));
        assertFalse(dashboard.isUngroupedProjectPresent(project));
    }

    private void setShowAllGroups(Boolean showAll) throws Exception
    {
        setBoolean(SHOW_ALL_GROUPS, showAll);
    }

    private void setShowAllProjects(Boolean showAll) throws Exception
    {
        setBoolean(SHOW_ALL_PROJECTS, showAll);
    }

    private void setBoolean(String key, Boolean b) throws Exception
    {
        String dashboardPath = PathUtils.getPath(userPath, "preferences", "dashboard");
        Hashtable dashboardConfig = xmlRpcHelper.getConfig(dashboardPath);
        if(!b.equals(dashboardConfig.get(key)))
        {
            dashboardConfig.put(key, b);
            xmlRpcHelper.saveConfig(dashboardPath, dashboardConfig, false);
        }
    }
}
