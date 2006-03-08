package com.cinnamonbob.acceptance;

import com.cinnamonbob.core.util.RandomUtils;

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

    public void testCreateProject()
    {
        // navigate to project panel.
        beginAt("/");
        clickLinkWithText("projects");
        assertLinkPresentWithText("add new project");

        clickLinkWithText("add new project");
        assertFormPresent("project.basics");

        String projectName = "test project " + RandomUtils.randomString(3);

        setWorkingForm("project.basics");
        setFormElement("name", projectName);
        setFormElement("description", "this is a test project created by the automated project wizard acceptance test.");
        setFormElement("scm", "cvs");
        setFormElement("type", "custom");
        submit("next");

        assertFormPresent("cvs.setup");
        setWorkingForm("cvs.setup");
        setFormElement("cvs.root", "/local");
        setFormElement("cvs.module", "module");
        submit("next");

        assertFormPresent("custom.setup");
        assertFormElementNotEmpty("details.bobFileName");
        submit("next");

        // assert that all of the expected tables have the expected data.
        assertTablePresent("project.basics");
        assertTableRowsEqual("project.basics", 1, new String[][]{
                new String[]{"name", projectName},
                new String[]{"description", "this is a test project created by the automated project wizard acceptance test."}
        });

        assertTablePresent("project.specifics");
        assertTableRowsEqual("project.specifics", 1, new String[][]{
                new String[]{"type", "custom"},
                new String[]{"cinnabo file", "bob.xml"}
        });

        assertTablePresent("project.scm");
        assertTableRowsEqual("project.scm", 1, new String[][]{
                new String[]{"type", "cvs"},
                new String[]{"location", "/local [module]"}
        });

        assertTablePresent("project.cleanup");
        assertTableRowsEqual("project.cleanup", 1, new String[][]{
                new String[]{"working directories deleted", "after 30 days"},
                new String[]{"build results deleted", "never"}
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
                new String[]{"name", "type", "spec", "actions"},
                new String[]{projectName + " scm trigger", "event", "default", "delete"},
                new String[]{"add new trigger", "add new trigger", "add new trigger", "add new trigger"}
        });


    }
}
