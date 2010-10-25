package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.Constants.TRIVIAL_ANT_REPOSITORY;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.Collections;

public class ProjectHomeAcceptanceTest extends SeleniumTestBase
{
    private static final long BUILD_TIMEOUT = 90000;

    private static final String CHANGE_AUTHOR = "pulse";

    private static final String BUILD_FILE_BROKEN = "<?xml version=\"1.0\"?>\n" +
                "<project default=\"default\">\n" +
                "    <target name=\"default\">\n" +
                "        <fail message=\"broken\"/>\n" +
                "    </target>\n" +
                "</project>";

    private static final String BUILD_FILE_FIXED = "<?xml version=\"1.0\"?>\n" +
                "<project default=\"default\">\n" +
                "    <target name=\"default\">\n" +
                "        <echo message=\"fixed\"/>\n" +
                "    </target>\n" +
                "</project>";

    private static final String COMMENT_BROKEN = "I broke it!";
    private static final String COMMENT_FIXED = "Phew, nobody noticed.";

    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private BuildRunner buildRunner;

    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir();

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);
        buildRunner = new BuildRunner(xmlRpcHelper);
        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testNewProject() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random);
        browser.loginAsAdmin();

        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, random);
        assertEquals("unknown", homePage.getHealth());
        assertEquals("idle", homePage.getState());
        assertEquals(0, homePage.getSuccessRate());
        assertFalse(homePage.hasStatistics());
        assertFalse(homePage.hasBuildActivity());
        assertFalse(homePage.hasLatestCompletedBuild());
        assertEquals(0, homePage.getRecentBuildsCount());
        assertEquals(0, homePage.getChangesCount());
        assertEquals(asList("clean up build directories", "trigger", "take responsibility"), homePage.getActions());
        assertEquals(asList("configure"), homePage.getLinks());
    }

    public void testBuildActivity() throws Exception
    {
        WaitProject project = projects.createWaitAntProject(random, new File(tempDir, random));
        configurationHelper.insertProject(project.getConfig(), false);

        buildRunner.triggerBuild(project);
        xmlRpcHelper.waitForBuildInProgress(project.getName(), 1);

        browser.loginAsAdmin();
        final ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, project.getName());
        assertTrue(homePage.hasBuildActivity());
        assertEquals("building", homePage.getState());
        assertEquals(1, homePage.getActiveBuildCount());

        buildRunner.triggerBuild(project);
        browser.refreshUntil(BUILD_TIMEOUT, new Condition()
        {
            public boolean satisfied()
            {
                homePage.waitFor();
                return homePage.getActivityCount() == 2;
            }
        }, "second build to show as queued");

        assertEquals(1, homePage.getQueuedBuildCount());
        assertEquals(1, homePage.getActiveBuildCount());
        
        project.releaseBuild();

        homePage.waitForLatestCompletedBuild(2, BUILD_TIMEOUT);
        assertFalse(homePage.hasBuildActivity());
    }

    public void testBuildAndChangeHistory() throws Exception
    {
        addProject(random, true);
        int initialBuildNumber = xmlRpcHelper.runBuild(random);
        String initialRevision = xmlRpcHelper.getBuildRevision(random, initialBuildNumber);
        String brokenRevision = editAndCommitBuildFile(COMMENT_BROKEN, BUILD_FILE_BROKEN);
        int brokenBuildNumber = xmlRpcHelper.runBuild(random);
        String fixedRevision = editAndCommitBuildFile(COMMENT_FIXED, BUILD_FILE_FIXED);
        int fixedBuildNumber = xmlRpcHelper.runBuild(random);

        browser.loginAsAdmin();
        ProjectHomePage homePage = browser.openAndWaitFor(ProjectHomePage.class, random);
        assertEquals("ok", homePage.getHealth());
        assertEquals("idle", homePage.getState());
        assertEquals(67, homePage.getSuccessRate());

        assertEquals(fixedBuildNumber, homePage.getLatestCompletedBuildId());
        assertEquals(ResultState.SUCCESS, homePage.getLatestCompletedBuildStatus());

        assertEquals(asList(
                new ProjectHomePage.BuildInfo(brokenBuildNumber, ResultState.FAILURE, brokenRevision),
                new ProjectHomePage.BuildInfo(initialBuildNumber, ResultState.SUCCESS, initialRevision)
        ), homePage.getRecentBuilds());
        
        assertEquals(asList(
                new Changelist(new Revision(fixedRevision), 0, CHANGE_AUTHOR, COMMENT_FIXED, Collections.<FileChange>emptyList()),
                new Changelist(new Revision(brokenRevision), 0, CHANGE_AUTHOR, COMMENT_BROKEN, Collections.<FileChange>emptyList())
        ), homePage.getChanges());
    }

    private String editAndCommitBuildFile(String comment, String newContent) throws IOException, SVNException
    {
        File wcDir = new File(tempDir, "wc");
        SubversionWorkspace workspace = new SubversionWorkspace(wcDir, CHANGE_AUTHOR, CHANGE_AUTHOR);
        try
        {
            if (!wcDir.isDirectory())
            {
                workspace.doCheckout(TRIVIAL_ANT_REPOSITORY);
            }

            File buildFile = new File(wcDir, "build.xml");
            assertTrue(buildFile.exists());
            FileSystemUtils.createFile(buildFile, newContent);

            return workspace.doCommit(comment, buildFile);
        }
        finally
        {
            IOUtils.close(workspace);
        }
    }
}
