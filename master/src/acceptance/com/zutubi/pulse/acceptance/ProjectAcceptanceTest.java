package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.*;
import com.zutubi.pulse.core.util.RandomUtils;

/**
 * <class-comment/>
 */
public class ProjectAcceptanceTest extends BaseAcceptanceTest
{
    private String projectName;
    private static final String DESCRIPTION = "test description";
    private static final String URL = "http://test/url";
    private static final String CRON_TRIGGER_NAME = "cron-trigger-name";
    private static final String CRON_STRING = "0 0 0 * * ?";
    private static final String SPEC_NAME = "spec-name";
    private static final String RECIPE_NAME = "recipe-name";

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
        submitProjectBasicsForm(projectName, DESCRIPTION, URL, "cvs", "custom");
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
        final String TEST_DESCRIPTION = "a description for this project.";
        final String TEST_NAME = "a temporary name";
        final String TEST_URL = "http://and/a/url";

        ProjectBasicForm form = new ProjectBasicForm(tester);

        assertProjectBasicsTable(projectName, DESCRIPTION, URL);

        assertLinkPresent("project.basics.edit");
        clickLink("project.basics.edit");

        // assert the initial values are as expected.
        form.assertFormElements(projectName, DESCRIPTION, URL);

        // check simple edit.
        form.saveFormElements(TEST_NAME, TEST_DESCRIPTION, TEST_URL);

        // assert that the changes just made have been persisted.
        assertProjectBasicsTable(TEST_NAME, TEST_DESCRIPTION, TEST_URL);

        clickLink("project.basics.edit");
        form.assertFormElements(TEST_NAME, TEST_DESCRIPTION, TEST_URL);

        // check validation of field input - name required.
        form.saveFormElements("", null, null);
        assertTextPresent("required");
        form.assertFormElements("", TEST_DESCRIPTION, TEST_URL);

        // check cancel works.
        form.cancelFormElements(projectName, null, null);
        assertProjectBasicsTable(TEST_NAME, TEST_DESCRIPTION, TEST_URL);

        // change the name back.
        clickLink("project.basics.edit");
        form.assertFormElements(TEST_NAME, TEST_DESCRIPTION, TEST_URL);
        form.saveFormElements(projectName, null, null);

        assertProjectBasicsTable(projectName, TEST_DESCRIPTION, TEST_URL);

        // check that we can save without making any changes to the form.
        clickLink("project.basics.edit");
        form.assertFormElements(projectName, TEST_DESCRIPTION, TEST_URL);
        form.saveFormElements(null, null, null);
        form.assertFormNotPresent();

        assertProjectBasicsTable(projectName, TEST_DESCRIPTION, TEST_URL);
    }

    public void testEditBasicsValidationProjectNameUnique()
    {
        ProjectBasicForm form = new ProjectBasicForm(tester);

        //check that we can not change the name of the project to an already in use project name.
        // create a new project.
        clickLinkWithText("projects");
        clickLinkWithText("add new project");

        String newProject = "project " + RandomUtils.randomString(5);
        submitProjectBasicsForm(newProject, "test description", "http://test/url", "cvs", "custom");
        submitCvsSetupForm("/local", "module", "", "");
        submitCustomSetupForm("bob.xml");

        clickLink("project.basics.edit");
        form.saveFormElements(projectName, null, null);

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
        CvsForm.Edit form = new CvsForm.Edit(tester);

        assertProjectCvsTable("cvs", "/local[module]");

        assertLinkPresent("project.scm.edit");
        clickLink("project.scm.edit");

        form.assertFormElements("/local", "module", "", "", "", "");

        // change the root and module, verify updates as expected.
        form.saveFormElements("/loc", "mod", "", "", "1", "1");
        assertProjectCvsTable("cvs", "/loc[mod]");

        // check the form again to ensure that the path has been saved.
        clickLink("project.scm.edit");
        form.assertFormElements("/loc", "mod", "", "", "1", "1");

    }

    public void testEditScmPasswordValue()
    {
        CvsForm.Edit form = new CvsForm.Edit(tester);

        clickLink("project.scm.edit");

        // check that we can set a password and that it is persisted
        // and visible on subsequent edit.
        form.saveFormElements("/loc", "mod", "password", "", "", "");
        assertProjectCvsTable("cvs", "/loc[mod]");
        clickLink("project.scm.edit");
        form.assertFormElements("/loc", "mod", "password", "", "", "");
    }

    public void testEditScmValidation()
    {
        CvsForm.Edit form = new CvsForm.Edit(tester);
        clickLink("project.scm.edit");
        form.assertFormElements("/local", "module", "", "", "", "");

        // check that the cvs root is a required field.
        form.saveFormElements("", "module", "", "", "", "");
        assertTextPresent("required");
        form.assertFormElements("", "module", "", "", "", "");

        // check that the cvs module is a required field.
        form.saveFormElements("/local", "", "", "", "", "");
        assertTextPresent("required");
        form.assertFormElements("/local", "", "", "", "", "");

        // check that the cvs root is validated correctly.
        form.saveFormElements("an invalid arg", "mod", "", "", "", "");
        assertTextPresent("required");
        form.assertFormElements("an invalid arg", "mod", "", "", "", "");

        // check the validation on the minutes field
        form.saveFormElements("/local", "", "", "", "a", "");
        assertTextPresent("required");
        form.assertFormElements("/local", "", "",  "", "a", "");

        form.saveFormElements("/local", "", "", "", "-1", "");
        assertTextPresent("specify a positive");
        form.assertFormElements("/local", "", "", "", "-1", "");

        // check the validation on the seconds field
        form.saveFormElements("/local", "", "", "", "", "a");
        assertTextPresent("required");
        form.assertFormElements("/local", "", "", "", "", "a");

        form.saveFormElements("/local", "", "", "", "", "-1");
        assertTextPresent("specify a positive");
        form.assertFormElements("/local", "", "", "", "", "-1");
    }

    public void testAddCleanupPolicy()
    {
        CleanupRuleForm form = new CleanupRuleForm(tester, "addCleanupRule");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });

        clickLinkWithText("add new cleanup rule");

        form.assertFormElements(null, null, "10", "builds");
        assertOptionsEqual(CleanupRuleForm.WORK_DIR_ONLY, new String[]{ "whole build results", "working directories only" });
        assertSelectionValues(CleanupRuleForm.WORK_DIR_ONLY, new String[]{ "true" });
        assertOptionsEqual(CleanupRuleForm.STATE_NAMES, new String[]{ "error", "failure", "success"});
        assertSelectionValues(CleanupRuleForm.STATE_NAMES, new String[] {});
        assertRadioOptionPresent(CleanupRuleForm.BUILD_UNITS, "days");

        tester.selectOption(CleanupRuleForm.WORK_DIR_ONLY, "whole build results");
        selectMultipleValues(CleanupRuleForm.STATE_NAMES, new String[] { "ERROR", "SUCCESS" });
        form.saveFormElements(null, null, "2", "days");

        assertProjectCleanupTable(new String[][] {
                getCleanupRow(true, "any", "10 builds"),
                getCleanupRow(false, "error, success", "2 days")
        });
    }

    public void testAddCleanupPolicyCancel()
    {
        CleanupRuleForm form = new CleanupRuleForm(tester, "addCleanupRule");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });

        clickLinkWithText("add new cleanup rule");

        form.assertFormElements(null, null, "10", "builds");
        tester.selectOption(CleanupRuleForm.WORK_DIR_ONLY, "whole build results");
        selectMultipleValues(CleanupRuleForm.STATE_NAMES, new String[] { "ERROR", "SUCCESS" });
        form.cancelFormElements(null, null, "2", "days");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });
    }

    public void testAddCleanupPolicyValidation()
    {
        CleanupRuleForm form = new CleanupRuleForm(tester, "addCleanupRule");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });

        clickLinkWithText("add new cleanup rule");

        form.saveFormElements(null, null, "0", "days");
        form.assertFormPresent();
        assertTextPresent("limit must be a positive value");
    }

    public void testEditCleanupPolicyUpdate()
    {
        CleanupRuleForm form = new CleanupRuleForm(tester, "editCleanupRule");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });

        clickLinkWithText("edit", 3);

        form.assertFormElements(null, null, "10", "builds");
        assertOptionsEqual(CleanupRuleForm.WORK_DIR_ONLY, new String[]{ "whole build results", "working directories only" });
        assertSelectionValues(CleanupRuleForm.WORK_DIR_ONLY, new String[]{ "true" });
        assertOptionsEqual(CleanupRuleForm.STATE_NAMES, new String[]{ "error", "failure", "success"});
        assertSelectionValues(CleanupRuleForm.STATE_NAMES, new String[] {});
        assertRadioOptionPresent(CleanupRuleForm.BUILD_UNITS, "days");

        tester.selectOption(CleanupRuleForm.WORK_DIR_ONLY, "whole build results");
        selectMultipleValues(CleanupRuleForm.STATE_NAMES, new String[] { "ERROR", "SUCCESS" });
        form.saveFormElements(null, null, "2", "days");

        assertProjectCleanupTable(new String[][] { getCleanupRow(false, "error, success", "2 days") });

        // Check form is correctly populated again
        clickLinkWithText("edit", 3);

        form.assertFormElements(null, null, "2", "days");
        assertSelectionValues(CleanupRuleForm.WORK_DIR_ONLY, new String[]{ "false" });
        assertSelectionValues(CleanupRuleForm.STATE_NAMES, new String[] { "ERROR", "SUCCESS"});
    }

    public void testEditCleanupPolicyCancel()
    {
        CleanupRuleForm form = new CleanupRuleForm(tester, "editCleanupRule");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });

        clickLinkWithText("edit", 3);

        form.assertFormElements(null, null, "10", "builds");
        tester.selectOption(CleanupRuleForm.WORK_DIR_ONLY, "whole build results");
        selectMultipleValues(CleanupRuleForm.STATE_NAMES, new String[] { "ERROR", "SUCCESS" });
        form.cancelFormElements(null, null, "2", "days");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });
    }

    public void testEditCleanupPolicyValidation()
    {
        CleanupRuleForm form = new CleanupRuleForm(tester, "editCleanupRule");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });

        clickLinkWithText("edit", 3);

        form.saveFormElements(null, null, "0", "days");
        form.assertFormPresent();
        assertTextPresent("limit must be a positive value");
    }

    public void testDeleteCleanupPolicy()
    {
        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });
        clickLinkWithText("delete", 0);
        assertProjectCleanupTable(null);
    }

    public void testAddNewBuildSpec()
    {
        BuildSpecForm form = new BuildSpecForm(tester, true);

        assertProjectBuildSpecTable(new String[][]{
                createBuildSpecRow("default", "[default]", "[never]")
        });

        assertAndClick("project.buildspec.add");
        form.assertFormPresent();
        form.saveFormElements(SPEC_NAME, RECIPE_NAME, "true", "100");

        assertProjectBuildSpecTable(new String[][]{
                createBuildSpecRow("default", "[default]", "[never]"),
                createBuildSpecRow(SPEC_NAME, RECIPE_NAME, "100 minutes")
        });
    }

    public void testAddBuildSpecValidation()
    {
        BuildSpecForm form = new BuildSpecForm(tester, true);

        assertProjectBuildSpecTable(new String[][]{
                createBuildSpecRow("default", "[default]", "[never]")
        });

        assertAndClick("project.buildspec.add");
        form.assertFormPresent();
        form.saveFormElements("", "", "true", "-100");
        form.assertFormPresent();

        assertTextPresent("name is required");
        assertTextPresent("Timeout must be a positive value");
    }

    public void testAddBuildSpecDuplicate()
    {
        BuildSpecForm form = new BuildSpecForm(tester, true);

        assertAndClick("project.buildspec.add");
        form.assertFormPresent();
        form.saveFormElements("default", "", "true", "100");
        form.assertFormPresent();

        assertTextPresent("'default' already exists");
    }

    public void testEditBuildSpec()
    {
        testAddNewBuildSpec();
        assertAndClick("edit_" + SPEC_NAME);

        BuildSpecForm form = new BuildSpecForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements(SPEC_NAME, RECIPE_NAME, "true", "100");
        form.saveFormElements(SPEC_NAME + "edited", RECIPE_NAME + "edited", null, null);

        assertProjectBuildSpecTable(new String[][]{
                createBuildSpecRow("default", "[default]", "[never]"),
                createBuildSpecRow(SPEC_NAME + "edited", RECIPE_NAME + "edited", "100 minutes")
        });
    }

    public void testEditBuildSpecValidation()
    {
        testAddNewBuildSpec();
        assertAndClick("edit_" + SPEC_NAME);

        BuildSpecForm form = new BuildSpecForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements(SPEC_NAME, RECIPE_NAME, "true", "100");
        form.saveFormElements("", "", "true", "-100");
        form.assertFormPresent();

        assertTextPresent("name is required");
        assertTextPresent("Timeout must be a positive value");
    }

    public void testEditBuildSpecDuplicate()
    {
        testAddNewBuildSpec();
        assertAndClick("edit_" + SPEC_NAME);

        BuildSpecForm form = new BuildSpecForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements(SPEC_NAME, RECIPE_NAME, "true", "100");
        form.saveFormElements("default", "", "true", "100");
        form.assertFormPresent();

        assertTextPresent("'default' already exists");
    }

    public void testAddNewTrigger()
    {
        assertProjectTriggerTable(new String[][]{
                getTriggerRow(projectName + " scm trigger", "event", "default"),
        });

        assertLinkPresent("project.trigger.add");
        clickLink("project.trigger.add");

        // check form is available.
        assertFormPresent("trigger.type");
        // default trigger type.
        // default specification.
        setFormElement("name", CRON_TRIGGER_NAME);
        submit("next");

        // check form is available.
        assertFormPresent("trigger.cron.create");
        setWorkingForm("trigger.cron.create");
        setFormElement("cron", CRON_STRING);
        submit("next");

        assertProjectTriggerTable(new String[][]{
                getTriggerRow(projectName + " scm trigger", "event", "default"),
                getTriggerRow(CRON_TRIGGER_NAME, "cron", "default"),
        });
    }

    public void testDeleteTrigger()
    {
        String triggerName = projectName + " scm trigger";
        assertProjectTriggerTable(new String[][]{
                getTriggerRow(triggerName, "event", "default"),
        });

        assertLinkPresent("delete_" + triggerName);
        clickLink("delete_" + triggerName);

        assertProjectTriggerTable(new String[][]{});
    }

    public void testCreateTriggerValidation()
    {
        // ensure that the name remains unique.
        String triggerName = projectName + " scm trigger"; // this is the default trigger name.
        assertProjectTriggerTable(new String[][]{
                getTriggerRow(triggerName, "event", "default")
        });

        clickLink("project.trigger.add");
        assertFormPresent("trigger.type");
        // check that we can not create a trigger with an existing name.
        setFormElement("name", triggerName);
        // go with the defaults.
        submit("next");

        assertFormPresent("trigger.type");
        // assert text present..

        assertOptionValuesEqual("type", new String[]{"cron", "monitor"});

        // use some random name.
        setFormElement("name", "trigger " + RandomUtils.randomString(4));
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

        submit("previous");
    }

    private CronTriggerEditForm editCronTriggerHelper()
    {
        // First ensure we have a cron trigger and two build specs
        testAddNewTrigger();
        testAddNewBuildSpec();

        CronTriggerEditForm form = new CronTriggerEditForm(tester);
        clickLink(getEditId(CRON_TRIGGER_NAME));

        form.assertFormPresent();
        form.assertFormElements(CRON_TRIGGER_NAME, "default", CRON_STRING);
        assertOptionValuesEqual("specification", new String[]{"default", SPEC_NAME});
        return form;
    }

    public void testEditCronTrigger()
    {
        CronTriggerEditForm form = editCronTriggerHelper();
        form.saveFormElements("new name", SPEC_NAME, "0 0 1 * * ?");

        assertProjectTriggerTable(new String[][]{
                getTriggerRow(projectName + " scm trigger", "event", "default"),
                getTriggerRow("new name", "cron", SPEC_NAME)
        });

        clickLink(getEditId("new name"));

        form.assertFormPresent();
        form.assertFormElements("new name", SPEC_NAME, "0 0 1 * * ?");
    }

    public void testEditCronTriggerCancel()
    {
        CronTriggerEditForm form = editCronTriggerHelper();
        form.cancelFormElements("new name", SPEC_NAME, "0 0 1 * * ?");

        assertProjectTriggerTable(new String[][]{
                getTriggerRow(projectName + " scm trigger", "event", "default"),
                getTriggerRow(CRON_TRIGGER_NAME, "cron", "default"),
        });
    }

    public void testEditCronTriggerValidation()
    {
        CronTriggerEditForm form = editCronTriggerHelper();

        // Try empty name
        form.saveFormElements("", SPEC_NAME, "0 0 1 * * ?");
        form.assertFormPresent();
        assertTextPresent("name is required");

        // Try an empty cron string
        form.saveFormElements("name", SPEC_NAME, "");
        form.assertFormPresent();
        assertTextPresent("cron expression is required");

        // Try an invalid cron string
        form.saveFormElements("name", SPEC_NAME, "0 0 1 * *");
        form.assertFormPresent();
        assertTextPresent("Unexpected end of expression");
    }

    private EventTriggerEditForm editEventTriggerHelper()
    {
        // First ensure we have a two build specs
        testAddNewBuildSpec();

        EventTriggerEditForm form = new EventTriggerEditForm(tester);
        assertAndClick(getEditId(projectName + " scm trigger"));

        form.assertFormPresent();
        form.assertFormElements(projectName + " scm trigger", "default");
        assertOptionValuesEqual("specification", new String[]{"default", SPEC_NAME});
        return form;
    }

    public void testEditEventTrigger()
    {
        EventTriggerEditForm form = editEventTriggerHelper();
        form.saveFormElements("new name", SPEC_NAME);

        assertProjectTriggerTable(new String[][]{
                getTriggerRow("new name", "event", SPEC_NAME),
        });
    }

    public void testEditEventTriggerCancel()
    {
        EventTriggerEditForm form = editEventTriggerHelper();
        form.cancelFormElements("new name", SPEC_NAME);

        assertProjectTriggerTable(new String[][]{
                getTriggerRow(projectName + " scm trigger", "event", "default"),
        });
    }

    public void testEditEventTriggerValidation()
    {
        EventTriggerEditForm form = editEventTriggerHelper();

        // Try empty name
        form.saveFormElements("", SPEC_NAME);
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

/*

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

    private void assertProjectBasicsTable(String name, String description, String url)
    {
        assertTablePresent("project.basics");
        assertTableRowsEqual("project.basics", 1, new String[][]{
                new String[]{"name", name},
                new String[]{"description", description},
                new String[]{"url", url}
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

    private void assertProjectCleanupTable(String[][] rows)
    {
        assertTablePresent("project.cleanup");
        if(rows != null)
        {
            String[][] allRows = new String[rows.length + 2][5];
            allRows[0] = new String[]{"what", "with state(s)", "retain for up to", "actions", "actions"};
            for(int i = 1; i <= rows.length; i++)
            {
                allRows[i] = rows[i - 1];
            }
            allRows[rows.length + 1] = new String[]{"add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule"};
            assertTableRowsEqual("project.cleanup", 1, allRows);
        }
        else
        {
            assertTableRowsEqual("project.cleanup", 1, new String[][] {
                    new String[] { "what", "with state(s)", "retain for up to", "actions"},
                    new String[] { "add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule" }
            });
        }
    }

    private String[] getCleanupRow(boolean workDirsOnly, String states, String limit)
    {
        return new String[]{workDirsOnly ? "working directories" : "whole build results", states, limit, "edit", "delete"};
    }

    private void assertProjectBuildSpecTable(String[][] rows)
    {
        assertTablePresent("project.buildspecs");
        assertTableRowsEqual("project.buildspecs", 2, rows);
    }

    private String[] createBuildSpecRow(String name, String recipe, String timeout)
    {
        return new String[]{name, recipe, timeout, "trigger", "edit", "delete"};
    }

    private void assertProjectTriggerTable(String[][] rows)
    {
        assertTablePresent("project.triggers");
        assertTableRowsEqual("project.triggers", 2, rows);
    }

    private String[] getTriggerRow(String name, String type, String spec)
    {
        return new String[]{name, type, spec, "edit", "delete"};
    }

}