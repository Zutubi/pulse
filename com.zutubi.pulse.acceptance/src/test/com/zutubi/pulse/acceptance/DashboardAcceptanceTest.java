package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.acceptance.utils.BuildRunner;
import com.zutubi.pulse.acceptance.utils.WaitProject;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.util.CollectionUtils.asPair;
import static com.zutubi.util.CollectionUtils.asVector;
import static com.zutubi.util.Constants.SECOND;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * Acceptance tests for the users dashboard view.
 */
public class DashboardAcceptanceTest extends AcceptanceTestBase
{
    private static final String SHOW_ALL_GROUPS    = "showAllGroups";
    private static final String SHOWN_GROUPS       = "shownGroups";
    private static final String SHOW_ALL_PROJECTS  = "showAllProjects";
    private static final String SHOWN_PROJECTS     = "shownProjects";
    private static final String BUILDS_PER_PROJECT = "buildsPerProject";
    
    private String userPath;
    private BuildRunner buildRunner;
    
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
        String user = RandomUtils.randomString(10);
        userPath = rpcClient.RemoteApi.insertTrivialUser(user);
        assertTrue(getBrowser().login(user, ""));
        
        buildRunner = new BuildRunner(rpcClient.RemoteApi);
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testNoTrailingSlash() throws Exception
    {
        // See CIB-1715.
        getBrowser().open(urls.base() + "dashboard");
        getBrowser().waitForPageToLoad(3 * SECOND);
        DashboardPage page = getBrowser().createPage(DashboardPage.class);
        assertTrue(page.isPresent());
        assertEquals(getBrowser().getTitle(), page.getTitle());
    }

    public void testShowGroups() throws Exception
    {
        setDashboard(asPair(SHOW_ALL_GROUPS, true), asPair(SHOW_ALL_PROJECTS, true));

        String group = random + "-group";
        String project = random + "-project";

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(project, false);
        addLabel(projectPath, group);

        DashboardPage dashboard = getBrowser().openAndWaitFor(DashboardPage.class);
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

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(project, false);
        addLabel(projectPath, group1);
        addLabel(projectPath, group2);

        DashboardPage dashboard = getBrowser().openAndWaitFor(DashboardPage.class);
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

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(project, false);
        addLabel(projectPath, group1);
        addLabel(projectPath, group2);

        DashboardPage dashboard = getBrowser().openAndWaitFor(DashboardPage.class);
        assertTrue(dashboard.isGroupPresent(group1));
        assertTrue(dashboard.isGroupPresent(group2));
        assertFalse(dashboard.isUngroupedProjectPresent(project));
        // Check the ungrouped projects cannot be hidden (CIB-1963).
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

        String project1Path = rpcClient.RemoteApi.insertSimpleProject(project1, false);
        rpcClient.RemoteApi.insertSimpleProject(project2, false);

        setDashboard(asPair(SHOW_ALL_GROUPS, true), asPair(SHOW_ALL_PROJECTS, false), asPair(SHOWN_PROJECTS, asVector(project1Path)));

        DashboardPage dashboard = getBrowser().openAndWaitFor(DashboardPage.class);
        assertTrue(dashboard.isUngroupedProjectPresent(project1));
        assertFalse(dashboard.isUngroupedProjectPresent(project2));
    }

    public void testHideProject() throws Exception
    {
        String project1 = random + "-project1";
        String project2 = random + "-project2";

        setDashboard(asPair(SHOW_ALL_GROUPS, true), asPair(SHOW_ALL_PROJECTS, true));

        rpcClient.RemoteApi.insertSimpleProject(project1, false);
        rpcClient.RemoteApi.insertSimpleProject(project2, false);

        DashboardPage dashboard = getBrowser().openAndWaitFor(DashboardPage.class);
        assertTrue(dashboard.isUngroupedProjectPresent(project1));
        assertTrue(dashboard.isUngroupedProjectPresent(project2));

        dashboard.hideProjectAndWait(null, project1);

        assertFalse(dashboard.isUngroupedProjectPresent(project1));
        assertTrue(dashboard.isUngroupedProjectPresent(project2));
    }
    
    public void testMultipleBuildsPerProject() throws Exception
    {
        File tmpDir = createTempDirectory();
        try
        {
            setDashboard(asPair(BUILDS_PER_PROJECT, 2));

            WaitProject project = projectConfigurations.createWaitAntProject(random, tmpDir, true);
            configurationHelper.insertProject(project.getConfig(), false);

            DashboardPage dashboard = getBrowser().openAndWaitFor(DashboardPage.class);
            List<Long> buildIds = dashboard.getBuildIds(null, random);
            assertEquals(0, buildIds.size());
            
            buildRunner.triggerBuild(project);
            rpcClient.RemoteApi.waitForBuildInProgress(random, 1);
            
            getBrowser().refresh();
            dashboard.waitForReload();
            buildIds = dashboard.getBuildIds(null, random);
            assertEquals(Arrays.asList(1L), buildIds);
            
            project.releaseBuild();
            rpcClient.RemoteApi.waitForBuildToComplete(random, 1);

            getBrowser().refresh();
            dashboard.waitForReload();
            buildIds = dashboard.getBuildIds(null, random);
            assertEquals(Arrays.asList(1L), buildIds);

            buildRunner.triggerBuild(project);
            rpcClient.RemoteApi.waitForBuildInProgress(random, 2);
            
            getBrowser().refresh();
            dashboard.waitForReload();
            buildIds = dashboard.getBuildIds(null, random);
            assertEquals(Arrays.asList(2L, 1L), buildIds);

            project.releaseBuild();
            rpcClient.RemoteApi.waitForBuildToComplete(random, 1);

            getBrowser().refresh();
            dashboard.waitForReload();
            buildIds = dashboard.getBuildIds(null, random);
            assertEquals(Arrays.asList(2L, 1L), buildIds);
        }
        finally
        {
            FileSystemUtils.rmdir(tmpDir);
        }
    }
    
    private void addLabel(String projectPath, String label) throws Exception
    {
        Hashtable<String, Object> labelConfig = rpcClient.RemoteApi.createDefaultConfig(LabelConfiguration.class);
        labelConfig.put("label", label);
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, "labels"), labelConfig);
    }

    private void setDashboard(Pair<String, ?>... values) throws Exception
    {
        String dashboardPath = PathUtils.getPath(userPath, "preferences", "dashboard");
        Hashtable<String, Object> dashboardConfig = rpcClient.RemoteApi.getConfig(dashboardPath);
        for (Pair<String, ?> pair : values)
        {
            if (!pair.second.equals(dashboardConfig.get(pair.first)))
            {
                dashboardConfig.put(pair.first, pair.second);
                rpcClient.RemoteApi.saveConfig(dashboardPath, dashboardConfig, false);
            }
        }
    }
}
