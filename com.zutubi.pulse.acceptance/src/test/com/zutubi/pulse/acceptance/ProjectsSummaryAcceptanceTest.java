package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.util.Hashtable;

/**
 * Tests for the shared projects summary parts of the dashboard and browse
 * sections of the UI.
 */
public class ProjectsSummaryAcceptanceTest extends AcceptanceTestBase
{
    private static final int BUILD_TIMEOUT = 90000;

    private static final String STATUS_NONE_BUILDING = "no projects building";
    private static final String STATUS_ONE_BUILDING = "1 project building";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testBrowseViewDescendantsBuilding() throws Exception
    {
        descendantsBuildingHelper(getBrowser().createPage(BrowsePage.class));
    }

    public void testDashboardDescendantsBuilding() throws Exception
    {
        descendantsBuildingHelper(getBrowser().createPage(DashboardPage.class));
    }

    private void descendantsBuildingHelper(final ProjectsSummaryPage summaryPage) throws Exception
    {
        final String templateProject = random + "-template";
        String childProject = random + "-child";

        File waitFile = new File(FileSystemUtils.getSystemTempDir(), childProject);

        Hashtable<String, Object> svn = rpcClient.RemoteApi.getSubversionConfig(Constants.WAIT_ANT_REPOSITORY);
        rpcClient.RemoteApi.insertProject(templateProject, ProjectManager.GLOBAL_PROJECT_NAME, true, svn, null);

        Hashtable<String,Object> antType = rpcClient.RemoteApi.getAntConfig();
        antType.put(Constants.Project.AntCommand.ARGUMENTS, getFileArgument(waitFile));
        rpcClient.RemoteApi.insertSingleCommandProject(childProject, templateProject, false, null, antType);

        try
        {
            getBrowser().loginAsAdmin();
            summaryPage.openAndWaitFor();
            assertEquals(STATUS_NONE_BUILDING, summaryPage.getBuildingSummary(null, templateProject));

            triggerAndWaitForBuildToCommence(childProject);
            getBrowser().refresh();
            TestUtils.waitForCondition(new Condition() {
                public boolean satisfied() {
                    return STATUS_ONE_BUILDING.equals(summaryPage.getBuildingSummary(null, templateProject));
                }
            }, SeleniumBrowser.DEFAULT_TIMEOUT, "build to start");

            FileSystemUtils.createFile(waitFile, "test");
            rpcClient.RemoteApi.waitForBuildToComplete(childProject, 1);
            getBrowser().refresh();
            TestUtils.waitForCondition(new Condition() {
                public boolean satisfied() {
                    return STATUS_NONE_BUILDING.equals(summaryPage.getBuildingSummary(null, templateProject));
                }
            }, SeleniumBrowser.DEFAULT_TIMEOUT, "build to complete");
        }
        finally
        {
            if (waitFile.exists() && !waitFile.delete())
            {
                waitFile.deleteOnExit();
            }
            rpcClient.RemoteApi.cancelBuild(childProject, 1);
        }
    }

    private String getFileArgument(File waitFile1)
    {
        return "-Dfile=" + waitFile1.getAbsolutePath().replace("\\", "/");
    }

    private void triggerAndWaitForBuildToCommence(String project) throws Exception
    {
        rpcClient.RemoteApi.triggerBuild(project);
        rpcClient.RemoteApi.waitForBuildInProgress(project, 1, BUILD_TIMEOUT);
    }

    public void testInvalidProject() throws Exception
    {
        String template = random + "-template";
        String concrete = random + "-concrete";

        String templatePath = rpcClient.RemoteApi.insertSingleCommandProject(template, ProjectManager.GLOBAL_PROJECT_NAME, true, rpcClient.RemoteApi.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), rpcClient.RemoteApi.getAntConfig());
        rpcClient.RemoteApi.insertProject(concrete, template, false, null, null);

        String svnPath = PathUtils.getPath(templatePath, Constants.Project.SCM);
        Hashtable<String, Object> svn = rpcClient.RemoteApi.getConfig(svnPath);
        svn.put("url", "");
        rpcClient.RemoteApi.saveConfig(svnPath, svn, false);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertTrue(browsePage.isInvalidProjectPresent(concrete));
    }
}
