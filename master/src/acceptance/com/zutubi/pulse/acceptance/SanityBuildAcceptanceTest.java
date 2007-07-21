package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * An acceptance test that adds a very simple project and runs a build as a
 * sanity test.
 */
public class SanityBuildAcceptanceTest extends SeleniumTestBase
{
    public void testSimpleBuild() throws InterruptedException, IOException, SAXException
    {
        loginAsAdmin();

        goTo(Navigation.LOCATION_PROJECT_CONFIG);

        ProjectHierarchyPage globalPage = new ProjectHierarchyPage(selenium, "global project template", true);
        globalPage.waitFor();
        globalPage.clickAdd();

        String projectName = "project " + random;
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(selenium);
        projectState.waitFor();
        projectState.nextFormElements(projectName, "test description", "http://test.com/");

        SelectTypeState scmTypeState = new SelectTypeState(selenium);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("svn");

        AddProjectWizard.SvnState svnState = new AddProjectWizard.SvnState(selenium);
        svnState.waitFor();
        svnState.nextFormElements("svn://localhost/test/trunk", null, null, null, null, null);

        SelectTypeState projectTypeState = new SelectTypeState(selenium);
        projectTypeState.waitFor();
        scmTypeState.nextFormElements("ant");

        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(selenium);
        antState.waitFor();
        antState.finishFormElements("build.xml", null, null, null);

        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, projectName, false);
        hierarchyPage.waitFor();
        hierarchyPage.assertPresent();
    }

}
