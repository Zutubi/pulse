package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.AddProjectWizard;
import com.zutubi.pulse.acceptance.forms.CustomDetailsForm;
import com.zutubi.pulse.acceptance.forms.Maven2DetailsForm;
import com.zutubi.pulse.acceptance.forms.SubversionForm;
import com.zutubi.pulse.util.RandomUtils;

import java.util.Map;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class ProjectWizardAcceptanceTest extends BaseAcceptanceTestCase
{
    private static final String TEST_CVSROOT = ":pserver:cvstester:cvs@www.cinnamonbob.com:/cvsroot";

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

        loginAsAdmin();
    }

    public void testCreateVersionedProject()
    {
        // navigate to project panel.
        startAddProjectWizard();

        String projectName = "test project " + RandomUtils.randomString(3);
        String description = "this is a test project created by the automated project wizard acceptance test.";
        String url = "http://wizard/url";
        String scm = "cvs";
        String type = "versioned";

        submitProjectBasicsForm(projectName, description, url, scm, type);

        submitCvsSetupForm(TEST_CVSROOT, "module", "", "");

        assertFormElementNotEmpty(VERSIONED_SETUP_FILE);
        submitVersionedSetupForm("pulse.xml");

        // assert that all of the expected tables have the expected data.
        assertBasics(projectName, description, url);

        Properties properties = new Properties();
        properties.put("pulse file", "pulse.xml");

        assertSpecifics(type, properties);

        String location = TEST_CVSROOT + "[module]";
        assertScm(scm, location);

        assertDefaultSetup(projectName);

        // ensure that it appears in your list of projects.
        clickLink(Navigation.TAB_PROJECTS);
        assertLinkPresentWithText(projectName);
    }

    public void testCreateCustomProject()
    {
        // navigate to project panel.
        startAddProjectWizard();

        String projectName = "custom project " + RandomUtils.randomString(3);
        String description = "this is a custom project created by the automated project wizard acceptance test.";
        String url = "http://custom/url";
        String scm = "svn";
        String type = "custom";

        submitProjectBasicsForm(projectName, description, url, scm, type);

        String location = submitSubversion();

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
        clickLink(Navigation.TAB_PROJECTS);
        assertLinkPresentWithText(projectName);
    }

    public void testCreateMaven2Project()
    {
        // navigate to project panel.
        startAddProjectWizard();

        String projectName = "maven2 project " + RandomUtils.randomString(3);
        String description = "this is a maven2 project created by the automated project wizard acceptance test.";
        String url = "http://maven2/url";
        String scm = "svn";
        String type = "maven2";

        submitProjectBasicsForm(projectName, description, url, scm, type);

        String location = submitSubversion();

        Maven2DetailsForm detailsForm = new Maven2DetailsForm(tester, true);
        detailsForm.assertFormPresent();
        detailsForm.nextFormElements("work", "compile test", "-arg");

        // assert that all of the expected tables have the expected data.
        assertBasics(projectName, description, url);

        Properties properties = new Properties();
        properties.put("working directory", "work");
        properties.put("goals", "compile test");
        properties.put("arguments", "-arg");
        assertSpecifics(type, properties);

        assertScm("subversion", location);

        assertDefaultSetup(projectName);

        // edit it
        clickLink("project.specifics.edit");
        detailsForm = new Maven2DetailsForm(tester,  false);
        detailsForm.assertFormPresent();
        detailsForm.assertFormElements("work", "compile test", "-arg");
        detailsForm.saveFormElements("newwork", "", "");

        properties.clear();
        properties.put("working directory", "newwork");
        assertSpecifics(type, properties);

        // ensure that it appears in your list of projects.
        clickLink(Navigation.TAB_PROJECTS);
        assertLinkPresentWithText(projectName);
    }

    private void startAddProjectWizard()
    {
        beginAt("/");
        clickLink(Navigation.TAB_PROJECTS);
        assertAndClick(Navigation.Projects.LINK_ADD_PROJECT);
    }

    public void testProjectNameUnique()
    {
        // create a project with the specified name (or look up an existing project?)..
        startAddProjectWizard();

        String projectName = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(projectName, "", "", "cvs", "versioned");
        submitCvsSetupForm(TEST_CVSROOT, "module", "", "");
        submitVersionedSetupForm("pulse.xml");
        assertTablePresent("project.basics");

        // attempt to create another project with that same name.
        startAddProjectWizard();

        AddProjectWizard.Select form = new AddProjectWizard.Select(tester);
        form.assertFormPresent();
        form.nextFormElements(projectName, "", "", "cvs", "versioned");

        // assert that the form is still there and that we get an error message.
        form.assertFormPresent();
        assertTextPresent("already being used");
    }

    private String submitSubversion()
    {
        SubversionForm.Create svnForm = new SubversionForm.Create(tester);
        svnForm.assertFormPresent();
        String location = "http://url";
        svnForm.nextFormElements("username", "password", location, null, null, null);
        return location;
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

        String [][] rows = new String[rowCount][2];

        rows[0] = new String[]{"type", type};

        int i = 1;
        for(Map.Entry e: properties.entrySet())
        {
            rows[i++] = new String[] { (String) e.getKey(), (String) e.getValue() };
        }

        if(!type.equals("versioned"))
        {
            if(type.equals("custom"))
            {
                rows[i] = new String[] { "actions", "convert to versioned project" };
            }
            else
            {
                rows[i] = new String[] { "actions", "convert to custom project                            \n" +
                        "                            ::\n" +
                        "                                    \n" +
                        "                                convert to versioned project" };
            }
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
                new String[]{"name", "timeout", "actions", "actions", "actions"},
                new String[]{"default", "[never]", "trigger", "edit", "delete"},
                new String[]{"add new build specification", "add new build specification",
                        "add new build specification", "add new build specification",
                        "add new build specification"}
        });

        assertTablePresent("project.triggers");
        assertTableRowsEqual("project.triggers", 1, new String[][]{
                new String[]{"name", "type", "build specification", "state", "actions", "actions", "actions"},
                new String[]{"scm trigger", "event", "default", "enabled", "disable", "edit", "delete"},
                new String[]{"add new trigger", "add new trigger", "add new trigger", "add new trigger", "add new trigger", "add new trigger", "add new trigger"}
        });
    }

}
