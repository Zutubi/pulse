package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.tove.type.record.PathUtils;
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

    private String userPath;

    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        String user = RandomUtils.randomString(10);
        userPath = xmlRpcHelper.insertTrivialUser(user);
        login(user, "");
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testNoTrailingSlash() throws Exception
    {
        // See CIB-1715.
        goTo("dashboard");
        selenium.waitForPageToLoad("3000");
        DashboardPage page = new DashboardPage(selenium, urls);
        assertTrue(page.isPresent());
        assertTitle(page);
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
        assertTrue(dashboard.isProjectPresent(group, project));
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
        assertTrue(dashboard.isProjectPresent(group1, project));
        assertFalse(dashboard.isGroupPresent(group2));
        assertFalse(dashboard.isProjectPresent(group2, project));
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
        // Check the upngrouped projects cannot be hidden (CIB-1963).
        assertFalse(dashboard.isGroupActionPresent(null, ProjectsSummaryPage.ACTION_HIDE));

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

        dashboard.hideProjectAndWait(null, project1);

        assertFalse(dashboard.isUngroupedProjectPresent(project1));
        assertTrue(dashboard.isUngroupedProjectPresent(project2));
    }

    private void addLabel(String projectPath, String label) throws Exception
    {
        Hashtable<String, Object> labelConfig = xmlRpcHelper.createDefaultConfig(LabelConfiguration.class);
        labelConfig.put("label", label);
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "labels"), labelConfig);
    }

    private void setDashboard(Pair<String, ?>... values) throws Exception
    {
        String dashboardPath = PathUtils.getPath(userPath, "preferences", "dashboard");
        Hashtable<String, Object> dashboardConfig = xmlRpcHelper.getConfig(dashboardPath);
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
