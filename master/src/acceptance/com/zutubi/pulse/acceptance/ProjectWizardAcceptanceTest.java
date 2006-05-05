/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.util.RandomUtils;
import com.zutubi.pulse.acceptance.forms.SubversionForm;
import com.zutubi.pulse.acceptance.forms.CustomDetailsForm;

import java.util.Properties;
import java.util.Map;

/**
 * <class-comment/>
 */
public class ProjectWizardAcceptanceTest extends BaseAcceptanceTest
{
    public ProjectWizardAcceptanceTest()
    {
    }

    public ProjectWizardAcceptanceTest(String name)
    {
        super(name);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        login("admin", "admin");
    }

    public void testCreateVersionedProject()
    {
        // navigate to project panel.
        beginAt("/");
        clickLinkWithText("projects");
        assertLinkPresentWithText("add new project");

        clickLinkWithText("add new project");
        assertFormPresent(FO_PROJECT_BASICS);

        String projectName = "test project " + RandomUtils.randomString(3);
        String description = "this is a test project created by the automated project wizard acceptance test.";
        String url = "http://wizard/url";
        String scm = "cvs";
        String type = "versioned";

        submitProjectBasicsForm(projectName, description, url, scm, type);

        submitCvsSetupForm("/local", "module", "", "");

        assertFormElementNotEmpty(VERSIONED_SETUP_FILE);
        submitVersionedSetupForm("pulse.xml");

        // assert that all of the expected tables have the expected data.
        assertBasics(projectName, description, url);

        Properties properties = new Properties();
        properties.put("pulse file", "pulse.xml");

        assertSpecifics(type, properties);

        String location = "/local[module]";
        assertScm(scm, location);

        assertDefaultSetup(projectName);

        // ensure that it appears in your list of projects.
        clickLinkWithText("projects");
        assertLinkPresentWithText(projectName);
    }

    public void testCreateCustomProject()
    {
        // navigate to project panel.
        beginAt("/");
        clickLinkWithText("projects");
        assertLinkPresentWithText("add new project");

        clickLinkWithText("add new project");
        assertFormPresent(FO_PROJECT_BASICS);

        String projectName = "custom project " + RandomUtils.randomString(3);
        String description = "this is a custom project created by the automated project wizard acceptance test.";
        String url = "http://custom/url";
        String scm = "svn";
        String type = "custom";

        submitProjectBasicsForm(projectName, description, url, scm, type);

        SubversionForm.Create svnForm = new SubversionForm.Create(tester);
        svnForm.assertFormPresent();
        String location = "http://url";
        svnForm.nextFormElements("username", "password", location, null, null);

        CustomDetailsForm detailsForm = new CustomDetailsForm(tester);
        detailsForm.assertFormPresent();
        detailsForm.nextFormElements("<?xml version=\"1.0\"?><project><property name=\"foo\" value=\"${base.dir\"/></project>");

        // assert that all of the expected tables have the expected data.
        assertBasics(projectName, description, url);

        Properties properties = new Properties();
        assertSpecifics(type, properties);

        assertScm("subversion", location);

        assertDefaultSetup(projectName);

        // ensure that it appears in your list of projects.
        clickLinkWithText("projects");
        assertLinkPresentWithText(projectName);
    }

    public void testProjectNameUnique()
    {
        clickLinkWithText("projects");
        clickLink("project.add");

        String projectName = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(projectName, "", "", "cvs", "versioned");
        submitCvsSetupForm("/local", "module", "", "");
        submitVersionedSetupForm("pulse.xml");
        assertTablePresent("project.basics");

        clickLinkWithText("projects");
        clickLink("project.add");

        submitProjectBasicsForm(projectName, "", "", "cvs", "versioned");
        assertFormPresent(FO_PROJECT_BASICS);
    }

    private void assertBasics(String projectName, String description, String url)
    {
        assertTablePresent("project.basics");
        assertTableRowsEqual("project.basics", 1, new String[][]{
                new String[]{"name", projectName},
                new String[]{"description", description},
                new String[]{"url", url}
        });
    }

    private void assertSpecifics(String type, Properties properties)
    {
        int rowCount = properties.size() + 1;

        if(!type.equals("versioned"))
        {
            rowCount++;
        }

        String [][] rows = new String[2][rowCount];

        rows[0] = new String[]{"type", type};

        int i = 1;
        for(Map.Entry e: properties.entrySet())
        {
            rows[i++] = new String[] { (String) e.getKey(), (String) e.getValue() };
        }

        if(!type.equals("versioned"))
        {
            rows[i] = new String[] { "actions", "convert to versioned project" };
        }

        assertTablePresent("project.specifics");
        assertTableRowsEqual("project.specifics", 1, rows);
    }

    private void assertScm(String scm, String location)
    {
        assertTablePresent("project.scm");
        assertTableRowsEqual("project.scm", 1, new String[][]{
                new String[]{"type", scm},
                new String[]{"location", location}
        });
    }

    private void assertDefaultSetup(String projectName)
    {
        assertTablePresent("project.cleanup");
        assertTableRowsEqual("project.cleanup", 1, new String[][]{
                new String[]{"what", "with state(s)", "retain for up to", "actions", "actions"},
                new String[]{"working directories", "any", "10 builds", "edit", "delete"},
                new String[]{"add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule"},
        });

        assertTablePresent("project.buildspecs");
        assertTableRowsEqual("project.buildspecs", 1, new String[][]{
                new String[]{"name", "recipe", "timeout", "actions", "actions", "actions"},
                new String[]{"default", "[default]", "[never]", "trigger", "edit", "delete"},
                new String[]{"add new build specification", "add new build specification",
                        "add new build specification", "add new build specification",
                        "add new build specification", "add new build specification"}
        });

        assertTablePresent("project.triggers");
        assertTableRowsEqual("project.triggers", 1, new String[][]{
                new String[]{"name", "type", "build specification", "actions", "actions"},
                new String[]{projectName + " scm trigger", "event", "default", "edit", "delete"},
                new String[]{"add new trigger", "add new trigger", "add new trigger", "add new trigger", "add new trigger"}
        });
    }

}
