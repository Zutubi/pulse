package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.prototype.config.LabelConfiguration;
import static com.zutubi.util.CollectionUtils.asPair;
import static com.zutubi.util.CollectionUtils.asVector;
import com.zutubi.util.Pair;
import com.zutubi.util.RandomUtils;

import java.util.Hashtable;

/**
 * Acceptance tests for the users dashboard view.
 */
public class DashboardAcceptanceTest extends SeleniumTestBase
{
    private static final String SHOW_ALL_GROUPS   = "showAllGroups";
    private static final String SHOWN_GROUPS      = "shownGroups";
    private static final String SHOW_ALL_PROJECTS = "showAllProjects";
    private static final String SHOWN_PROJECTS    = "shownProjects";

    private String user;
    private String userPath;

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
        setDashboard(asPair(SHOW_ALL_GROUPS, true), asPair(SHOW_ALL_PROJECTS, true));

        String group = random + "-group";
        String project = random + "-project";

        String projectPath = xmlRpcHelper.insertSimpleProject(project, false);
        addLabel(projectPath, group);

        DashboardPage dashboard = new DashboardPage(selenium, urls);
        dashboard.goTo();
        assertTrue(dashboard.isGroupPresent(group));
        assertTrue(dashboard.isGroupedProjectPresent(group, project));
        assertFalse(dashboard.isUngroupedProjectPresent(project));
    }

    public void testShowSpecificGroups() throws Exception
    {
        String group1 = random + "-group1";
        String group2 = random + "-group2";
        String project = random + "-project";

        setDashboard(asPair(SHOW_ALL_GROUPS, false), asPair(SHOWN_GROUPS, asVector(group1)), asPair(SHOW_ALL_PROJECTS, true));

        String projectPath = xmlRpcHelper.insertSimpleProject(project, false);
        addLabel(projectPath, group1);
        addLabel(projectPath, group2);

        DashboardPage dashboard = new DashboardPage(selenium, urls);
        dashboard.goTo();
        assertTrue(dashboard.isGroupPresent(group1));
        assertTrue(dashboard.isGroupedProjectPresent(group1, project));
        assertFalse(dashboard.isGroupPresent(group2));
        assertFalse(dashboard.isGroupedProjectPresent(group2, project));
        assertFalse(dashboard.isUngroupedProjectPresent(project));
    }

    public void testHideGroup() throws Exception
    {
        String group1 = random + "-group1";
        String group2 = random + "-group2";
        String project = random + "-project";

        setDashboard(asPair(SHOW_ALL_GROUPS, true), asPair(SHOW_ALL_PROJECTS, true));

        String projectPath = xmlRpcHelper.insertSimpleProject(project, false);
        addLabel(projectPath, group1);
        addLabel(projectPath, group2);

        DashboardPage dashboard = new DashboardPage(selenium, urls);
        dashboard.goTo();
        assertTrue(dashboard.isGroupPresent(group1));
        assertTrue(dashboard.isGroupPresent(group2));
        assertFalse(dashboard.isUngroupedProjectPresent(project));

        dashboard.hideGroupAndWait(group1);

        assertFalse(dashboard.isGroupPresent(group1));
        assertTrue(dashboard.isGroupPresent(group2));
        assertFalse(dashboard.isUngroupedProjectPresent(project));
    }

    public void testShowSpecificProjects() throws Exception
    {
        String project1 = random + "-project1";
        String project2 = random + "-project2";

        String project1Path = xmlRpcHelper.insertSimpleProject(project1, false);
        xmlRpcHelper.insertSimpleProject(project2, false);

        setDashboard(asPair(SHOW_ALL_GROUPS, true), asPair(SHOW_ALL_PROJECTS, false), asPair(SHOWN_PROJECTS, asVector(project1Path)));

        DashboardPage dashboard = new DashboardPage(selenium, urls);
        dashboard.goTo();
        assertTrue(dashboard.isUngroupedProjectPresent(project1));
        assertFalse(dashboard.isUngroupedProjectPresent(project2));
    }

    public void testHideProject() throws Exception
    {
        String project1 = random + "-project1";
        String project2 = random + "-project2";

        setDashboard(asPair(SHOW_ALL_GROUPS, true), asPair(SHOW_ALL_PROJECTS, true));

        xmlRpcHelper.insertSimpleProject(project1, false);
        xmlRpcHelper.insertSimpleProject(project2, false);

        DashboardPage dashboard = new DashboardPage(selenium, urls);
        dashboard.goTo();
        assertTrue(dashboard.isUngroupedProjectPresent(project1));
        assertTrue(dashboard.isUngroupedProjectPresent(project2));

        dashboard.hideProjectAndWait(project1);

        assertFalse(dashboard.isUngroupedProjectPresent(project1));
        assertTrue(dashboard.isUngroupedProjectPresent(project2));
    }

    private void addLabel(String projectPath, String label) throws Exception
    {
        Hashtable labelConfig = xmlRpcHelper.createDefaultConfig(LabelConfiguration.class);
        labelConfig.put("label", label);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "labels"), labelConfig);
    }

    private void setDashboard(Pair<String, ?>... values) throws Exception
    {
        String dashboardPath = PathUtils.getPath(userPath, "preferences", "dashboard");
        Hashtable dashboardConfig = xmlRpcHelper.getConfig(dashboardPath);
        for (Pair<String, ?> pair : values)
        {
            if (!pair.second.equals(dashboardConfig.get(pair.first)))
            {
                dashboardConfig.put(pair.first, pair.second);
                xmlRpcHelper.saveConfig(dashboardPath, dashboardConfig, false);
            }
        }
    }
}
