package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.util.Hashtable;

/**
 * Tests for the shared projects summary parts of the dashbaord and browse
 * sections of the UI.
 */
public class ProjectsSummaryAcceptanceTest extends SeleniumTestBase
{
    private static final int BUILD_TIMEOUT = 90000;

    private static final String STATUS_NONE_BUILDING = "no projects building";
    private static final String STATUS_ONE_BUILDING = "1 project building";

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testBrowseViewDescendentsBuilding() throws Exception
    {
        descendentsBuildingHelper(new BrowsePage(selenium, urls));
    }

    public void testDashboardDescendentsBuilding() throws Exception
    {
        descendentsBuildingHelper(new DashboardPage(selenium, urls));
    }

    private void descendentsBuildingHelper(ProjectsSummaryPage summaryPage) throws Exception
    {
        String templateProject = random + "-template";
        String childProject = random + "-child";

        File waitFile = new File(FileSystemUtils.getSystemTempDir(), childProject);

        Hashtable<String, Object> svn = xmlRpcHelper.getSubversionConfig(Constants.WAIT_ANT_REPOSITORY);
        xmlRpcHelper.insertProject(templateProject, ProjectManager.GLOBAL_PROJECT_NAME, true, svn, null);

        Hashtable<String,Object> antType = xmlRpcHelper.getAntConfig();
        antType.put(Constants.Project.AntCommand.ARGUMENTS, getFileArgument(waitFile));
        xmlRpcHelper.insertSingleCommandProject(childProject, templateProject, false, null, antType);

        try
        {
            loginAsAdmin();
            summaryPage.goTo();
            assertEquals(STATUS_NONE_BUILDING, summaryPage.getBuildingSummary(null, templateProject));

            triggerAndWaitForBuildToCommence(childProject);
            selenium.refresh();
            selenium.waitForPageToLoad("30000");
            assertEquals(STATUS_ONE_BUILDING, summaryPage.getBuildingSummary(null, templateProject));

            FileSystemUtils.createFile(waitFile, "test");
            xmlRpcHelper.waitForBuildToComplete(childProject, 1, BUILD_TIMEOUT);
            selenium.refresh();
            selenium.waitForPageToLoad("30000");
            assertEquals(STATUS_NONE_BUILDING, summaryPage.getBuildingSummary(null, templateProject));
        }
        finally
        {
            waitFile.delete();
            xmlRpcHelper.cancelBuild(childProject, 1);
        }
    }

    private String getFileArgument(File waitFile1)
    {
        return "-Dfile=" + waitFile1.getAbsolutePath().replace("\\", "/");
    }

    private void triggerAndWaitForBuildToCommence(String project) throws Exception
    {
        xmlRpcHelper.triggerBuild(project);
        xmlRpcHelper.waitForBuildInProgress(project, 1, BUILD_TIMEOUT);
    }
}
