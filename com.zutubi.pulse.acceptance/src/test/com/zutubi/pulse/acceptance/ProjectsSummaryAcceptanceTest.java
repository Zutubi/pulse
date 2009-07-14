package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.ProjectsSummaryPage;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.acceptance.pages.dashboard.DashboardPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.util.Constants.SECOND;
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
        descendentsBuildingHelper(browser.createPage(BrowsePage.class));
    }

    public void testDashboardDescendentsBuilding() throws Exception
    {
        descendentsBuildingHelper(browser.createPage(DashboardPage.class));
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
            summaryPage.openAndWaitFor();
            assertEquals(STATUS_NONE_BUILDING, summaryPage.getBuildingSummary(null, templateProject));

            triggerAndWaitForBuildToCommence(childProject);
            browser.refresh();
            browser.waitForPageToLoad(30 * SECOND);
            assertEquals(STATUS_ONE_BUILDING, summaryPage.getBuildingSummary(null, templateProject));

            FileSystemUtils.createFile(waitFile, "test");
            xmlRpcHelper.waitForBuildToComplete(childProject, 1);
            browser.refresh();
            browser.waitForPageToLoad(30 * SECOND);
            assertEquals(STATUS_NONE_BUILDING, summaryPage.getBuildingSummary(null, templateProject));
        }
        finally
        {
            if (waitFile.exists() && !waitFile.delete())
            {
                waitFile.deleteOnExit();
            }
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

    public void testInvalidProject() throws Exception
    {
        String template = random + "-template";
        String concrete = random + "-concrete";

        String templatePath = xmlRpcHelper.insertSingleCommandProject(template, ProjectManager.GLOBAL_PROJECT_NAME, true, xmlRpcHelper.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig());
        xmlRpcHelper.insertProject(concrete, template, false, null, null);

        String svnPath = PathUtils.getPath(templatePath, Constants.Project.SCM);
        Hashtable<String, Object> svn = xmlRpcHelper.getConfig(svnPath);
        svn.put("url", "");
        xmlRpcHelper.saveConfig(svnPath, svn, false);
        
        loginAsAdmin();
        BrowsePage browsePage = browser.openAndWaitFor(BrowsePage.class);
        assertTrue(browsePage.isInvalidProjectPresent(concrete));
    }
}
