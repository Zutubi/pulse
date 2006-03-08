package com.cinnamonbob.acceptance;

import com.cinnamonbob.core.util.RandomUtils;

/**
 * <class-comment/>
 */
public class ProjectAcceptanceTest extends BaseAcceptanceTest
{
    private String projectName;

    private static final String FO_PROJECT_BASICS_EDIT = "project.edit";
    private static final String PROJECT_BASICS_EDIT_NAME = "project.name";
    private static final String PROJECT_BASICS_EDIT_DESCRIPTION = "project.description";

    public ProjectAcceptanceTest()
    {
    }

    public ProjectAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // create project that will be used during this set of tests.
        login("admin", "admin");

        // navigate to the create project wizard.
        // fill in the form details.
        clickLinkWithText("projects");
        clickLinkWithText("add new project");

        projectName = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(projectName, "", "cvs", "custom");
        submitCvsSetupForm("/local", "module", "", "");
        submitCustomSetupForm("bob.xml");
        assertTablePresent("project.basics");
    }

    protected void tearDown() throws Exception
    {

        super.tearDown();
    }

    public void testEditProjectBasics()
    {
        // not yet implemented.
        assertProjectBasicsTable(projectName, "");

        assertLinkPresent("project.basics.edit");
        clickLink("project.basics.edit");

        // assert the initial values are as expected.
        assertProjectBasicsEditFormElements(projectName, "");

        // check simple edit.
        setFormElement(PROJECT_BASICS_EDIT_NAME, "a temporary name");
        setFormElement(PROJECT_BASICS_EDIT_DESCRIPTION, "a description for this project.");
        submit("save");

        // assert that the changes just made have been persisted.
        assertProjectBasicsTable("a temporary name", "a description for this project.");

        clickLink("project.basics.edit");
        assertProjectBasicsEditFormElements("a temporary name", "a description for this project.");

        // check validation of field input - name required.
        setFormElement(PROJECT_BASICS_EDIT_NAME, "");
        submit("save");

        assertTextPresent("required");
        assertProjectBasicsEditFormElements("", "a description for this project.");

        // check cancel works.
        setFormElement(PROJECT_BASICS_EDIT_NAME, projectName);
        submit("cancel");

        // assert that the changes just made have been persisted.
        assertProjectBasicsTable("a temporary name", "a description for this project.");

        // change the name back.
        clickLink("project.basics.edit");
        assertProjectBasicsEditFormElements("a temporary name", "a description for this project.");

        setWorkingForm(FO_PROJECT_BASICS_EDIT);
        setFormElement(PROJECT_BASICS_EDIT_NAME, projectName);
        submit("save");

        assertProjectBasicsTable(projectName, "a description for this project.");

        // check that we can save without making any changes to the form.
        clickLink("project.basics.edit");
        assertProjectBasicsEditFormElements(projectName, "a description for this project.");

        setWorkingForm(FO_PROJECT_BASICS_EDIT);
        submit("save");

        assertFormNotPresent(FO_PROJECT_BASICS_EDIT);
        assertProjectBasicsTable(projectName, "a description for this project.");
    }

    public void testEditBasicsValidationProjectNameUnique()
    {
        //check that we can not change the name of the project to an already in use project name.
        // create a new project.
        clickLinkWithText("projects");
        clickLinkWithText("add new project");

        String newProject = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(newProject, "", "cvs", "custom");
        submitCvsSetupForm("/local", "module", "", "");
        submitCustomSetupForm("bob.xml");

        clickLink("project.basics.edit");
        setWorkingForm(FO_PROJECT_BASICS_EDIT);
        setFormElement(PROJECT_BASICS_EDIT_NAME, projectName);
        submit("save");

        // assert that we are still on the edit basics page
        assertFormPresent(FO_PROJECT_BASICS_EDIT);
        // assert the error message.
        assertTextPresent("select a different project");
    }

    public void testEditCustomSpecifics()
    {
        // verify what we have to start with.
        assertProjectSpecificsTable("custom", "bob.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        // assert that the form is pre populated with the expected data.
        assertProjectSpecificsFormElements("bob.xml");

        setFormElement("details.bobFileName", "custom.xml");
        submit("save");

        // assert that the data has been updated.
        assertProjectSpecificsTable("custom", "custom.xml");
    }

    public void testEditCustomSpecificsValidation()
    {
        assertProjectSpecificsTable("custom", "bob.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        assertProjectSpecificsFormElements("bob.xml");

        setFormElement("details.bobFileName", "");
        submit("save");

        assertTextPresent("required");
        assertProjectSpecificsFormElements("");
    }

    public void testEditCustomSpecificsCancel()
    {
        // test the editing of custom specifics.
        assertProjectSpecificsTable("custom", "bob.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        assertProjectSpecificsFormElements("bob.xml");

        setFormElement("details.bobFileName", "custom");
        submit("cancel");

        assertProjectSpecificsTable("custom", "bob.xml");
    }

    public void testEditScm()
    {
        // not yet implemented.
    }

    public void testEditCleanupPolicy()
    {
        // not yet implemented.
    }

    public void testAddNewTrigger()
    {
        // not yet implemented.
    }

    public void testDeleteTrigger()
    {
        // not yet implemented.
    }

    public void testAddNewBuildSpec()
    {
        // not yet implemented.
    }

    public void testDeleteBuildSpec()
    {
        // not yet implemented.

        // - delete build spec with associated trigger - ensure trigger deleted.
    }

    public void testEditBuildSpec()
    {
        // not yet implemented.
    }

    public void testDeleteProject()
    {
        // not yet implemented.
    }

    private void assertProjectSpecificsTable(String type, String file)
    {
        assertTablePresent("project.specifics");
        assertTableRowsEqual("project.specifics", 1, new String[][]{
                new String[]{"type", type},
                new String[]{"cinnabo file", file}
        });
    }

    private void assertProjectSpecificsFormElements(String file)
    {
        assertFormPresent("custom.edit");
        setWorkingForm("custom.edit");
        assertFormElementEquals("details.bobFileName", file);
    }

    private void assertProjectBasicsEditFormElements(String name, String description)
    {
        assertFormPresent(FO_PROJECT_BASICS_EDIT);
        setWorkingForm(FO_PROJECT_BASICS_EDIT);
        assertFormElementEquals(PROJECT_BASICS_EDIT_NAME, name);
        assertFormElementEquals(PROJECT_BASICS_EDIT_DESCRIPTION, description);
    }

    private void assertProjectBasicsTable(String name, String description)
    {
        assertTablePresent("project.basics");
        assertTableRowsEqual("project.basics", 1, new String[][]{
                new String[]{"name", name},
                new String[]{"description", description}
        });
    }

}