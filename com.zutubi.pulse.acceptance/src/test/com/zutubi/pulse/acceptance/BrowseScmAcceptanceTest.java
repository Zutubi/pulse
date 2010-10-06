package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.admin.AntCommandForm;
import com.zutubi.pulse.acceptance.forms.admin.ConvertToVersionedForm;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.utils.ConfigurationHelper;
import com.zutubi.pulse.acceptance.utils.ConfigurationHelperFactory;
import com.zutubi.pulse.acceptance.utils.ProjectConfigurations;
import com.zutubi.pulse.acceptance.utils.SingletonConfigurationHelperFactory;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.Condition;
import com.zutubi.util.WebUtils;

/**
 * A high level acceptance test that checks the ability to browse and select
 * files and directories from the scm repository.
 */
public class BrowseScmAcceptanceTest extends SeleniumTestBase
{
    private static final long TIMEOUT = 30000;
    
    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;

    protected void setUp() throws Exception
    {
        super.setUp();

        browser.loginAsAdmin();
        xmlRpcHelper.loginAsAdmin();

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);
        projects = new ProjectConfigurations(configurationHelper);
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        browser.logout();

        super.tearDown();
    }

    //---( Test the rendering of the 'browse' link on the project wizard type form )---

    public void testBrowseLinkAvailableInProjectWizardForSubversion()
    {
        runAddProjectWizard(new DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, random, false)
        {
            public void commandState(AddProjectWizard.CommandState form)
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

            public void commandState(AddProjectWizard.CommandState form)
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
        AntCommandForm antForm = insertTestSvnProjectAndNavigateToCommandConfig(false);
        assertTrue(antForm.isBrowseBuildFileLinkPresent());
        assertTrue(antForm.isBrowseWorkingDirectoryLinkPresent());
    }

    public void testBrowseLinkAvailableForTemplateSubversionAntProjectConfiguration() throws Exception
    {
        AntCommandForm antForm = insertTestSvnProjectAndNavigateToCommandConfig(true);
        assertTrue(antForm.isBrowseBuildFileLinkPresent());
        assertTrue(antForm.isBrowseWorkingDirectoryLinkPresent());
    }
    
    // --( test rendering browse link for project to versioned transformation )---
    public void testBrowseLinkAvailableForVersionedProjectConversion() throws Exception
    {
        configurationHelper.insertProject(projects.createTestAntProject(random).getConfig(), false);
        ProjectConfigPage projectPage = browser.openAndWaitFor(ProjectConfigPage.class, random, false);
        projectPage.clickAction("convertToVersioned");

        ConvertToVersionedForm form = browser.createForm(ConvertToVersionedForm.class);
        form.waitFor();
        
        assertTrue(form.isBrowsePulseFileNameLinkAvailable());
        PulseFileSystemBrowserWindow browse = form.clickBrowsePulseFileName();
        browse.waitForNode("src");
        browse.selectNode("src");
        browse.clickOk();
        browse.waitForClose();

        assertEquals("src", form.getPulseFileNameFieldValue());
    }

    //---( test the browse window . )---

    public void testBrowseSelectionOfScmFile() throws Exception
    {
        AntCommandForm antForm = insertTestSvnProjectAndNavigateToCommandConfig(false);
        assertEquals("build.xml", antForm.getBuildFileFieldValue());
        assertTrue(antForm.isBrowseBuildFileLinkPresent());

        selectJUnitJarAndConfirm(antForm);
    }

    public void testBrowseSelectionOfScmDirectory() throws Exception
    {
        AntCommandForm antForm = insertTestSvnProjectAndNavigateToCommandConfig(false);
        assertTrue(antForm.isBrowseWorkingDirectoryLinkPresent());

        PulseFileSystemBrowserWindow browse = antForm.clickBrowseWorkingDirectory();
        browse.waitForNode("lib");
        browse.doubleClickNode("lib");
        assertFalse(browse.isNodePresent("junit-3.8.1.jar"));
        browse.selectNode("lib");
        browse.clickOk();
        browse.waitForClose();

        assertEquals("lib", antForm.getWorkingDirectoryFieldValue());
    }

    public void testBrowseAndCancelSelectionOfScmFile() throws Exception
    {
        AntCommandForm antForm = insertTestSvnProjectAndNavigateToCommandConfig(false);
        assertEquals("build.xml", antForm.getBuildFileFieldValue());
        assertTrue(antForm.isBrowseBuildFileLinkPresent());

        PulseFileSystemBrowserWindow browse = antForm.clickBrowseBuildFile();
        browse.waitForNode("lib");
        browse.doubleClickNode("lib");
        browse.waitForNode("junit-3.8.1.jar");
        browse.selectNode("junit-3.8.1.jar");
        browse.clickCancel();
        browse.waitForClose();

        assertEquals("build.xml", antForm.getBuildFileFieldValue());
    }

    public void testBrowseFileUsesWorkingDirectory() throws Exception
    {
        AntCommandForm antForm = insertTestSvnProjectAndNavigateToCommandConfig(false);
        antForm.setFieldValue("workingDir", "src");

        PulseFileSystemBrowserWindow browse = antForm.clickBrowseBuildFile();
        browse.waitForNode("java");
        assertFalse(browse.isNodePresent("lib"));
        assertFalse(browse.isNodePresent("build.xml"));
        assertTrue(browse.isNodePresent("test"));
        browse.expandPath("java", "com", "zutubi", "testant");
        browse.waitForNode("Unit.java");
        browse.selectNode("Unit.java");
        browse.clickOk();
        browse.waitForClose();

        assertEquals("java/com/zutubi/testant/Unit.java", antForm.getBuildFileFieldValue());
    }

    public void testBrowseTemplateProject() throws Exception
    {
        AntCommandForm antForm = insertTestSvnProjectAndNavigateToCommandConfig(true);
        assertEquals("build.xml", antForm.getBuildFileFieldValue());
        assertTrue(antForm.isBrowseBuildFileLinkPresent());

        selectJUnitJarAndConfirm(antForm);
    }

    public void testBrowseAddingCommandToExistingProject() throws Exception
    {
        configurationHelper.insertProject(projects.createTestAntProject(random).getConfig(), false);
        browser.open(urls.adminProject(WebUtils.uriComponentEncode(random)) + "/" +
                Constants.Project.TYPE + "/" +
                Constants.Project.MultiRecipeType.RECIPES + "/" +
                Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME
        );
        browser.waitAndClick(ListPage.ADD_LINK);

        SelectTypeState commandType = new SelectTypeState(browser);
        commandType.waitFor();
        commandType.nextFormElements("zutubi.antCommandConfig");
        
        final AntCommandForm antForm = browser.createForm(AntCommandForm.class);
        AcceptanceTestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return antForm.isBrowseBuildFileLinkPresent(); 
            }
        }, TIMEOUT, "browse link to be shown");

        selectJUnitJarAndConfirm(antForm);
    }

    private AntCommandForm insertTestSvnProjectAndNavigateToCommandConfig(boolean template) throws Exception
    {
        configurationHelper.insertProject(projects.createTestAntProject(random).getConfig(), template);
        browser.open(urls.adminProject(WebUtils.uriComponentEncode(random)) +
                Constants.Project.TYPE + "/" +
                Constants.Project.MultiRecipeType.RECIPES + "/" +
                Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME + "/" +
                Constants.Project.MultiRecipeType.Recipe.COMMANDS + "/"+
                Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND
        );
        AntCommandForm antForm = browser.createForm(AntCommandForm.class);
        antForm.waitFor();
        return antForm;
    }

    private void selectJUnitJarAndConfirm(AntCommandForm antForm)
    {
        PulseFileSystemBrowserWindow browse = antForm.clickBrowseBuildFile();
        browse.waitForNode("lib");
        browse.doubleClickNode("lib");
        browse.waitForNode("junit-3.8.1.jar");
        browse.selectNode("junit-3.8.1.jar");
        browse.clickOk();
        browse.waitForClose();
        
        assertEquals("lib/junit-3.8.1.jar", antForm.getBuildFileFieldValue());
    }
}
