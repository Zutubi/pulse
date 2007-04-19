package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.*;
import com.zutubi.util.RandomUtils;

import java.io.IOException;

/**
 * <class-comment/>
 */
public class ProjectAcceptanceTest extends ProjectAcceptanceTestBase
{
    private static final String TRIGGER_NAME = "trigger-name";
    private static final String CRON_STRING = "0 0 0 * * ?";
    private static final String SPEC_NAME = "spec-name";
    private static final String RECIPE_NAME = "recipe-name";
    private static final String STAGE_NAME = "stage-name";
    private static final String NEW_RECIPE = "new-recipe";
    private static final String VERSION_VALUE = "version-value";
    private static final String RESOURCE_NAME = "resource-name";
    private static final String PROPERTY_NAME = "pname";

    public ProjectAcceptanceTest()
    {
        super(Type.ANT);
    }

    public ProjectAcceptanceTest(String name)
    {
        super(name, Type.ANT);
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
        clickLink(Navigation.TAB_PROJECTS);
        assertAndClick(Navigation.Projects.LINK_ADD_PROJECT);

        String newProject = "proect " + RandomUtils.randomString(5);
        submitProjectBasicsForm(newProject, "test description", "http://test/url", "cvs", "versioned");
        submitCvsSetupForm(TEST_CVSROOT, "module", "", "");
        submitVersionedSetupForm("pulse.xml");

        clickLink("project.basics.edit");
        form.saveFormElements(projectName, null, null);

        // assert that we are still on the edit basics page
        form.assertFormPresent();

        // assert the error message.
        assertTextPresent("select a different project");
    }

    public void testEditScm()
    {
        CvsForm.Edit form = new CvsForm.Edit(tester);

        assertProjectCvsTable("cvs", TEST_CVSROOT + "[module]");

        assertLinkPresent("project.scm.edit");
        clickLink("project.scm.edit");

        form.assertFormElements(TEST_CVSROOT, "module", "", "", "", "", "false");

        // change the root and module, verify updates as expected.
        form.saveFormElements("/loc", "mod", "", "", "1", "1", "true");
        assertProjectCvsTable("cvs", "/loc[mod]", "");

        // check the form again to ensure that the path has been saved.
        clickLink("project.scm.edit");
        form.assertFormElements("/loc", "mod", "", "", "1", "1", "true");

        // turn off monitoring.
        form.saveFormElements("/loc", "mod", "", "", "1", "1", "false");
    }

    public void testEditScmPasswordValue()
    {
        CvsForm.Edit form = new CvsForm.Edit(tester);

        clickLink("project.scm.edit");

        // check that we can set a password and that it is persisted
        // and visible on subsequent edit.
        form.saveFormElements("/loc", "mod", "password", "", "", "", "false");
        assertProjectCvsTable("cvs", "/loc[mod]");
        clickLink("project.scm.edit");
        form.assertFormElements("/loc", "mod", "password", "", "", "", "false");
    }

    public void testEditScmValidation()
    {
        CvsForm.Edit form = new CvsForm.Edit(tester);
        clickLink("project.scm.edit");
        form.assertFormElements(TEST_CVSROOT, "module", "", "", "", "", "false");

        // check that the cvs root is a required field.
        form.saveFormElements("", "module", "", "", "", "", "false");
        assertTextPresent("required");
        form.assertFormElements("", "module", "", "", "", "", "false");

        // check that the cvs module is a required field.
        form.saveFormElements(TEST_CVSROOT, "", "", "", "", "", "false");
        assertTextPresent("required");
        form.assertFormElements(TEST_CVSROOT, "", "", "", "", "", "false");

        // check that the cvs root is validated correctly.
        form.saveFormElements("an invalid arg", "mod", "", "", "", "", "false");
        assertTextPresent("required");
        form.assertFormElements("an invalid arg", "mod", "", "", "", "", "false");

        // check the validation on the minutes field
        form.saveFormElements(TEST_CVSROOT, "", "", "", "a", "", "false");
        assertTextPresent("required");
        form.assertFormElements(TEST_CVSROOT, "", "",  "", "a", "", "false");

        form.saveFormElements(TEST_CVSROOT, "", "", "", "-1", "", "false");
        assertTextPresent("specify a positive");
        form.assertFormElements(TEST_CVSROOT, "", "", "", "-1", "", "false");

        // check the validation on the seconds field
        form.saveFormElements(TEST_CVSROOT, "", "", "", "", "a", "false");
        assertTextPresent("required");
        form.assertFormElements(TEST_CVSROOT, "", "", "", "", "a", "false");

        form.saveFormElements(TEST_CVSROOT, "", "", "", "", "-1", "false");
        assertTextPresent("specify a positive");
        form.assertFormElements(TEST_CVSROOT, "", "", "", "", "-1", "false");
    }

    public void testEditScmActiveStatus()
    {
        CvsForm.Edit form = new CvsForm.Edit(tester);
        clickLink("project.scm.edit");
        form.assertFormElements(TEST_CVSROOT, "module", "", "", "", "", "false");
        form.saveFormElements(TEST_CVSROOT, "module", "", "", "", "", "true");
        clickLink("project.scm.edit");
        form.assertFormElements(TEST_CVSROOT, "module", "", "", "", "", "true");
        form.saveFormElements(TEST_CVSROOT, "module", "", "", "", "", "false");

        // verify data in table.
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

        // using the index of the link is brittle, but since the links id
        // contains a reference to the id of the object (which we do not know)
        // there are not many options available.
        clickLinkWithText("edit", 7);

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
        clickLinkWithText("edit", 7);

        form.assertFormElements(null, null, "2", "days");
        assertSelectionValues(CleanupRuleForm.WORK_DIR_ONLY, new String[]{ "false" });
        assertSelectionValues(CleanupRuleForm.STATE_NAMES, new String[] { "ERROR", "SUCCESS"});
    }

    public void testEditCleanupPolicyCancel()
    {
        CleanupRuleForm form = new CleanupRuleForm(tester, "editCleanupRule");

        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });

        // using the index of the link is brittle, but since the links id
        // contains a reference to the id of the object (which we do not know)
        // there are not many options available.
        clickLinkWithText("edit", 7);

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

        // using the index of the link is brittle, but since the links id
        // contains a reference to the id of the object (which we do not know)
        // there are not many options available.
        clickLinkWithText("edit", 7);

        form.saveFormElements(null, null, "0", "days");
        form.assertFormPresent();
        assertTextPresent("limit must be a positive value");
    }

    public void testDeleteCleanupPolicy()
    {
        assertProjectCleanupTable(new String[][] { getCleanupRow(true, "any", "10 builds") });
        clickLinkWithText("delete", 1);
        assertProjectCleanupTable(null);
    }

    public void testAddBuildSpec()
    {
        addSpec(SPEC_NAME);

        assertBuildSpecification(SPEC_NAME, true, true, "clean checkout", true, 100, true, new String[] { STAGE_NAME, RECIPE_NAME, "master"});

        // Check back on the configuration tab: ensure spec appears
        clickLinkWithText("configuration");
        assertProjectBuildSpecTable(new String[][]{
                createBuildSpecRow("default", true),
                createBuildSpecRow(SPEC_NAME, false)
        });
    }

    private void addSpec(String name)
    {
        AddBuildSpecForm form = new AddBuildSpecForm(tester);

        assertAndClick("project.buildspec.add");
        form.assertFormPresent();
        form.saveFormElements(name, "true", "true", "true", "100", "true", STAGE_NAME, RECIPE_NAME, "1");
    }

    public void testAddBuildSpecValidation()
    {
        AddBuildSpecForm form = new AddBuildSpecForm(tester);

        assertProjectBuildSpecTable(new String[][]{
                createBuildSpecRow("default", true)
        });

        assertAndClick("project.buildspec.add");
        form.assertFormPresent();
        form.saveFormElements("", "true", "true", "true", "-100", "true", "", "", "1");
        form.assertFormPresent();

        assertTextPresent("name is required");
        assertTextPresent("Timeout must be a positive value");
        assertTextPresent("stage name is required");
    }

    public void testAddBuildSpecDuplicate()
    {
        AddBuildSpecForm form = new AddBuildSpecForm(tester);

        assertAndClick("project.buildspec.add");
        form.assertFormPresent();
        form.saveFormElements("default", "false", "false", "true", "100", "true", STAGE_NAME, RECIPE_NAME, "1");
        form.assertFormPresent();

        assertTextPresent("'default' already exists");
    }

    public void testEditBuildSpec()
    {
        addSpec(SPEC_NAME);
        assertAndClick("edit_basics");

        EditBuildSpecForm form = new EditBuildSpecForm(tester);
        form.assertFormPresent();
        form.assertFormElements(SPEC_NAME, "true", "true", "CLEAN_CHECKOUT", "true", "100", "true");
        form.saveFormElements(SPEC_NAME + "_edited", "false", "false", "INCREMENTAL_UPDATE", null, null, "false");

        assertBuildSpecification(SPEC_NAME + "_edited", false, false, "incremental update", false, 0, false);

        clickLinkWithText("configuration");
        assertProjectBuildSpecTable(new String[][]{
                createBuildSpecRow("default", true),
                createBuildSpecRow(SPEC_NAME + "_edited", false)
        });
    }

    public void testEditBuildSpecValidation()
    {
        addSpec(SPEC_NAME);
        assertAndClick("edit_basics");

        EditBuildSpecForm form = new EditBuildSpecForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", "true", "true", "INCREMENTAL_UPDATE", "true", "-100", "true");
        form.assertFormPresent();

        assertTextPresent("name is required");
        assertTextPresent("Timeout must be a positive value");
    }

    public void testEditBuildSpecDuplicate()
    {
        addSpec(SPEC_NAME);
        assertAndClick("edit_basics");

        EditBuildSpecForm form = new EditBuildSpecForm(tester);
        form.assertFormPresent();
        form.saveFormElements("default", "true", "true", "INCREMENTAL_UPDATE", "true", "100", "true");
        form.assertFormPresent();

        assertTextPresent("'default' already exists");
    }

    public void testDeleteBuildSpec()
    {
        testAddBuildSpec();

        assertTextPresent("scm trigger");
        clickLinkWithText("make default");
        clickLinkWithText("delete");
        assertProjectBuildSpecTable(new String[][]{
                createBuildSpecRow(SPEC_NAME, true)
        });

        assertTextNotPresent("scm trigger");
    }

    public void testAddBuildStage()
    {
        addSpec(SPEC_NAME);
        addStage("new-stage");

        assertBuildStage("new-stage", NEW_RECIPE, "master");
    }

    private void addStage(String name)
    {
        clickLink("stage.add");

        BuildStageForm form = new BuildStageForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements(name, NEW_RECIPE, "1");
    }

    public void testAddBuildStageValidation()
    {
        addSpec(SPEC_NAME);
        clickLink("stage.add");

        BuildStageForm form = new BuildStageForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("", "", "1");

        form.assertFormPresent();
        assertTextPresent("stage name is required");
    }

    public void testAddBuildStageDuplicate()
    {
        addSpec(SPEC_NAME);
        clickLink("stage.add");

        BuildStageForm form = new BuildStageForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements(STAGE_NAME, "", "1");

        form.assertFormPresent();
        assertTextPresent("A stage with name '" + STAGE_NAME + "' already exists");
    }

    public void testEditBuildStage() throws IOException
    {
        addSpec(SPEC_NAME);
        clickLinkWithText("edit", 1);

        BuildStageForm form = new BuildStageForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements(STAGE_NAME, RECIPE_NAME, "1");
        form.saveFormElements(STAGE_NAME + "_edited", RECIPE_NAME + "_edited", "0");

        assertBuildStage(STAGE_NAME + "_edited", RECIPE_NAME + "_edited", "[any]");
        clickLinkWithText("edit", 2);
        form.assertFormPresent();
        form.assertFormElements(STAGE_NAME + "_edited", RECIPE_NAME + "_edited", "0");
    }

    public void testEditBuildStageValidation() throws IOException
    {
        addSpec(SPEC_NAME);
        clickLinkWithText("edit", 1);

        BuildStageForm form = new BuildStageForm(tester, false);
        form.assertFormPresent();
        form.saveFormElements("", "", "0");

        form.assertFormPresent();
        assertTextPresent("stage name is required");
    }

    public void testEditBuildStageDuplicate() throws IOException
    {
        addSpec(SPEC_NAME);
        addStage("aneditablestage");

        clickLinkWithText("edit", 3);

        BuildStageForm form = new BuildStageForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements("aneditablestage", NEW_RECIPE, "1");
        form.saveFormElements(STAGE_NAME, "", "0");

        form.assertFormPresent();
        assertTextPresent("A stage with name '" + STAGE_NAME + "' already exists");
    }

    public void testDeleteBuildStage()
    {
        // httpunit canna handle it :|
//        addSpec(SPEC_NAME);
//        addStage("deleta");
//
//        assertTextPresent("deleta");
//        clickLink("delete_deleta");
//        assertTextNotPresent("deleta");
    }

    public void testAddBuildSpecProperty()
    {
        clickLink("edit_default");
        addBuildSpecProperty(PROPERTY_NAME);

        assertBuildSpecProperties(getPropertyRow(PROPERTY_NAME, "pvalue", true, true, true));
    }

    private CreatePropertyForm addBuildSpecProperty(String name)
    {
        clickLink("add_property");

        CreatePropertyForm form = new CreatePropertyForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements(name, "pvalue", "true", "true", "true");
        return form;
    }

    public void testAddBuildSpecPropertyValidation()
    {
        clickLink("edit_default");
        clickLink("add_property");

        CreatePropertyForm form = new CreatePropertyForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("", "pvalue", "true", "true", "true");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testAddBuildSpecPropertyDuplicate()
    {
        clickLink("edit_default");
        addBuildSpecProperty(PROPERTY_NAME);

        CreatePropertyForm form = addBuildSpecProperty(PROPERTY_NAME);
        form.assertFormPresent();
        assertTextPresent("This specification already contains a property with name '" + PROPERTY_NAME + "'");
    }

    public void testEditBuildSpecProperty()
    {
        clickLink("edit_default");
        addBuildSpecProperty(PROPERTY_NAME);

        clickLink("edit_property_" + PROPERTY_NAME);
        EditPropertyForm form = new EditPropertyForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("edited", "editedvalue", "false", "false", "false");
        assertBuildSpecProperties(getPropertyRow("edited", "editedvalue", false, false, false));
    }

    public void testEditBuildSpecPropertyValidation()
    {
        clickLink("edit_default");
        addBuildSpecProperty(PROPERTY_NAME);

        clickLink("edit_property_" + PROPERTY_NAME);
        EditPropertyForm form = new EditPropertyForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("", "editedvalue", "false", "false", "false");
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testEditBuildSpecPropertyDuplicate()
    {
        clickLink("edit_default");
        addBuildSpecProperty("thefuzz");
        addBuildSpecProperty(PROPERTY_NAME);

        clickLink("edit_property_" + PROPERTY_NAME);
        EditPropertyForm form = new EditPropertyForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("thefuzz", "editedvalue", "false", "false", "false");
        form.assertFormPresent();
        assertTextPresent("this specification already contains a property with name 'thefuzz'");
    }

    public void testDeleteBuildSpecProperty()
    {
        clickLink("edit_default");
        addBuildSpecProperty(PROPERTY_NAME);

        clickLink("delete_property_" + PROPERTY_NAME);
        assertTextNotPresent(PROPERTY_NAME);
    }

    public void testAddBuildSpecResource()
    {
        addSpec(SPEC_NAME);
        addResource(RESOURCE_NAME, 0);

        assertResourceRequirements("", new String[] { RESOURCE_NAME, VERSION_VALUE });
    }

    private void addResource(String name, int index)
    {
        clickLinkWithText("add required resource", index);

        RequiredResourceForm form = new RequiredResourceForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements(name, VERSION_VALUE);
    }

    public void testAddBuildSpecResourceValidation()
    {
        addSpec(SPEC_NAME);
        clickLinkWithText("add required resource");

        RequiredResourceForm form = new RequiredResourceForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("", "");

        form.assertFormPresent();
        assertTextPresent("resource name is required");
    }

    public void testEditBuildSpecResource()
    {
        addSpec(SPEC_NAME);
        addResource(RESOURCE_NAME, 0);

        clickLinkWithText("edit", 1);
        RequiredResourceForm form = new RequiredResourceForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements(RESOURCE_NAME, VERSION_VALUE);
        form.saveFormElements(RESOURCE_NAME + "_edited", VERSION_VALUE + "_edited");

        assertResourceRequirements("", new String[] { RESOURCE_NAME + "_edited", VERSION_VALUE + "_edited" });
    }

    public void testAddBuildStageResource()
    {
        addSpec(SPEC_NAME);
        addResource(RESOURCE_NAME, 1);

        assertResourceRequirements(STAGE_NAME, new String[] { RESOURCE_NAME, VERSION_VALUE });
    }

    public void testEditBuildStageResource()
    {
        addSpec(SPEC_NAME);
        addResource(RESOURCE_NAME, 1);

        clickLinkWithText("edit", 2);
        RequiredResourceForm form = new RequiredResourceForm(tester, false);
        form.assertFormPresent();
        form.assertFormElements(RESOURCE_NAME, VERSION_VALUE);
        form.saveFormElements(RESOURCE_NAME + "_edited", VERSION_VALUE + "_edited");

        assertResourceRequirements(STAGE_NAME, new String[] { RESOURCE_NAME + "_edited", VERSION_VALUE + "_edited" });
    }

    public void testAddNewTrigger()
    {
        assertProjectTriggerTable(new String[][]{
                getTriggerRow("scm trigger", "event", "default"),
        });

        assertLinkPresent("project.trigger.add");
        clickLink("project.trigger.add");

        // check form is available.
        assertFormPresent("trigger.type");
        setFormElement("type", "cron");
        // default specification.
        setFormElement("name", TRIGGER_NAME);
        submit("next");

        // check form is available.
        assertFormPresent("trigger.cron.create");
        setWorkingForm("trigger.cron.create");
        setFormElement("cron", CRON_STRING);
        submit("next");

        assertProjectTriggerTable(new String[][]{
                getTriggerRow("scm trigger", "event", "default"),
                getTriggerRow(TRIGGER_NAME, "cron", "default"),
        });
    }

    public void testDeleteTrigger()
    {
        String triggerName = "scm trigger";
        assertProjectTriggerTable(new String[][]{
                getTriggerRow(triggerName, "event", "default"),
        });

        assertLinkPresent("delete_" + triggerName);
        clickLink("delete_" + triggerName);

        assertProjectTriggerTable(new String[][]{});
    }

    public void testDisableEnableTrigger()
    {
        String triggerName = "scm trigger";
        assertProjectTriggerTable(new String[][]{
                getTriggerRow(triggerName, "event", "default", true),
        });

        assertAndClick("toggle_" + triggerName);
        assertProjectTriggerTable(new String[][]{
                getTriggerRow(triggerName, "event", "default", false),
        });

        assertAndClick("toggle_" + triggerName);
        assertProjectTriggerTable(new String[][]{
                getTriggerRow(triggerName, "event", "default", true),
        });
    }

    public void testCreateTriggerValidation()
    {
        // ensure that the name remains unique.
        String triggerName = "scm trigger"; // this is the default trigger name.
        assertProjectTriggerTable(new String[][]{
                getTriggerRow(triggerName, "event", "default")
        });

        clickLink("project.trigger.add");
        assertFormPresent("trigger.type");
        // check that we can not create a trigger with an existing name.
        setFormElement("name", triggerName);
        setFormElement("type", "cron");
        // go with the defaults.
        submit("next");

        assertFormPresent("trigger.type");
        // assert text present..

        assertOptionValuesEqual("type", new String[]{"build.completed", "cron", "monitor"});

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
        testAddBuildSpec();

        CronTriggerEditForm form = new CronTriggerEditForm(tester);
        clickLink(getEditId(TRIGGER_NAME));

        form.assertFormPresent();
        form.assertFormElements(TRIGGER_NAME, form.getOptionValue("specification", "default"), CRON_STRING);
        assertOptionValuesEqual("specification", new String[]{form.getOptionValue("specification", "default"), form.getOptionValue("specification", SPEC_NAME)});
        return form;
    }

    public void testEditCronTrigger()
    {
        CronTriggerEditForm form = editCronTriggerHelper();
        form.saveFormElements("new name", form.getOptionValue("specification", SPEC_NAME), "0 0 1 * * ?");

        assertProjectTriggerTable(new String[][]{
                getTriggerRow("scm trigger", "event", "default"),
                getTriggerRow("new name", "cron", SPEC_NAME)
        });

        clickLink(getEditId("new name"));

        form.assertFormPresent();
        form.assertFormElements("new name", form.getOptionValue("specification", SPEC_NAME), "0 0 1 * * ?");
    }

    public void testEditCronTriggerCancel()
    {
        CronTriggerEditForm form = editCronTriggerHelper();
        form.cancelFormElements("new name", form.getOptionValue("specification", SPEC_NAME), "0 0 1 * * ?");

        assertProjectTriggerTable(new String[][]{
                getTriggerRow("scm trigger", "event", "default"),
                getTriggerRow(TRIGGER_NAME, "cron", "default"),
        });
    }

    public void testEditCronTriggerValidation()
    {
        CronTriggerEditForm form = editCronTriggerHelper();

        // Try empty name
        form.saveFormElements("", form.getOptionValue("specification", SPEC_NAME), "0 0 1 * * ?");
        form.assertFormPresent();
        assertTextPresent("name is required");

        // Try an empty cron string
        form.saveFormElements("name", form.getOptionValue("specification", SPEC_NAME), "");
        form.assertFormPresent();
        assertTextPresent("cron expression is required");

        // Try an invalid cron string
        form.saveFormElements("name", form.getOptionValue("specification", SPEC_NAME), "0 0 1 * *");
        form.assertFormPresent();
        assertTextPresent("Unexpected end of expression");
    }

    private EventTriggerEditForm editEventTriggerHelper()
    {
        // First ensure we have a two build specs
        testAddBuildSpec();

        EventTriggerEditForm form = new EventTriggerEditForm(tester);
        assertAndClick(getEditId("scm trigger"));

        form.assertFormPresent();
        form.assertFormElements("scm trigger", form.getOptionValue("specification", "default"));
        assertOptionValuesEqual("specification", new String[]{form.getOptionValue("specification", "default"), form.getOptionValue("specification", SPEC_NAME)});
        return form;
    }

    public void testEditEventTrigger()
    {
        EventTriggerEditForm form = editEventTriggerHelper();
        form.saveFormElements("new name", form.getOptionValue("specification", SPEC_NAME));

        assertProjectTriggerTable(new String[][]{
                getTriggerRow("new name", "event", SPEC_NAME),
        });
    }

    public void testEditEventTriggerCancel()
    {
        EventTriggerEditForm form = editEventTriggerHelper();
        form.cancelFormElements("new name", form.getOptionValue("specification", SPEC_NAME));

        assertProjectTriggerTable(new String[][]{
                getTriggerRow("scm trigger", "event", "default"),
        });
    }

    public void testEditEventTriggerValidation()
    {
        EventTriggerEditForm form = editEventTriggerHelper();

        // Try empty name
        form.saveFormElements("", form.getOptionValue("specification", SPEC_NAME));
        form.assertFormPresent();
        assertTextPresent("name is required");
    }

    public void testAddNewBuildCompletedTrigger()
    {
        assertProjectTriggerTable(new String[][]{
                getTriggerRow("scm trigger", "event", "default"),
        });

        assertLinkPresent("project.trigger.add");
        clickLink("project.trigger.add");

        // check form is available.
        assertFormPresent("trigger.type");
        setFormElement("type", "build.completed");
        // default specification.
        setFormElement("name", TRIGGER_NAME);
        submit("next");

        // check form is available.
        CreateBuildCompletedTriggerForm form = new CreateBuildCompletedTriggerForm(tester);
        form.assertFormPresent();
        String id = tester.getDialog().getValueForOption(form.getFieldNames()[0], projectName);
        form.nextFormElements(id, "default", "FAILURE");

        assertProjectTriggerTable(new String[][]{
                getTriggerRow("scm trigger", "event", "default"),
                getTriggerRow(TRIGGER_NAME, "event", "default"),
        });
    }

    private EditBuildCompletedTriggerForm editBuildCompletedTriggerHelper()
    {
        // First ensure we have a trigger and two build specs
        testAddBuildSpec();
        testAddNewBuildCompletedTrigger();

        EditBuildCompletedTriggerForm form = new EditBuildCompletedTriggerForm(tester);
        assertAndClick(getEditId(TRIGGER_NAME));
        form.assertFormPresent();
        return form;
    }

    // This is another case where Javascript causes HTTPUnit to barf
//    public void testEditBuildCompletedTrigger()
//    {
//        EditBuildCompletedTriggerForm form = editBuildCompletedTriggerHelper();
//        String id = tester.getDialog().getValueForOption(form.getFieldNames()[2], projectName);
//        form.assertFormElements(TRIGGER_NAME, "default", id, "default", "FAILURE");
//        form.saveFormElements("new name", form.getOptionValue("specification", SPEC_NAME), id, "", "SUCCESS");
//
//        assertProjectTriggerTable(new String[][]{
//                getTriggerRow("new name", "event", SPEC_NAME),
//        });
//
//        assertAndClick(getEditId(TRIGGER_NAME));
//        form.assertFormPresent();
//        form.assertFormElements("new name", form.getOptionValue("specification", SPEC_NAME), id, "", "SUCCESS");
//    }

    public void testCloneProject() throws IOException
    {
        String originalConfig = getResponse();

        clickLinkWithText("home");
        clickLinkWithText("clone project");

        CloneProjectForm form = new CloneProjectForm(tester);
        form.assertFormPresent();
        String clone = "clone " + RandomUtils.randomString(5);
        form.saveFormElements(clone, DESCRIPTION);

        String newConfig = getResponse();
        // Config should be the same, just the name and ids are different.
        assertEquals(nukeIds(originalConfig.replace(projectName, clone)), nukeIds(newConfig));
    }

    public void testCloneSameName()
    {
        clickLinkWithText("home");
        clickLinkWithText("clone project");

        CloneProjectForm form = new CloneProjectForm(tester);
        form.assertFormPresent();
        form.saveFormElements(projectName, DESCRIPTION);
        form.assertFormPresent();
        assertTextPresent("name '" + projectName + "' is already in use");
    }

    public void testCloneNoName()
    {
        clickLinkWithText("home");
        clickLinkWithText("clone project");

        CloneProjectForm form = new CloneProjectForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", DESCRIPTION);
        form.assertFormPresent();
        assertTextPresent("project name is required");
    }

    /**
     * CIB-409.
     */
    public void testCloneProjectCreatesDistinctScm()
    {
        // assert initial scm details
        assertProjectCvsTable("cvs", TEST_CVSROOT + "[module]");

        // take the default project and clone it.
        clickLinkWithText("home");
        clickLinkWithText("clone project");

        // clone it -> cloned project
        CloneProjectForm form = new CloneProjectForm(tester);
        form.saveFormElements("cloned " + projectName, "project b description");

        // update project Bs scm
        clickLink("project.scm.edit");
        CvsForm.Edit cvsForm = new CvsForm.Edit(tester);
        cvsForm.saveFormElements(TEST_CVSROOT, "updatedModule", "", "", "", "", "false");
        assertProjectCvsTable("cvs", TEST_CVSROOT + "[updatedModule]");

        // ensure that project A is as expected.
        clickLink(Navigation.TAB_PROJECTS);
        clickLink(projectName); // use the id of the link.
        clickLinkWithText("configuration");
        assertProjectCvsTable("cvs", TEST_CVSROOT + "[module]");
    }

    /**
     * Test that by default, the scm filters row in the scm table on the project configuration page
     * shows that filters are disabled.
     */
    public void testScmFilterDefaultDisplay()
    {
        assertTablePresent("project.scm");
        assertTableRowEqual("project.scm", 6, new String[]{"filters", "disabled"});
    }

    public void testScmFilterAddDeleteExclusion()
    {
        clickLink("project.scm.filter");

        assertTextNotPresent("some/path/to/exclude/**");

        // assert that the form is present.
        AddScmFilterForm form = new AddScmFilterForm(tester);
        form.assertFormPresent();
        form.addFormElements("some/path/to/exclude/**");

        // assert that the data is now in the table
        assertTableRowEqual("scm.filters", 2, new String[]{"some/path/to/exclude/**", "delete"});

        // assert that the form has reset itself.
        form.assertFormElements("");

        // now ensure that we can delete it.
        clickLink("delete_0");
        assertTextNotPresent("some/path/to/exclude/**");
    }

    public void testScmFilterAddValidation()
    {
        clickLink("project.scm.filter");

        AddScmFilterForm form = new AddScmFilterForm(tester);
        form.assertFormPresent();
        form.addFormElements("");

        // assert that the table contains no data.
        assertLinkNotPresent("delete_0");
    }

    public void testAddTagAction()
    {
        addTagAction("test-action");

        assertProjectActionTable(new String[][] { getActionRow("test-action", "apply tag") });

        assertAndClick("edit_test-action");
        EditTagActionForm editForm = new EditTagActionForm(tester);
        editForm.assertFormPresent();
        editForm.assertFormElements("test-action", null, null, "true", "my tag", "true");
    }

    private void addTagAction(String name)
    {
        assertAndClick("project.post.build.action.add");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester);
        typeForm.assertFormPresent();
        typeForm.nextFormElements(name, "tag", null, null, "true");

        AddTagActionForm tagForm = new AddTagActionForm(tester);
        tagForm.assertFormPresent();
        tagForm.nextFormElements("my tag", "true");
    }

    public void testAddTagSpecsAndStates()
    {
        addSpec("aspec");
        clickLinkWithText("configuration");

        assertAndClick("project.post.build.action.add");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester);
        typeForm.assertFormPresent();
        String id = tester.getDialog().getValueForOption("specIds", "aspec");
        typeForm.nextFormElements("test-action", "tag", id, "FAILURE", "false");

        AddTagActionForm tagForm = new AddTagActionForm(tester);
        tagForm.assertFormPresent();
        tagForm.nextFormElements("my tag", "false");

        assertProjectActionTable(new String[][] { getActionRow("test-action", "apply tag") });

        assertAndClick("edit_test-action");
        EditTagActionForm editForm = new EditTagActionForm(tester);
        editForm.assertFormPresent();
        editForm.assertFormElements("test-action", id, "FAILURE", "false", "my tag", "false");
    }

    public void testAddTagActionValidation()
    {
        assertAndClick("project.post.build.action.add");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester);
        typeForm.assertFormPresent();
        typeForm.nextFormElements("", "tag", null, null, "true");
        typeForm.assertFormPresent();
        assertTextPresent("name is required");

        typeForm.nextFormElements("my-action", "tag", null, null, "true");
        AddTagActionForm tagForm = new AddTagActionForm(tester);
        tagForm.assertFormPresent();
        tagForm.nextFormElements("", "true");
        tagForm.assertFormPresent();
        assertTextPresent("tag name is required");
    }

    public void testAddTagActionDuplicate()
    {
        addTagAction("dupit");

        assertAndClick("project.post.build.action.add");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester);
        typeForm.assertFormPresent();
        typeForm.nextFormElements("dupit", "tag", null, null, "true");
        typeForm.assertFormPresent();
        assertTextPresent("This project already has a post build action with name 'dupit'");
    }

    public void testEditTagAction()
    {
        addTagAction("mytag");

        assertAndClick("edit_mytag");
        EditTagActionForm form = new EditTagActionForm(tester);
        form.assertFormPresent();
        String id = tester.getDialog().getValueForOption("specIds", "default");
        form.saveFormElements("editedtag", id, "SUCCESS", "false", "${project}", "false");

        assertProjectActionTable(new String[][] { getActionRow("editedtag", "apply tag") });
        assertAndClick("edit_editedtag");
        form.assertFormPresent();
        form.assertFormElements("editedtag", id, "SUCCESS", "false", "${project}", "false");
    }

    public void testEditTagActionValidation()
    {
        addTagAction("mytag");

        assertAndClick("edit_mytag");
        EditTagActionForm form = new EditTagActionForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", null, null, "false", "", "false");
        form.assertFormPresent();
        assertTextPresent("tag name is required");

        form.saveFormElements("", null, null, "false", "tagname", "false");
        assertTextPresent("name is required");
    }

    public void testEditTagActionDuplicate()
    {
        addTagAction("mytag");
        addTagAction("dupit");

        assertAndClick("edit_mytag");
        EditTagActionForm form = new EditTagActionForm(tester);
        form.assertFormPresent();
        form.saveFormElements("dupit", null, null, "false", "${status}", "false");
        form.assertFormPresent();
        assertTextPresent("This project already has a post build action with name 'dupit'");
    }

    public void testAddExeAction()
    {
        addExeAction("test-action");

        assertProjectActionTable(new String[][] { getActionRow("test-action", "run executable") });

        assertAndClick("edit_test-action");
        EditExeActionForm editForm = new EditExeActionForm(tester);
        editForm.assertFormPresent();
        editForm.assertFormElements("test-action", null, null, "true", "thecommand", "theargs");
    }

    private void addExeAction(String name)
    {
        assertAndClick("project.post.build.action.add");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester);
        typeForm.assertFormPresent();
        typeForm.nextFormElements(name, "exe", null, null, "true");

        AddExeActionForm exeForm = new AddExeActionForm(tester);
        exeForm.assertFormPresent();
        exeForm.nextFormElements("thecommand", "theargs");
    }

    public void testAddExeActionValidation()
    {
        assertAndClick("project.post.build.action.add");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester);
        typeForm.assertFormPresent();
        typeForm.nextFormElements("", "exe", null, null, "true");
        typeForm.assertFormPresent();
        assertTextPresent("name is required");

        typeForm.nextFormElements("my-action", "exe", null, null, "true");
        AddExeActionForm tagForm = new AddExeActionForm(tester);
        tagForm.assertFormPresent();
        tagForm.nextFormElements("", "args");
        tagForm.assertFormPresent();
        assertTextPresent("command is required");
    }

    public void testEditExeAction()
    {
        addExeAction("myexe");

        assertAndClick("edit_myexe");
        EditExeActionForm form = new EditExeActionForm(tester);
        form.assertFormPresent();
        String id = tester.getDialog().getValueForOption("specIds", "default");
        form.saveFormElements("editedexe", id, "SUCCESS", "false", "command", "${project} ${number} ${status}");

        assertProjectActionTable(new String[][] { getActionRow("editedexe", "run executable") });
        assertAndClick("edit_editedexe");
        form.assertFormPresent();
        form.assertFormElements("editedexe", id, "SUCCESS", "false", "command", "${project} ${number} ${status}");
    }

    public void testEditExeActionValidation()
    {
        addExeAction("myexe");

        assertAndClick("edit_myexe");
        EditExeActionForm form = new EditExeActionForm(tester);
        form.assertFormPresent();
        form.saveFormElements("", null, null, "false", "", "");
        form.assertFormPresent();
        assertTextPresent("name is required");
        assertTextPresent("command is required");
    }

    public void testEditExeActionSameName()
    {
        addExeAction("myexe");

        assertAndClick("edit_myexe");
        EditExeActionForm form = new EditExeActionForm(tester);
        form.assertFormPresent();
        form.saveFormElements("myexe", null, null, "false", "command", "args");
        assertProjectActionTable(new String[][] { getActionRow("myexe", "run executable") });
    }

    public void testDeletePostBuildAction()
    {
        addTagAction("deadtag");
        assertTextPresent("deadtag");
        assertAndClick("delete_deadtag");
        assertTextNotPresent("deadtag");
    }

    public void testAddTagStageAction()
    {
        clickLink("edit_default");
        addTagStageAction("test-action");

        assertStageActionTable("default", new String[][] { getActionRow("test-action", "apply tag") });

        assertAndClick("edit_post_default_test-action");
        EditTagActionForm editForm = new EditTagActionForm(tester, true);
        editForm.assertFormPresent();
        editForm.assertFormElements("test-action", null, "true", "my tag", "true");
    }

    private void addTagStageAction(String name)
    {
        assertAndClick("add_post_action_default");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester, true);
        typeForm.assertFormPresent();
        typeForm.nextFormElements(name, "tag", null, "true");

        AddTagActionForm tagForm = new AddTagActionForm(tester);
        tagForm.assertFormPresent();
        tagForm.nextFormElements("my tag", "true");
    }

    public void testAddTagStageStates()
    {
        clickLink("edit_default");
        assertAndClick("add_post_action_default");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester, true);
        typeForm.assertFormPresent();
        typeForm.nextFormElements("test-action", "tag", "FAILURE", "false");

        AddTagActionForm tagForm = new AddTagActionForm(tester);
        tagForm.assertFormPresent();
        tagForm.nextFormElements("my tag", "false");

        assertStageActionTable("default", new String[][] { getActionRow("test-action", "apply tag") });

        assertAndClick("edit_post_default_test-action");
        EditTagActionForm editForm = new EditTagActionForm(tester, true);
        editForm.assertFormPresent();
        editForm.assertFormElements("test-action", "FAILURE", "false", "my tag", "false");
    }

    public void testAddTagStageActionValidation()
    {
        clickLink("edit_default");
        assertAndClick("add_post_action_default");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester, true);
        typeForm.assertFormPresent();
        typeForm.nextFormElements("", "tag", null, "true");
        typeForm.assertFormPresent();
        assertTextPresent("name is required");

        typeForm.nextFormElements("my-action", "tag", null, "true");
        AddTagActionForm tagForm = new AddTagActionForm(tester);
        tagForm.assertFormPresent();
        tagForm.nextFormElements("", "true");
        tagForm.assertFormPresent();
        assertTextPresent("tag name is required");
    }

    public void testAddTagStageActionDuplicate()
    {
        clickLink("edit_default");
        addTagStageAction("dupit");

        assertAndClick("add_post_action_default");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester, true);
        typeForm.assertFormPresent();
        typeForm.nextFormElements("dupit", "tag", null, "true");
        typeForm.assertFormPresent();
        assertTextPresent("This stage already has a post stage action with name 'dupit'");
    }

    public void testEditTagStageAction()
    {
        clickLink("edit_default");
        addTagStageAction("mytag");

        assertAndClick("edit_post_default_mytag");
        EditTagActionForm form = new EditTagActionForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("editedtag", "SUCCESS", "false", "${project}", "false");

        assertStageActionTable("default", new String[][] { getActionRow("editedtag", "apply tag") });
        assertAndClick("edit_post_default_editedtag");
        form.assertFormPresent();
        form.assertFormElements("editedtag", "SUCCESS", "false", "${project}", "false");
    }

    public void testEditTagStageActionValidation()
    {
        clickLink("edit_default");
        addTagStageAction("mytag");

        assertAndClick("edit_post_default_mytag");
        EditTagActionForm form = new EditTagActionForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("name", null, "false", "", "false");
        form.assertFormPresent();
        assertTextPresent("tag name is required");

        form.saveFormElements("", null, "false", "tagname", "false");
        assertTextPresent("name is required");
    }

    public void testEditTagStageActionDuplicate()
    {
        clickLink("edit_default");
        addTagStageAction("mytag");
        addTagStageAction("dupit");

        assertAndClick("edit_post_default_mytag");
        EditTagActionForm form = new EditTagActionForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("dupit", null, "false", "${status}", "false");
        form.assertFormPresent();
        assertTextPresent("This stage already has a post stage action with name 'dupit'");
    }

    public void testAddExeStageAction()
    {
        clickLink("edit_default");
        addExeStageAction("test-action");

        assertStageActionTable("default", new String[][] { getActionRow("test-action", "run executable") });

        assertAndClick("edit_post_default_test-action");
        EditExeActionForm editForm = new EditExeActionForm(tester, true);
        editForm.assertFormPresent();
        editForm.assertFormElements("test-action", null, "true", "thecommand", "theargs");
    }

    private void addExeStageAction(String name)
    {
        assertAndClick("add_post_action_default");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester, true);
        typeForm.assertFormPresent();
        typeForm.nextFormElements(name, "exe", null, "true");

        AddExeActionForm exeForm = new AddExeActionForm(tester);
        exeForm.assertFormPresent();
        exeForm.nextFormElements("thecommand", "theargs");
    }

    public void testAddExeStageActionValidation()
    {
        clickLink("edit_default");
        assertAndClick("add_post_action_default");

        AddPostBuildActionForm typeForm = new AddPostBuildActionForm(tester, true);
        typeForm.assertFormPresent();
        typeForm.nextFormElements("", "exe", null, "true");
        typeForm.assertFormPresent();
        assertTextPresent("name is required");
        typeForm.nextFormElements("my-action", "exe", null, "true");

        AddExeActionForm exeForm = new AddExeActionForm(tester);
        exeForm.assertFormPresent();
        exeForm.nextFormElements("", "theargs");
        exeForm.assertFormPresent();
        assertTextPresent("command is required");
    }

    public void testEditExeStageAction()
    {
        clickLink("edit_default");
        addExeStageAction("myexe");

        assertAndClick("edit_post_default_myexe");
        EditExeActionForm form = new EditExeActionForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("editedexe", "SUCCESS", "false", "command", "${project} ${number} ${status}");

        assertStageActionTable("default", new String[][] { getActionRow("editedexe", "run executable") });
        assertAndClick("edit_post_default_editedexe");
        form.assertFormPresent();
        form.assertFormElements("editedexe", "SUCCESS", "false", "command", "${project} ${number} ${status}");
    }

    public void testEditExeStageActionValidation()
    {
        clickLink("edit_default");
        addExeStageAction("myexe");

        assertAndClick("edit_post_default_myexe");
        EditExeActionForm form = new EditExeActionForm(tester, true);
        form.assertFormPresent();
        form.saveFormElements("", null, "false", "", "");
        form.assertFormPresent();
        assertTextPresent("name is required");
        assertTextPresent("command is required");
    }

    public void testDeletePostStageAction()
    {
        clickLink("edit_default");
        addExeStageAction("deadexe");
        assertTextPresent("deadexe");
        assertAndClick("delete_post_default_deadexe");
        assertTablePresent("post.actions.default");
        assertTextNotPresent("deadtag");
    }

    public void testAddNewCommitMessageTransformer() throws Exception
    {
        callRemoteApi("deleteAllCommitMessageLinks");

        // click add link.
        assertAndClick("project.transformer.add");

        // select standard.
        selectCommitMessageTransformerType("link");
        
        // fill in the blanks.
        AddCommitMessageTransformerWizard.Link link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormPresent();
        link.finishFormElements("name", "expression", "link");
        link.assertFormNotPresent();

        // ensure that the transformer is listed.
        assertLinkPresent("edit_name");

        // ensure that the transformer is in the global list.
        clickLink(Navigation.TAB_ADMINISTRATION);
        assertLinkPresent("edit_name");
    }

    public void testAddExistingCommitMessageTransformer() throws Exception
    {
        callRemoteApi("deleteAllCommitMessageLinks");

        // create a transformer via admin.
        createCommitMessageTransformer("link", "name", "expression", "link");

        // navigate back to the project.
        beginAt(Navigation.Projects.ACTION_PROJECT_CONFIG + "?projectName=" + projectName);

        assertLinkNotPresent("edit_name");

        // click add link.
        assertAndClick("project.transformer.add");

        // select from existing list.
        AddExistingCommitMessageTransformerForm form = new AddExistingCommitMessageTransformerForm(tester);
        form.assertFormPresent();
        form.saveFormElements("name");

        // ensure that the transformer is in the list.
        assertLinkPresent("edit_name");
    }

    public void testOnlyUnassignedExistingTransformersDisplayed() throws Exception
    {
        // create two transformers via admin.
        callRemoteApi("deleteAllCommitMessageLinks");

        // create a transformer via admin.
        createCommitMessageTransformer("link", "name1", "expression1", "link1");
        createCommitMessageTransformer("link", "name2", "expression2", "link2");

        // navigate back to the project.
        beginAt(Navigation.Projects.ACTION_PROJECT_CONFIG + "?projectName=" + projectName);

        assertLinkNotPresent("edit_name1");
        assertLinkNotPresent("edit_name2");

        // click add link.
        assertAndClick("project.transformer.add");

        // select from existing list.
        AddExistingCommitMessageTransformerForm form = new AddExistingCommitMessageTransformerForm(tester);
        form.assertFormPresent();

        // select existing transformer. (ensure that two are listed)
        String[] options = form.getSelectOptions("existing");
        assertEquals(2, options.length);
        assertEquals("name1", options[0]);
        assertEquals("name2", options[1]);

        form.saveFormElements("name1");

        // ensure that the transformer is in the list.
        assertLinkPresent("edit_name1");

        // click add
        assertAndClick("project.transformer.add");
        form.assertFormPresent();

        // select existing transformer. (ensure only one is listed)
        options = form.getSelectOptions("existing");
        assertEquals(1, options.length);
        assertEquals("name2", options[0]);
        
        // submit.
        form.saveFormElements("name2");
        assertLinkPresent("edit_name2");

        // click add/
        assertAndClick("project.transformer.add");

        // ensure that no existing transformers are listed.
        form.assertFormNotPresent();
    }

    public void testAddValidatedNameUniqueness() throws Exception
    {
        callRemoteApi("deleteAllCommitMessageLinks");

        // create a transformer via admin.
        createCommitMessageTransformer("link", "name", "expression", "link");

        // navigate back to the project.
        beginAt(Navigation.Projects.ACTION_PROJECT_CONFIG + "?projectName=" + projectName);

        // click add.
        assertAndClick("project.transformer.add");

        // select standard, fill in blanks using existing name
        selectCommitMessageTransformerType("link");
        
        // ensure that form is still present.
        AddCommitMessageTransformerWizard.Link link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormPresent();
        link.finishFormElements("name", "expression1", "link2");
        link.assertFormPresent();
    }

    public void testEditCommitMessageTransformer() throws Exception
    {
        callRemoteApi("deleteAllCommitMessageLinks");

        // create a transformer
        assertAndClick("project.transformer.add");

        // select standard.
        selectCommitMessageTransformerType("link");

        // fill in the blanks.
        AddCommitMessageTransformerWizard.Link link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormPresent();
        link.finishFormElements("name", "expression", "link");
        link.assertFormNotPresent();

        // ensure that the transformer is listed.
        assertLinkPresent("edit_name");

        // click edit transformer.
        assertAndClick("edit_name");

        // change all the fields.
        link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormElements("name", "expression", "link");
        link.saveFormElements("name1", "expression1", "link1");

        // ensure that the name has changed.
        assertLinkNotPresent("edit_name");
        // click edit.
        assertAndClick("edit_name1");

        // ensure that all the field values are as expected.
        link.assertFormElements("name1", "expression1", "link1");
    }

    public void testRemoveCommitMessageTransformer() throws Exception
    {
        // create transformer.
        callRemoteApi("deleteAllCommitMessageLinks");

        // create a transformer
        assertAndClick("project.transformer.add");

        // select standard.
        selectCommitMessageTransformerType("link");

        // fill in the blanks.
        AddCommitMessageTransformerWizard.Link link = new AddCommitMessageTransformerWizard.Link(tester);
        link.assertFormPresent();
        link.finishFormElements("name", "expression", "link");
        link.assertFormNotPresent();

        assertLinkPresent("edit_name");

        // click remove transformer.
        assertAndClick("remove_name");

        // ensure that it is no longer in the list.
        assertLinkNotPresent("edit_name");

        // ensure that the transformer still exists in the global list.
        clickLink(Navigation.TAB_ADMINISTRATION);
        assertLinkPresent("edit_name");
    }

    private String nukeIds(String text)
    {
        text = text.replaceAll("href=\"[^\"]*\"", "");
        text = text.replaceAll("[iI]d=[0-9]+", "");
        text = text.replaceAll("onclick=\"[^\"]*\"", "");
        text = text.replaceAll("id=\"[^\"]*\"", "");
        return text;
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

    private void assertProjectCvsTable(String type, String location, String branch)
    {
        assertTablePresent("project.scm");
        assertTableRowsEqual("project.scm", 1, new String[][]{
                new String[]{"type", type},
                new String[]{"location", location},
                new String[]{"branch", branch}
        });
    }

    private void assertProjectCleanupTable(String[][] rows)
    {
        assertTablePresent("project.cleanup");
        if(rows != null)
        {
            String[][] allRows = new String[rows.length + 2][6];
            allRows[0] = new String[]{"what", "with state(s)", "retain for up to", "actions", "actions", "actions"};
            for(int i = 1; i <= rows.length; i++)
            {
                allRows[i] = rows[i - 1];
            }
            allRows[rows.length + 1] = new String[]{"add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule"};
            assertTableRowsEqual("project.cleanup", 1, allRows);
        }
        else
        {
            assertTableRowsEqual("project.cleanup", 1, new String[][] {
                    new String[] { "what", "with state(s)", "retain for up to", "actions"},
                    new String[] { "no rules configured", "no rules configured", "no rules configured", "no rules configured"},
                    new String[] { "add new cleanup rule", "add new cleanup rule", "add new cleanup rule", "add new cleanup rule" }
            });
        }
    }

    private String[] getCleanupRow(boolean workDirsOnly, String states, String limit)
    {
        return new String[]{workDirsOnly ? "working directories" : "whole build results", states, limit, "trigger", "edit", "delete"};
    }

    private void assertProjectBuildSpecTable(String[][] rows)
    {
        assertTablePresent("project.buildspecs");
        assertTableRowsEqual("project.buildspecs", 2, rows);
    }

    private void assertBuildSpecification(String specName, boolean isolateChangelists, boolean retainWorkingCopy, String checkoutScheme, boolean timeoutEnabled, int timeout, boolean prompt, String[]... stages)
    {
        String timeoutText = "[never]";

        if(timeoutEnabled)
        {
            timeoutText = Integer.toString(timeout) + " minutes";
        }

        assertTableRowsEqual("spec.basics", 1, new String[][] {
                new String[]{ "name", specName },
                new String[]{ "isolate changelists", Boolean.toString(isolateChangelists) },
                new String[]{ "retain working copy", Boolean.toString(retainWorkingCopy) },
                new String[]{ "checkout scheme", checkoutScheme },
                new String[]{ "timeout", timeoutText },
                new String[]{ "prompt", Boolean.toString(prompt) }
        });

        for(String[] stage: stages)
        {
            assertBuildStage(stage[0], stage[1], stage[2]);
        }
    }

    private void assertBuildStage(String name, String recipe, String agent)
    {
        assertLinkPresentWithText(name);
        assertTableRowsEqual("stage_" + name, 1, new String[][] {
                new String[] { "stage name", name },
                new String[] { "stage recipe", recipe },
                new String[] { "agent", agent },
        });
    }

    private void assertResourceRequirements(String stage, String[]... resources)
    {
        String [][] resourceRows = new String[resources.length][4];
        for(int i = 0; i < resources.length; i++)
        {
            resourceRows[i] = new String[] { resources[i][0], resources[i][1], "edit", "delete" };
        }

        assertTableRowsEqual("resources_" + stage, 2, resourceRows);
    }

    private String[] createBuildSpecRow(String name, boolean isDefault)
    {
        String def = isDefault ? "true" : "false                                        [make default]";
        String del = isDefault ? "default spec" : "delete";
        return new String[]{name, def, "trigger", "edit", del};
    }

    private void assertProjectTriggerTable(String[][] rows)
    {
        assertTablePresent("project.triggers");
        assertTableRowsEqual("project.triggers", 2, rows);
    }

    private String[] getTriggerRow(String name, String type, String spec)
    {
        return getTriggerRow(name, type, spec, true);
    }

    private String[] getTriggerRow(String name, String type, String spec, boolean enabled)
    {
        String state;
        String action;
        if(enabled)
        {
            state = "enabled";
            action = "disable";
        }
        else
        {
            state = "disabled";
            action = "enable";
        }

        return new String[]{name, type, spec, state, action, "edit", "delete"};
    }

    private void assertProjectActionTable(String[][] rows)
    {
        assertTablePresent("project.post.build.actions");
        assertTableRowsEqual("project.post.build.actions", 2, rows);
    }

    private void assertStageActionTable(String stage, String[][] rows)
    {
        String id = "post.actions." + stage;
        assertTablePresent(id);
        assertTableRowsEqual(id, 2, rows);
    }

    private String[] getActionRow(String name, String type)
    {
        return new String[]{name, type, "edit", "delete"};
    }

    private void assertBuildSpecProperties(String[]... props)
    {
        assertTableRowsEqual("spec.properties", 2, props);
    }

    private String[] getPropertyRow(String name, String value, boolean addToEnv, boolean addToPath, boolean resolve)
    {
        return new String[] { name, value, Boolean.toString(addToEnv), Boolean.toString(addToPath), Boolean.toString(resolve), "edit", "delete" };
    }

}