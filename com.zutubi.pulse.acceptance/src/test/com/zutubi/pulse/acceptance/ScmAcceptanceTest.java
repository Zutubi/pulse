package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.master.model.ProjectManager;

/**
 * A high level acceptance test that checks some of the scm integration
 * points.
 */
public class ScmAcceptanceTest extends SeleniumTestBase
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
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.insertSimpleProject(random, false);
        xmlRpcHelper.logout();

        // go to the type configuration pages.
        ProjectHierarchyPage globalPage = new ProjectHierarchyPage(selenium, urls, random, false);
        globalPage.goTo();
        globalPage.waitFor();
        ProjectConfigPage configPage = globalPage.clickConfigure();
        configPage.waitFor();
        CompositePage page = configPage.clickComposite("type", "ant command and artifacts");
        page.waitFor();

        selenium.isElementPresent("zfid.work.browse");
        selenium.isElementPresent("zfid.file.browse");        
    }

    public void testBrowseLinkNoAvailableForGitAntProjectConfiguration()
    {
        // TODO: need to set up a temporary git repository to allow testing.  Need to be able to initialise the project.
    }
}
