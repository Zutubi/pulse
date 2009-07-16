package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.SeleniumTestBase;
import com.zutubi.pulse.acceptance.forms.admin.DependencyForm;
import com.zutubi.pulse.acceptance.pages.admin.ProjectDependenciesPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;

/**
 * A set of acceptance tests focused on the dependency systems UI.
 */
public class DependenciesConfigurationAcceptanceTest extends SeleniumTestBase
{
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

    public void testDependencyCanNotReferenceOwningProject() throws Exception
    {
        String projectName = random;

        // create project
        String path = addProject(projectName, true);

        String projectHandle = xmlRpcHelper.getConfigHandle(path);

        loginAsAdmin();

        // go to add dependency page.
        ProjectConfigPage projectPage = browser.openAndWaitFor(ProjectConfigPage.class, projectName, false);

        ProjectDependenciesPage projectDependenciesPage = projectPage.clickDependenciesAndWait();
        DependencyForm form = projectDependenciesPage.clickAdd();
        form.waitFor();
        
        assertFalse(form.isProjectInOptions(projectHandle));
    }
}
