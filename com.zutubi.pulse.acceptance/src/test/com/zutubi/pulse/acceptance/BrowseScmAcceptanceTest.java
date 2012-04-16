package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.admin.AntCommandForm;
import com.zutubi.pulse.acceptance.forms.admin.ConvertToVersionedForm;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.Condition;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

/**
 * A high level acceptance test that checks the ability to browse and select
 * files and directories from the scm repository.
 */
public class BrowseScmAcceptanceTest extends AcceptanceTestBase
{
    private static final long TIMEOUT = 30000;
    
    protected void setUp() throws Exception
    {
        super.setUp();

        getBrowser().loginAsAdmin();
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        getBrowser().logout();

        super.tearDown();
    }

    //---( Test the rendering of the 'browse' link on the project wizard type form )---

    public void testBrowseLinkAvailableInProjectWizardForSubversion()
    {
        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.runAddProjectWizard(new AddProjectWizard.DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, random, false)
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
        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.runAddProjectWizard(new AddProjectWizard.DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, random, false)
        {
            public String selectScm()
            {
                return "zutubi.gitConfig";
            }

            public void scmState(AddProjectWizard.ScmState form)
            {
                form.nextFormElements("file://doesnt/matter/", "master");
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
        configurationHelper.insertProject(projectConfigurations.createTestAntProject(random).getConfig(), false);
        ProjectConfigPage projectPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, random, false);
        projectPage.clickAction("convertToVersioned");

        ConvertToVersionedForm form = getBrowser().createForm(ConvertToVersionedForm.class);
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
        configurationHelper.insertProject(projectConfigurations.createTestAntProject(random).getConfig(), false);
        getBrowser().open(urls.adminProject(WebUtils.uriComponentEncode(random)) + "/" +
                Constants.Project.TYPE + "/" +
                Constants.Project.MultiRecipeType.RECIPES + "/" +
                Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME
        );
        getBrowser().waitAndClick(By.id(ListPage.ADD_LINK));

        SelectTypeState commandType = new SelectTypeState(getBrowser());
        commandType.waitFor();
        commandType.nextFormElements("zutubi.antCommandConfig");
        
        final AntCommandForm antForm = getBrowser().createForm(AntCommandForm.class);
        TestUtils.waitForCondition(new Condition()
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
        configurationHelper.insertProject(projectConfigurations.createTestAntProject(random).getConfig(), template);
        getBrowser().open(urls.adminProject(WebUtils.uriComponentEncode(random)) +
                Constants.Project.TYPE + "/" +
                Constants.Project.MultiRecipeType.RECIPES + "/" +
                Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME + "/" +
                Constants.Project.MultiRecipeType.Recipe.COMMANDS + "/"+
                Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND
        );
        AntCommandForm antForm = getBrowser().createForm(AntCommandForm.class);
        antForm.waitFor();
        return antForm;
    }

    private void selectJUnitJarAndConfirm(final AntCommandForm antForm)
    {
        PulseFileSystemBrowserWindow browse = antForm.clickBrowseBuildFile();
        browse.waitForNode("lib");
        browse.doubleClickNode("lib");
        browse.waitForNode("junit-3.8.1.jar");
        browse.selectNode("junit-3.8.1.jar");
        browse.clickOk();
        browse.waitForClose();

        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return "lib/junit-3.8.1.jar".equals(antForm.getBuildFileFieldValue());
            }
        }, TIMEOUT, "build file field to be updated to with the selected value. " +
                "Current value is " + antForm.getBuildFileFieldValue() + ", expected lib/junit-3.8.1.jar");
    }
}
