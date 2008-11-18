package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.admin.AntTypeForm;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.windows.BrowseScmWindow;
import com.zutubi.pulse.master.model.ProjectManager;

import java.util.Hashtable;
import java.io.File;

/**
 * A high level acceptance test that checks the ability to browse and select
 * files and directories from the scm repository.
 */
public class BrowseScmAcceptanceTest extends SeleniumTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        logout();
        
        super.tearDown();
    }

    //---( Test the rendering of the 'browse' link on the project wizard type form )---

    public void testBrowseLinkAvailableInProjectWizardForSubversion()
    {
        runAddProjectWizard(new DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, random, false)
        {
            public void typeState(AddProjectWizard.TypeState form)
            {
                AddProjectWizard.AntState ant = (AddProjectWizard.AntState) form;
                assertTrue(ant.isBrowseFileAvailable());
                assertTrue(ant.isBrowseWorkAvailable());
                ant.cancel();
            }
        });
    }

    public void testBrowseLinkNotAvailableInProjectWizardForGit()
    {
        runAddProjectWizard(new DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, random, false)
        {
            public String selectScm()
            {
                return "zutubi.gitConfig";
            }

            public void scmState(AddProjectWizard.ScmState form)
            {
                form.nextFormElements("file://doesnt/matter/", "master", "CLEAN_CHECKOUT");
            }

            public void typeState(AddProjectWizard.TypeState form)
            {
                AddProjectWizard.AntState ant = (AddProjectWizard.AntState) form;
                assertFalse(ant.isBrowseFileAvailable());
                assertFalse(ant.isBrowseWorkAvailable());
                ant.cancel();
            }
        });
    }

    //---( test rendering browse link is available in the project type configuration pages. )---

    public void testBrowseLinkAvailableForSubversionAntProjectConfiguration() throws Exception
    {
        AntTypeForm antForm = insertTestSvnProjectAndNavigateToTypeConfig();
        assertTrue(antForm.isBrowseBuildFileLinkPresent());
        assertTrue(antForm.isBrowseWorkingDirectoryLinkPresent());
    }

    public void testBrowseLinkAvailableForGitAntProjectConfiguration() throws Exception
    {
        AntTypeForm antForm = insertTestGitProjectAndNavigateToTypeConfig();
        assertTrue(antForm.isBrowseBuildFileLinkPresent());
        assertTrue(antForm.isBrowseWorkingDirectoryLinkPresent());
    }

    public void testBrowseSelectionOfScmFile() throws Exception
    {
        AntTypeForm antForm = insertTestSvnProjectAndNavigateToTypeConfig();
        assertEquals("build.xml", antForm.getBuildFileFieldValue());
        assertTrue(antForm.isBrowseBuildFileLinkPresent());

        BrowseScmWindow browse = antForm.clickBrowseBuildFile();
        browse.waitForNode("lib");
        browse.doubleClickNode("lib");
        browse.waitForNode("lib", "junit-3.8.1.jar");
        browse.selectNode("lib", "junit-3.8.1.jar");
        browse.clickOkay();

        assertEquals("lib/junit-3.8.1.jar", antForm.getBuildFileFieldValue());
    }

    public void testBrowseSelectionOfScmDirectory() throws Exception
    {
        AntTypeForm antForm = insertTestSvnProjectAndNavigateToTypeConfig();
        assertTrue(antForm.isBrowseWorkingDirectoryLinkPresent());

        BrowseScmWindow browse = antForm.clickBrowseWorkingDirectory();
        browse.waitForNode("lib");
        browse.doubleClickNode("lib");
        assertFalse(browse.isNodePresent("lib", "junit-3.8.1.jar"));
        browse.selectNode("lib");
        browse.clickOkay();

        assertEquals("lib", antForm.getWorkingDirectoryFieldValue());
    }

    public void testBrowseAndCancelSelectionOfScmFile() throws Exception
    {
        AntTypeForm antForm = insertTestSvnProjectAndNavigateToTypeConfig();
        assertEquals("build.xml", antForm.getBuildFileFieldValue());
        assertTrue(antForm.isBrowseBuildFileLinkPresent());

        BrowseScmWindow browse = antForm.clickBrowseBuildFile();
        browse.waitForNode("lib");
        browse.doubleClickNode("lib");
        browse.waitForNode("lib", "junit-3.8.1.jar");
        browse.selectNode("lib", "junit-3.8.1.jar");
        browse.clickCancel();

        assertEquals("build.xml", antForm.getBuildFileFieldValue());
    }

    public void testBrowseFileUsesWorkingDirectory() throws Exception
    {
        AntTypeForm antForm = insertTestSvnProjectAndNavigateToTypeConfig();
        antForm.setFieldValue("work", "src");

        BrowseScmWindow browse = antForm.clickBrowseBuildFile();
        browse.waitForNode("java");
        assertFalse(browse.isNodePresent("lib"));
        assertFalse(browse.isNodePresent("build.xml"));
        assertTrue(browse.isNodePresent("test"));
        browse.clickCancel();

        assertEquals("build.xml", antForm.getBuildFileFieldValue());
    }


    private AntTypeForm insertTestSvnProjectAndNavigateToTypeConfig() throws Exception
    {
        Hashtable<String, Object> svnConfig = xmlRpcHelper.getSubversionConfig();
        svnConfig.put("url", Constants.TEST_PROJECT_REPOSITORY);
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, svnConfig, xmlRpcHelper.getAntConfig());
        xmlRpcHelper.logout();
        return navigateToTypeConfig();
    }

    private AntTypeForm insertTestGitProjectAndNavigateToTypeConfig() throws Exception
    {
        // the git repository is located on the local file system in the work.dir/git-repo directory
        File workingDir = new File("./working"); // from IDEA, the working directory is located in the same directory as where the projects are run.
        if (System.getProperties().contains("work.dir"))
        {
            // from the acceptance test suite, the work.dir system property is specified
            workingDir = new File(System.getProperty("work.dir"));
        }
        File repositoryBase = new File(workingDir, "git-repo");

        Hashtable<String, Object> gitConfig = xmlRpcHelper.createEmptyConfig("zutubi.gitConfig");
        gitConfig.put("repository", "file://" + repositoryBase.getCanonicalPath());
        gitConfig.put("checkoutScheme", "CLEAN_CHECKOUT");
        gitConfig.put("monitor", false);
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.insertProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, gitConfig, xmlRpcHelper.getAntConfig());
        xmlRpcHelper.logout();
        return navigateToTypeConfig();
    }

    private AntTypeForm navigateToTypeConfig()
    {
        // go to the type configuration pages.
        ProjectHierarchyPage globalPage = new ProjectHierarchyPage(selenium, urls, random, false);
        globalPage.goTo();
        globalPage.waitFor();
        ProjectConfigPage configPage = globalPage.clickConfigure();
        configPage.waitFor();
        CompositePage page = configPage.clickComposite("type", "ant command and artifacts");
        page.waitFor();

        AntTypeForm antForm = new AntTypeForm(selenium);
        antForm.waitFor();
        return antForm;
    }
}
