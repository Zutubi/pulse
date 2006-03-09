package com.cinnamonbob.acceptance;

import com.cinnamonbob.core.util.RandomUtils;
import com.cinnamonbob.acceptance.forms.ProjectBasicForm;
import com.cinnamonbob.acceptance.forms.CustomProjectEditForm;
import com.cinnamonbob.acceptance.forms.CvsEditForm;
import com.cinnamonbob.acceptance.forms.CleanupPolicyForm;

/**
 * <class-comment/>
 */
public class ProjectAcceptanceTest extends BaseAcceptanceTest
{
    private String projectName;

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
        ProjectBasicForm form = new ProjectBasicForm(tester);

        // not yet implemented.
        assertProjectBasicsTable(projectName, "");

        assertLinkPresent("project.basics.edit");
        clickLink("project.basics.edit");

        // assert the initial values are as expected.
        form.assertFormElements(projectName, "");

        // check simple edit.
        form.saveFormElements("a temporary name", "a description for this project.");

        // assert that the changes just made have been persisted.
        assertProjectBasicsTable("a temporary name", "a description for this project.");

        clickLink("project.basics.edit");
        form.assertFormElements("a temporary name", "a description for this project.");

        // check validation of field input - name required.
        form.saveFormElements("", null);

        assertTextPresent("required");
        form.assertFormElements("", "a description for this project.");

        // check cancel works.
        form.cancelFormElements(projectName, null);

        // assert that the changes just made have been persisted.
        assertProjectBasicsTable("a temporary name", "a description for this project.");

        // change the name back.
        clickLink("project.basics.edit");
        form.assertFormElements("a temporary name", "a description for this project.");

        form.saveFormElements(projectName, null);

        assertProjectBasicsTable(projectName, "a description for this project.");

        // check that we can save without making any changes to the form.
        clickLink("project.basics.edit");
        form.assertFormElements(projectName, "a description for this project.");

        form.saveFormElements(null, null);

        form.assertFormNotPresent();

        assertProjectBasicsTable(projectName, "a description for this project.");
    }

    public void testEditBasicsValidationProjectNameUnique()
    {
        ProjectBasicForm form = new ProjectBasicForm(tester);

        //check that we can not change the name of the project to an already in use project name.
        // create a new project.
        clickLinkWithText("projects");
        clickLinkWithText("add new project");

        String newProject = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(newProject, "", "cvs", "custom");
        submitCvsSetupForm("/local", "module", "", "");
        submitCustomSetupForm("bob.xml");

        clickLink("project.basics.edit");
        form.saveFormElements(projectName, null);

        // assert that we are still on the edit basics page
        form.assertFormPresent();

        // assert the error message.
        assertTextPresent("select a different project");
    }

    public void testEditCustomSpecifics()
    {
        CustomProjectEditForm form = new CustomProjectEditForm(tester);

        // verify what we have to start with.
        assertProjectSpecificsTable("custom", "bob.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        // assert that the form is pre populated with the expected data.
        form.assertFormElements("bob.xml");

        form.saveFormElements("custom.xml");

        // assert that the data has been updated.
        assertProjectSpecificsTable("custom", "custom.xml");
    }

    public void testEditCustomSpecificsValidation()
    {
        CustomProjectEditForm form = new CustomProjectEditForm(tester);

        assertProjectSpecificsTable("custom", "bob.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        form.assertFormElements("bob.xml");

        form.saveFormElements("");

        assertTextPresent("required");
        form.assertFormElements("");
    }

    public void testEditCustomSpecificsCancel()
    {
        CustomProjectEditForm form = new CustomProjectEditForm(tester);

        // test the editing of custom specifics.
        assertProjectSpecificsTable("custom", "bob.xml");

        assertLinkPresent("project.specifics.edit");
        clickLink("project.specifics.edit");

        form.assertFormElements("bob.xml");

        form.cancelFormElements("custom.xml");

        assertProjectSpecificsTable("custom", "bob.xml");
    }

    public void testEditScm()
    {
        CvsEditForm form = new CvsEditForm(tester);

        assertProjectCvsTable("cvs", "/local [module]");

        assertLinkPresent("project.scm.edit");
        clickLink("project.scm.edit");

        form.assertFormElements("/local", "module", "", "");

        // change the root and module, verify updates as expected.
        form.saveFormElements("/loc", "mod", "", "path");
        assertProjectCvsTable("cvs", "/loc [mod]");

        // check the form again to ensure that the path has been saved.
        clickLink("project.scm.edit");
        form.assertFormElements("/loc", "mod", "", "path");
    }

    public void testEditScmValidation()
    {
        CvsEditForm form = new CvsEditForm(tester);
        clickLink("project.scm.edit");
        form.assertFormElements("/local", "module", "", "");

        // check that the cvs root is a required field.
        form.saveFormElements("", "module", "", "");
        assertTextPresent("required");
        form.assertFormElements("", "module", "", "");

        // check that the cvs module is a required field.
        form.saveFormElements("/local", "", "", "");
        assertTextPresent("required");
        form.assertFormElements("/local", "", "", "");

        // check that the cvs root is validated correctly.
        form.saveFormElements("an invalid arg", "mod", "", "");
        assertTextPresent("required");
        form.assertFormElements("an invalid arg", "mod", "", "");
    }

    public void testEditCleanupPolicyUpdate()
    {
        CleanupPolicyForm form = new CleanupPolicyForm(tester);
        assertProjectCleanupTable("after 30 days", "never");

        clickLink("project.cleanup.edit");

        form.assertFormElements("true", "30", "false", "0");
        // check disabling both.
        form.saveFormElements("false", null, "false", null);
        assertProjectCleanupTable("never", "never");

        clickLink("project.cleanup.edit");
        form.assertFormElements("false", "0", "false", "0");

        form.saveFormElements(null, null, null, null);
        assertProjectCleanupTable("never", "never");
    }

    public void testEditCleanupPolicyCancel()
    {
        CleanupPolicyForm form = new CleanupPolicyForm(tester);
        assertProjectCleanupTable("after 30 days", "never");

        clickLink("project.cleanup.edit");

        form.assertFormElements("true", "30", "false", "0");
        form.cancelFormElements("false", null, "false", null);

        assertProjectCleanupTable("after 30 days", "never");
    }

    public void testEditCleanupPolicyValidation()
    {
        // check that positive values only
        CleanupPolicyForm form = new CleanupPolicyForm(tester);
        clickLink("project.cleanup.edit");

        // check 0 value.
        form.assertFormElements("true", "30", "false", "0");
        form.saveFormElements("true", "0", null, null);
        assertTextPresent("positive");

        // check negative value
        form.assertFormElements("true", "0", "false", "0");
        form.saveFormElements("true", "-3", null, null);
        assertTextPresent("positive");

        // check no value
        form.assertFormElements("true", "-3", "false", "0");
        form.saveFormElements("true", "", null, null);
        assertTextPresent("positive");

        // repeat the above tests for the result cleanup field.

        // check 0 value.
        form.assertFormElements("true", "0", "false", "0");
        form.saveFormElements("false", "0", "true", "0");
        assertTextPresent("positive");

        // check negative value
        form.assertFormElements("false", "0", "true", "0");
        form.saveFormElements(null, null, "true", "-3");
        assertTextPresent("positive");

        // check no value
        form.assertFormElements("false", "0", "true", "-3");
        form.saveFormElements(null, null, "true", "");
        assertTextPresent("positive");

        // ensure that working directories are cleaned up at least as frequently as results.
        form.saveFormElements("true", "4", "true", "1");
        assertTextPresent("least as frequently");
        form.assertFormElements("true", "4", "true", "1");

    }

    public void testAddNewTrigger()
    {
        assertProjectTriggerTable(new String[][]{
                new String[]{projectName + " scm trigger", "event", "default", "delete"}
        });

        assertLinkPresent("project.trigger.add");
        clickLink("project.trigger.add");

        // check form is available.
        assertFormPresent("trigger.type");
        // default trigger type.
        // default specification.
        setFormElement("name", "trigger name");
        submit("next");

        // check form is available.
        assertFormPresent("trigger.cron.create");
        setWorkingForm("trigger.cron.create");
        setFormElement("cron", "0 0 0 * * ?");
        submit("next");

        assertProjectTriggerTable(new String[][]{
                new String[]{projectName + " scm trigger", "event", "default", "delete"},
                new String[]{"trigger name", "cron", "default", "delete"}
        });
    }

    public void testDeleteTrigger()
    {
        String triggerName = projectName + " scm trigger";
        assertProjectTriggerTable(new String[][]{
                new String[]{triggerName, "event", "default", "delete"}
        });

        assertLinkPresent("delete_" + triggerName);
        clickLink("delete_" + triggerName);

        assertProjectTriggerTable(new String[][]{});
    }

    public void testCreateTriggerValidation()
    {
        // ensure that the name remains unique.
        String triggerName = projectName + " scm trigger";
        assertProjectTriggerTable(new String[][]{
                new String[]{triggerName, "event", "default", "delete"}
        });

        clickLink("project.trigger.add");
        assertFormPresent("trigger.type");
        setFormElement("name", triggerName);
        // go with the defaults.
        submit("next");

        // check that we can not create a trigger with an existing name.
        assertFormPresent("trigger.type");
        // assert text present..

        // use some random name.
        setFormElement("name", "trigger "+RandomUtils.randomString(4));
        submit("next");

        // check form is available.
        assertFormPresent("trigger.cron.create");
        setWorkingForm("trigger.cron.create");
        setFormElement("cron", "0");
        submit("next");

        // invalid cron string.
        assertFormPresent("trigger.cron.create");
        // assert error text present.
        setWorkingForm("trigger.cron.create");
        setFormElement("cron", "");
        submit("next");

        // cron string required.
        assertFormPresent("trigger.cron.create");
        assertTextPresent("required");

        // ensure that you can only create one event trigger.
        submit("previous");
        assertOptionsEqual("type", new String[]{"cron trigger"});
    }

/*
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
*/

    private void assertProjectSpecificsTable(String type, String file)
    {
        assertTablePresent("project.specifics");
        assertTableRowsEqual("project.specifics", 1, new String[][]{
                new String[]{"type", type},
                new String[]{"cinnabo file", file}
        });
    }

    private void assertProjectBasicsTable(String name, String description)
    {
        assertTablePresent("project.basics");
        assertTableRowsEqual("project.basics", 1, new String[][]{
                new String[]{"name", name},
                new String[]{"description", description}
        });
    }

    private void assertProjectCvsTable(String type, String location)
    {
        assertTablePresent("project.scm");
        assertTableRowsEqual("project.scm", 1, new String[][]{
                new String[]{"type", type},
                new String[]{"location", location}
        });
    }

    private void assertProjectCleanupTable(String work, String results)
    {
        assertTablePresent("project.cleanup");
        assertTableRowsEqual("project.cleanup", 1, new String[][]{
                new String[]{"working directories deleted", work},
                new String[]{"build results deleted", results}
        });
    }

    private void assertProjectTriggerTable(String[][] rows)
    {
        assertTablePresent("project.triggers");
        assertTableRowsEqual("project.triggers", 2, rows);
    }
}