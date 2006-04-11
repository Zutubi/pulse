package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.util.RandomUtils;

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

    public void testCreateCustomProject()
    {
        // navigate to project panel.
        beginAt("/");
        clickLinkWithText("projects");
        assertLinkPresentWithText("add new project");

        clickLinkWithText("add new project");
        assertFormPresent(FO_PROJECT_BASICS);

        String projectName = "test project " + RandomUtils.randomString(3);

        submitProjectBasicsForm(projectName, "this is a test project created by the automated project wizard acceptance test.", "http://wizard/url", "cvs", "custom");

        submitCvsSetupForm("/local", "module", "", "");

        assertFormElementNotEmpty(CUSTOM_SETUP_FILE);
        submitCustomSetupForm("bob.xml");

        // assert that all of the expected tables have the expected data.
        assertTablePresent("project.basics");
        assertTableRowsEqual("project.basics", 1, new String[][]{
                new String[]{"name", projectName},
                new String[]{"description", "this is a test project created by the automated project wizard acceptance test."},
                new String[]{"url", "http://wizard/url"}
        });

        assertTablePresent("project.specifics");
        assertTableRowsEqual("project.specifics", 1, new String[][]{
                new String[]{"type", "custom"},
                new String[]{"cinnabo file", "bob.xml"}
        });

        assertTablePresent("project.scm");
        assertTableRowsEqual("project.scm", 1, new String[][]{
                new String[]{"type", "cvs"},
                new String[]{"location", "/local[module]"}
        });

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

        // ensure that it appears in your list of projects.
        clickLinkWithText("projects");
        assertLinkPresentWithText(projectName);
    }

    public void testProjectNameUnique()
    {
        clickLinkWithText("projects");
        clickLink("project.add");

        String projectName = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(projectName, "", "", "cvs", "custom");
        submitCvsSetupForm("/local", "module", "", "");
        submitCustomSetupForm("bob.xml");
        assertTablePresent("project.basics");

        clickLinkWithText("projects");
        clickLink("project.add");

        submitProjectBasicsForm(projectName, "", "", "cvs", "custom");
        assertFormPresent(FO_PROJECT_BASICS);
    }
}
