package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.PerforceAcceptanceTest.P4PASSWD;
import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.admin.*;
import com.zutubi.pulse.acceptance.support.PerforceUtils;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.model.ProjectManager;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.CustomChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.Condition;
import com.zutubi.util.WebUtils;
import static com.zutubi.util.WebUtils.uriComponentEncode;
import com.zutubi.util.io.IOUtils;
import static java.util.Arrays.asList;

import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Acceptance tests that verify operation of the configuration UI by trying
 * some real cases against a running server.
 */
public class ConfigUIAcceptanceTest extends SeleniumTestBase
{
    private static final String CHECK_PROJECT = "config-check-project" + SPECIAL_CHARACTERS;

    private static final String ACTION_DOWN = "down";
    private static final String ACTION_UP   = "up";
    private static final String SYMBOLIC_NAME_MULTI_RECIPE = "zutubi.multiRecipeTypeConfig";
    private static final String SYMBOLIC_NAME_CUSTOM       = "zutubi.customTypeConfig";

    protected void setUp() throws Exception
    {
        super.setUp();
        random = randomName() + SPECIAL_CHARACTERS;
        xmlRpcHelper.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testEmptyOptionsAddedForSelects() throws Exception
    {
        // When configuring a template and a single select is shown, that
        // single select should have an empty option added.
        browser.loginAsAdmin();
        addProject(random, true, GLOBAL_PROJECT_NAME, false);
        browser.open(urls.adminProject(uriComponentEncode(random)) + "scm/");
        SubversionForm form = browser.createForm(SubversionForm.class);
        form.waitFor();
        String[] options = form.getComboBoxOptions("checkoutScheme");
        assertEquals("", options[0]);
    }

    public void testDeleteListItemFromTemplateChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        xmlRpcHelper.insertTrivialProject(parentName, true);
        String childPath = xmlRpcHelper.insertSimpleProject(childName, parentName, false);
        String labelsPath = getPath(childPath, "labels");

        browser.loginAsAdmin();
        ListPage labelsPage = browser.openAndWaitFor(ListPage.class, labelsPath);
        labelsPage.clickAdd();

        LabelForm labelForm = browser.createForm(LabelForm.class);
        labelForm.waitFor();
        labelForm.finishFormElements("my-label");

        labelsPage.waitFor();
        String baseName = getNewestListItem(labelsPath);
        assertItemPresent(labelsPage, baseName, null, "view", "delete");
        DeleteConfirmPage deleteConfirmPage = labelsPage.clickDelete(baseName);
        deleteConfirmPage.waitFor();
        labelsPage = deleteConfirmPage.confirmDeleteListItem();

        assertFalse(labelsPage.isItemPresent(baseName));
    }

    public void testCancelAddListItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);

        browser.loginAsAdmin();
        ListPage labelsPage = browser.openAndWaitFor(ListPage.class, getPath(projectPath, "labels"));
        labelsPage.clickAdd();

        LabelForm labelForm = browser.createForm(LabelForm.class);
        labelForm.waitFor();
        labelForm.cancelFormElements("my-label");

        labelsPage.waitFor();
        assertFalse(labelsPage.isItemPresent("my-label"));
    }

    public void testCancelViewListItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String labelBaseName = insertLabel(projectPath);

        browser.loginAsAdmin();
        ListPage labelsPage = browser.openAndWaitFor(ListPage.class, getPath(projectPath, "labels"));
        labelsPage.clickView(labelBaseName);

        LabelForm labelForm = browser.createForm(LabelForm.class);
        labelForm.waitFor();
        labelForm.cancelFormElements("");

        labelsPage.waitFor();
        assertItemPresent(labelsPage, labelBaseName, null);
    }

    private String insertLabel(String projectPath) throws Exception
    {
        Hashtable<String, Object> label = xmlRpcHelper.createEmptyConfig(LabelConfiguration.class);
        label.put("label", "test");
        return PathUtils.getBaseName(xmlRpcHelper.insertConfig(getPath(projectPath, "labels"), label));
    }

    public void testCheckForm() throws Exception
    {
        browser.loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        browser.open(urls.adminProject(WebUtils.uriComponentEncode(CHECK_PROJECT)) + "scm/");
        SubversionForm form = browser.createForm(SubversionForm.class);
        form.waitFor();
        form.setFieldValue("url", "svn://localhost:3088/");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkFormElementsAndWait();
        assertTrue(checkForm.isResultOk());
        assertEquals("configuration ok", checkForm.getResultMessage());
    }

    public void testCheckFormFailure() throws Exception
    {
        browser.loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        browser.open(urls.adminProject(WebUtils.uriComponentEncode(CHECK_PROJECT)) + "scm/");
        SubversionForm form = browser.createForm(SubversionForm.class);
        form.waitFor();
        form.setFieldValue("url", "svn://localhost:9999/foo");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkFormElementsAndWait();
        assertFalse(checkForm.isResultOk());
        assertTrue(checkForm.getResultMessage().contains("connection refused"));
    }

    public void testCheckFormValidationFailure() throws Exception
    {
        browser.loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        browser.open(urls.adminProject(uriComponentEncode(CHECK_PROJECT)) + "scm/");
        SubversionForm form = browser.createForm(SubversionForm.class);
        form.waitFor();
        form.setFieldValue("url", "");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkFormElementsAndWait();
        assertFalse(checkForm.isResultOk());
        assertEquals("unable to check configuration due to validation errors", checkForm.getResultMessage());
        assertTextPresent("url requires a value");
    }

    public void testCheckFormCheckFieldValidationFailure() throws Exception
    {
        browser.loginAsAdmin();
        browser.open(urls.admin() + "settings/email/");
        EmailSettingsForm form = browser.createForm(EmailSettingsForm.class);
        form.waitFor();
        EmailSettingsCheckForm checkForm = new EmailSettingsCheckForm(form);
        checkForm.checkFormElementsAndWait("");
        assertFalse(checkForm.isResultOk());
        assertEquals("unable to check configuration due to validation errors", checkForm.getResultMessage());
        assertTextPresent("recipient address requires a value");
    }

    public void testCheckFormInWizard() throws Exception
    {
        browser.loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, false);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser);
        projectState.waitFor();
        projectState.nextFormElements(random, null, null);

        SelectTypeState scmTypeState = new SelectTypeState(browser);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser);
        subversionState.waitFor();

        subversionState.setFieldValue("url", "svn://localhost:3088/");
        CheckForm checkForm = new CheckForm(subversionState);
        checkForm.checkFormElementsAndWait();
        assertTrue(checkForm.isResultOk());
        assertEquals("configuration ok", checkForm.getResultMessage());

        subversionState.cancelFormElements(null, null, null, null, null, null);
    }

    public void testClearItemPicker() throws Exception
    {
        ensureProject(random);

        browser.loginAsAdmin();
        ProjectConfigPage configPage = browser.openAndWaitFor(ProjectConfigPage.class, random, false);
        ListPage listPage = configPage.clickCollection("permissions", "permissions");
        listPage.waitFor();
        assertEquals("[view]", listPage.getCellContent(0, 1));

        browser.click("link=view");
        ProjectAclForm form = browser.createForm(ProjectAclForm.class);
        form.waitFor();
        assertFormElements(form, "all users", "view");
        form.saveFormElements(null, "");
        listPage.waitFor();

        assertEquals("[]", listPage.getCellContent(0, 1));
        browser.click("link=view");
        form = browser.createForm(ProjectAclForm.class);
        form.waitFor();
        assertFormElements(form, "all users", "");
    }

    public void testClearMultiSelect() throws Exception
    {
        browser.loginAsAdmin();

        browser.open(urls.adminGroup(UserManager.ANONYMOUS_USERS_GROUP_NAME));
        BuiltinGroupForm groupForm = browser.createForm(BuiltinGroupForm.class);
        groupForm.waitFor();
        groupForm.applyFormElements(null, ServerPermission.PERSONAL_BUILD.toString());
        groupForm.waitFor();
        assertFormElements(groupForm, null, ServerPermission.PERSONAL_BUILD.toString());

        groupForm.applyFormElements(null, "");
        groupForm.waitFor();
        assertFormElements(groupForm, null, "");
    }

    public void testNameValidationDuplicate() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String propertiesPath = getPropertiesPath(projectPath);
        insertProperty(projectPath);

        browser.loginAsAdmin();
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, propertiesPath);
        propertiesPage.clickAdd();

        ResourcePropertyForm form = browser.createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null, null);
        assertTrue(form.isFormPresent());
        assertTextPresent("name is already in use, please select another name");
    }

    public void testNameValidationDuplicateInherited() throws Exception
    {
        String parentName = random + "-parent";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        insertProperty(parentPath);
        String childPath = xmlRpcHelper.insertTrivialProject(random + "-child", parentName, false);

        browser.loginAsAdmin();
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(childPath));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = browser.createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null, null);
        assertTrue(form.isFormPresent());
        assertTextPresent("name is already in use, please select another name");
    }

    public void testNameValidationDuplicateInDescendant() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);
        insertProperty(childPath);

        browser.loginAsAdmin();
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(parentPath));
        assertFalse(propertiesPage.isItemPresent("p1"));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = browser.createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null, null);
        assertTrue(form.isFormPresent());
        assertTextPresent("name is already in use in descendant \"" + childName + "\", please select another name");
    }

    public void testNameValidationDuplicateInDescendants() throws Exception
    {
        String parentName = random + "-parent";
        String child1Name = random + "-child1";
        String child2Name = random + "-child2";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String child1Path = xmlRpcHelper.insertTrivialProject(child1Name, parentName, false);
        String child2Path = xmlRpcHelper.insertTrivialProject(child2Name, parentName, false);
        insertProperty(child1Path);
        insertProperty(child2Path);

        browser.loginAsAdmin();
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(parentPath));
        assertFalse(propertiesPage.isItemPresent("p1"));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = browser.createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null, null);
        assertTrue(form.isFormPresent());
        assertTextPresent("name is already in use in descendants [" + child1Name + ", " + child2Name + "], please select another name");
    }

    public void testNameValidationDuplicateInAncestor() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);
        insertProperty(parentPath);

        String childPropertiesPath = getPropertiesPath(childPath);
        xmlRpcHelper.deleteConfig(getPath(childPropertiesPath, "p1"));

        browser.loginAsAdmin();
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, childPropertiesPath);
        assertItemPresent(propertiesPage, "p1", ListPage.ANNOTATION_HIDDEN);
        propertiesPage.clickAdd();

        ResourcePropertyForm form = browser.createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null, null);
        assertTrue(form.isFormPresent());
        assertTextPresent("name is already in use in ancestor \"" + parentName + "\", please select another name");
    }

    public void testCannotConfigureOverriddenPath() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);

        // At this point we should be allowed to configure in the parent
        browser.loginAsAdmin();
        CompositePage compositePage = browser.openAndWaitFor(CompositePage.class, getPath(parentPath, "changeViewer"));
        assertTrue(compositePage.isConfigureLinkPresent());

        String childChangeViewerPath = getPath(childPath, "changeViewer");
        Hashtable<String, Object> changeViewer = xmlRpcHelper.createEmptyConfig(CustomChangeViewerConfiguration.class);
        changeViewer.put("changesetURL", "dummy");
        xmlRpcHelper.insertConfig(childChangeViewerPath, changeViewer);

        // Now the child exists we should no longer be able to configure
        // in the parent.
        compositePage.openAndWaitFor();
        assertFalse(compositePage.isConfigureLinkPresent());
        assertTrue(compositePage.isConfiguredDescendantPresent(childName));
    }

    public void testOrderLinks() throws Exception
    {
        String path = orderPrelude();

        browser.loginAsAdmin();
        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(path));
        assertItemPresent(listPage, "p1", null, ACTION_DOWN);
        assertFalse(listPage.isActionLinkPresent("p1", ACTION_UP));
        assertItemPresent(listPage, "p2", null, ACTION_DOWN, ACTION_UP);
        assertItemPresent(listPage, "p3", null, ACTION_UP);
        assertFalse(listPage.isActionLinkPresent("p3", ACTION_DOWN));
    }

    public void testMoveUp() throws Exception
    {
        String path = orderPrelude();

        browser.loginAsAdmin();
        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(path));
        assertEquals("p2", listPage.getCellContent(1, 0));
        assertEquals("p3", listPage.getCellContent(2, 0));
        listPage.clickUp("p3");
        listPage.waitFor();
        assertEquals("p3", listPage.getCellContent(1, 0));
        assertEquals("p2", listPage.getCellContent(2, 0));
    }

    public void testMoveDown() throws Exception
    {
        String path = orderPrelude();

        browser.loginAsAdmin();
        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(path));
        assertEquals("p2", listPage.getCellContent(1, 0));
        assertEquals("p3", listPage.getCellContent(2, 0));
        listPage.clickDown("p2");
        listPage.waitFor();
        assertEquals("p2", listPage.getCellContent(2, 0));
        assertEquals("p3", listPage.getCellContent(1, 0));
    }

    private String orderPrelude() throws Exception
    {
        String path = xmlRpcHelper.insertTrivialProject(random, false);
        insertProperty(path, "p1");
        insertProperty(path, "p2");
        insertProperty(path, "p3");
        return path;
    }

    public void testOrderLinksNotPresentForUnorderedCollection() throws Exception
    {
        String path = xmlRpcHelper.insertTrivialProject(random, false);
        Hashtable<String, Object> trigger = xmlRpcHelper.createDefaultConfig(ScmBuildTriggerConfiguration.class);
        trigger.put("name", "t1");
        String triggersPath = getPath(path, "triggers");
        xmlRpcHelper.insertConfig(triggersPath, trigger);
        trigger.put("name", "t2");
        xmlRpcHelper.insertConfig(triggersPath, trigger);

        browser.loginAsAdmin();
        ListPage listPage = browser.openAndWaitFor(ListPage.class, triggersPath);
        assertFalse(listPage.isOrderColumnPresent(2));
        assertItemPresent(listPage, "t1", null);
        assertItemPresent(listPage, "t2", null);
        assertFalse(listPage.isActionLinkPresent("t1", ACTION_UP));
        assertFalse(listPage.isActionLinkPresent("t1", ACTION_DOWN));
        assertFalse(listPage.isActionLinkPresent("t2", ACTION_UP));
        assertFalse(listPage.isActionLinkPresent("t2", ACTION_DOWN));
    }

    public void testOrderLinksNotPresentWithNoWritePermission() throws Exception
    {
        String path = orderPrelude();

        xmlRpcHelper.insertTrivialUser(random);
        browser.login(random, "");
        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(path));
        assertFalse(listPage.isOrderColumnPresent(2));
        assertItemPresent(listPage, "p1", null);
        assertItemPresent(listPage, "p2", null);
        assertItemPresent(listPage, "p3", null);
        assertFalse(listPage.isActionLinkPresent("p1", ACTION_UP));
        assertFalse(listPage.isActionLinkPresent("p1", ACTION_DOWN));
        assertFalse(listPage.isActionLinkPresent("p2", ACTION_UP));
        assertFalse(listPage.isActionLinkPresent("p2", ACTION_DOWN));
        assertFalse(listPage.isActionLinkPresent("p3", ACTION_UP));
        assertFalse(listPage.isActionLinkPresent("p3", ACTION_DOWN));
    }

    public void testInheritedOrder() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);

        insertProperty(parentPath, "p1");
        insertProperty(parentPath, "p2");

        browser.loginAsAdmin();
        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(childPath));
        assertFalse(listPage.isOrderInheritedPresent());
        assertFalse(listPage.isOrderOverriddenPresent());

        xmlRpcHelper.setConfigOrder(getPropertiesPath(parentPath), "p2", "p1");
        listPage.openAndWaitFor();
        assertTrue(listPage.isOrderInheritedPresent());
        assertFalse(listPage.isOrderOverriddenPresent());
    }

    public void testOverriddenOrder() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);

        insertProperty(parentPath, "p1");
        insertProperty(parentPath, "p2");

        xmlRpcHelper.setConfigOrder(getPropertiesPath(parentPath), "p2", "p1");
        xmlRpcHelper.setConfigOrder(getPropertiesPath(childPath), "p1", "p2");

        browser.loginAsAdmin();
        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(childPath));
        assertFalse(listPage.isOrderInheritedPresent());
        assertTrue(listPage.isOrderOverriddenPresent());
    }

    public void testWizardOverridingConfigured() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        xmlRpcHelper.insertSimpleProject(parentName, true);

        browser.loginAsAdmin();
        addInheritingProject(parentName, childName);

        ProjectHierarchyPage childHierarchyPage = browser.createPage(ProjectHierarchyPage.class, childName, false);
        childHierarchyPage.waitFor();
        ProjectConfigPage configPage = childHierarchyPage.clickConfigure();
        configPage.waitFor();
        browser.waitForLocator(configPage.getTreeLinkLocator("subversion configuration"));
        CompositePage scmPage = configPage.clickComposite("scm", "subversion configuration");
        scmPage.waitFor();
        SubversionForm subversionForm = browser.createForm(SubversionForm.class);
        subversionForm.waitFor();
        assertFormElements(subversionForm, Constants.TRIVIAL_ANT_REPOSITORY, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public void testWizardOverridingConfiguredWithNonDefaultName() throws Exception
    {
        final String NEW_RECIPE_NAME = "edited";
        final String NEW_COMMAND_NAME = "edited";

        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = xmlRpcHelper.insertSimpleProject(parentName, true);
        String recipePath = getPath(parentPath, Constants.Project.TYPE, Constants.Project.MultiRecipeType.RECIPES, Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME);
        Hashtable<String, Object> recipe = xmlRpcHelper.getConfig(recipePath);
        recipe.put("name", NEW_RECIPE_NAME);
        @SuppressWarnings({"unchecked"})
        Hashtable<String, Object> command = (Hashtable<String, Object>) ((Hashtable<String, Object>) recipe.get(Constants.Project.MultiRecipeType.Recipe.COMMANDS)).get(Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND);
        command.put("name", NEW_COMMAND_NAME);
        xmlRpcHelper.saveConfig(recipePath, recipe, true);

        browser.loginAsAdmin();
        addInheritingProject(parentName, childName);

        String childRecipesPath = getPath(PROJECTS_SCOPE, childName, Constants.Project.TYPE, Constants.Project.MultiRecipeType.RECIPES);
        assertEquals(asList(NEW_RECIPE_NAME), new LinkedList<String>(xmlRpcHelper.getConfigListing(childRecipesPath)));

        String childCommandsPath = getPath(childRecipesPath, NEW_RECIPE_NAME, Constants.Project.MultiRecipeType.Recipe.COMMANDS);
        assertEquals(asList(NEW_COMMAND_NAME), new LinkedList<String>(xmlRpcHelper.getConfigListing(childCommandsPath)));
        
        browser.openAndWaitFor(CompositePage.class, getPath(childCommandsPath, NEW_COMMAND_NAME));
        AntCommandForm form = browser.createForm(AntCommandForm.class);
        assertTrue(form.isFormPresent());
    }

    private void addInheritingProject(String parentName, String childName)
    {
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, parentName, true);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser);
        projectState.waitFor();
        projectState.nextFormElements(childName, null, null);
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser);
        subversionState.waitFor();
        subversionState.nextFormElements(null, null, null, null, null, null);
        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(browser);
        antState.waitFor();
        antState.finishFormElements(null, null, null, null, null, null);
    }

    public void testWizardMultiRecipeProject() throws Exception
    {
        browser.loginAsAdmin();

        runAddProjectWizard(new DefaultProjectWizardDriver(GLOBAL_PROJECT_NAME, random, false)
        {
            @Override
            public String selectType()
            {
                return ProjectTypeSelectionConfiguration.TYPE_MULTI_STEP;
            }
        });

        ProjectHierarchyPage hierarchyPage = browser.createPage(ProjectHierarchyPage.class, random, false);
        hierarchyPage.waitFor();

        String projectTypePath = getPath(PROJECTS_SCOPE, random, Constants.Project.TYPE);
        Hashtable<String, Object> type = xmlRpcHelper.getConfig(projectTypePath);
        assertEquals(SYMBOLIC_NAME_MULTI_RECIPE, type.get(XmlRpcHelper.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingMultiRecipeProject() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";
        
        xmlRpcHelper.insertProject(parent, GLOBAL_PROJECT_NAME, true, xmlRpcHelper.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), xmlRpcHelper.getMultiRecipeTypeConfig());

        browser.loginAsAdmin();

        ProjectHierarchyPage parentHierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, parent, true);
        parentHierarchyPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser);
        projectState.waitFor();
        projectState.nextFormElements(child, null, null);

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser);
        subversionState.waitFor();
        subversionState.finishFormElements(subversionState.getUnchangedValues());

        ProjectHierarchyPage childHierarchyPage = browser.createPage(ProjectHierarchyPage.class, child, false);
        childHierarchyPage.waitFor();

        String childTypePath = getPath(PROJECTS_SCOPE, child, Constants.Project.TYPE);
        Hashtable<String, Object> type = xmlRpcHelper.getConfig(childTypePath);
        assertEquals(SYMBOLIC_NAME_MULTI_RECIPE, type.get(XmlRpcHelper.SYMBOLIC_NAME_KEY));
    }

    public void testCustomProject() throws Exception
    {
        browser.loginAsAdmin();

        final String pulseFileString = IOUtils.inputStreamToString(getInput("pulseFile", "xml"));

        runAddProjectWizard(new DefaultProjectWizardDriver(GLOBAL_PROJECT_NAME, random, false)
        {
            @Override
            public String selectType()
            {
                return ProjectTypeSelectionConfiguration.TYPE_CUSTOM;
            }

            @Override
            public void typeState(AddProjectWizard.TypeState form)
            {
                form.finishFormElements(pulseFileString);
            }
        });

        ProjectHierarchyPage hierarchyPage = browser.createPage(ProjectHierarchyPage.class, random, false);
        hierarchyPage.waitFor();

        String projectTypePath = getPath(PROJECTS_SCOPE, random, Constants.Project.TYPE);
        Hashtable<String, Object> type = xmlRpcHelper.getConfig(projectTypePath);
        assertEquals(SYMBOLIC_NAME_CUSTOM, type.get(XmlRpcHelper.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingCustomProject() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";

        String pulseFileString = IOUtils.inputStreamToString(getInput("pulseFile", "xml"));
        xmlRpcHelper.insertProject(parent, GLOBAL_PROJECT_NAME, true, xmlRpcHelper.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), xmlRpcHelper.getCustomTypeConfig(pulseFileString));

        browser.loginAsAdmin();

        ProjectHierarchyPage parentHierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, parent, true);
        parentHierarchyPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser);
        projectState.waitFor();
        projectState.nextFormElements(child, null, null);

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser);
        subversionState.waitFor();
        subversionState.nextFormElements(subversionState.getUnchangedValues());

        AddProjectWizard.CustomTypeState customTypeState = new AddProjectWizard.CustomTypeState(browser);
        customTypeState.waitFor();
        customTypeState.finishFormElements(pulseFileString);
        
        ProjectHierarchyPage childHierarchyPage = browser.createPage(ProjectHierarchyPage.class, child, false);
        childHierarchyPage.waitFor();

        String childTypePath = getPath(PROJECTS_SCOPE, child, Constants.Project.TYPE);
        Hashtable<String, Object> type = xmlRpcHelper.getConfig(childTypePath);
        assertEquals(SYMBOLIC_NAME_CUSTOM, type.get(XmlRpcHelper.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingScrubRequired() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        xmlRpcHelper.insertSimpleProject(parentName, true);

        browser.loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, parentName, true);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser);
        projectState.waitFor();
        projectState.nextFormElements(childName, null, null);
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser);
        subversionState.waitFor();
        subversionState.nextFormElements("", null, null, null, null, null);
        assertTrue(subversionState.isFormPresent());
        assertTextPresent("url requires a value");
    }

    public void testDefaultProjectConfigCreated()
    {
        browser.loginAsAdmin();
        addProject(random, false);

        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPath(PROJECTS_SCOPE, random, "stages"));
        assertItemPresent(listPage, "default", null, "view", "delete");

        listPage = browser.openAndWaitFor(ListPage.class, getPath(PROJECTS_SCOPE, random, "triggers"));
        assertItemPresent(listPage, "scm trigger", null, "view", "delete", "pause");
    }

    public void testDefaultProjectConfigNotCreatedWhenAlreadyInherited()
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        browser.loginAsAdmin();
        addProject(parentName, true, GLOBAL_PROJECT_NAME, false);
        addInheritingProject(parentName, childName);

        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPath(PROJECTS_SCOPE, childName, "stages"));
        assertItemPresent(listPage, "default", ListPage.ANNOTATION_INHERITED, "view", "delete");

        listPage = browser.openAndWaitFor(ListPage.class, getPath(PROJECTS_SCOPE, childName, "triggers"));
        assertItemPresent(listPage, "scm trigger", ListPage.ANNOTATION_INHERITED, "view", "delete", "pause");
    }

    public void testValidationInWizard()
    {
        browser.loginAsAdmin();
        ProjectHierarchyPage projectsPage = browser.openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);
        projectsPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser);
        projectState.waitFor();
        assertTextPresent("add new project");
        assertTextNotPresent("add new project template");
        assertTrue(projectState.isMarkedRequired("name"));
        projectState.nextFormElements(random, "", "");

        SelectTypeState scmTypeState = new SelectTypeState(browser);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        // URL is required.
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser);
        subversionState.waitFor();
        assertTrue(subversionState.isMarkedRequired("url"));
        subversionState.nextFormElements("", null, null, null, null, "CLEAN_CHECKOUT");
        assertTrue(subversionState.isFormPresent());
        assertTextPresent("url requires a value");
        subversionState.cancelFormElements(null, null, null, null, null, "CLEAN_CHECKOUT");
    }

    public void testTemplateValidationInWizard()
    {
        browser.loginAsAdmin();
        ProjectHierarchyPage projectsPage = browser.openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);
        projectsPage.clickAddTemplate();

        // Despite the fact we are adding a template, we must specify the
        // name.
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser);
        projectState.waitFor();
        assertTextPresent("add new project template");
        assertTrue(projectState.isMarkedRequired("name"));
        projectState.nextFormElements("", "", "");
        assertTrue(projectState.isFormPresent());
        assertTextPresent("name is a required field");
        projectState.nextFormElements(random, "", "");

        SelectTypeState scmTypeState = new SelectTypeState(browser);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        // Here, we should get away without having a URL.
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser);
        subversionState.waitFor();
        assertFalse(subversionState.isMarkedRequired("url"));
        subversionState.nextFormElements("", null, null, null, null, "CLEAN_CHECKOUT");

        ProjectTypeSelectState projectTypeState = new ProjectTypeSelectState(browser);
        projectTypeState.waitFor();
        projectTypeState.nextFormElements(ProjectTypeSelectionConfiguration.TYPE_SINGLE_STEP, "zutubi.antCommandConfig");

        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(browser);
        antState.waitFor();
        antState.finishFormElements("build", null, "build.xml", null, null, null);

        ProjectHierarchyPage hierarchyPage = browser.createPage(ProjectHierarchyPage.class, random, true);
        hierarchyPage.waitFor();
    }

    public void testValidationOnSave() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, false);

        browser.loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);

        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        configPage.clickComposite("scm", "subversion configuration");

        SubversionForm subversionForm = browser.createForm(SubversionForm.class);
        subversionForm.waitFor();
        assertTrue(subversionForm.isMarkedRequired("url"));
        subversionForm.applyNamedFormElements(asPair("url", ""));
        subversionForm.waitFor();
        assertTextPresent("url requires a value");
    }

    public void testTemplateValidationOnSave() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, true);

        browser.loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, true);

        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        String treeLink = configPage.getTreeLinkLocator("subversion configuration");
        browser.waitForLocator(treeLink);
        browser.click(treeLink);

        SubversionForm subversionForm = browser.createForm(SubversionForm.class);
        subversionForm.waitFor();

        subversionForm.applyNamedFormElements(asPair("url", ""));
        subversionForm.waitFor();
        assertFalse(subversionForm.isMarkedRequired("url"));
        assertTextNotPresent("url requires a value");
    }

    public void testInvalidPathNonExistantScope() throws Exception
    {
        browser.loginAsAdmin();
        browser.open(urls.admin() + "scopish/");
        assertGenericError("Invalid path 'scopish': references non-existant root scope 'scopish'");
    }

    public void testInvalidPathNonExistantCollectionItem() throws Exception
    {
        browser.loginAsAdmin();
        browser.open(urls.adminUser("nosuchuser"));
        assertGenericError("Invalid path 'users/nosuchuser': references unknown child 'nosuchuser' of collection");
    }

    public void testInvalidPathNonExistantTemplateItem() throws Exception
    {
        browser.loginAsAdmin();
        browser.open(urls.adminProject("nosuchproject"));
        assertGenericError("Invalid path 'projects/nosuchproject': references unknown child 'nosuchproject' of collection");
    }

    public void testInvalidPathNonExistantProperty() throws Exception
    {
        browser.loginAsAdmin();
        browser.open(urls.adminProject(GLOBAL_PROJECT_NAME) + "nosuchproperty/");
        assertGenericError("Invalid path 'projects/global project template/nosuchproperty': references unknown property 'nosuchproperty' of type 'zutubi.projectConfig'");
    }

    public void testInvalidPathSimpleProperty() throws Exception
    {
        browser.loginAsAdmin();
        browser.open(urls.adminProject(GLOBAL_PROJECT_NAME) + "name/");
        assertGenericError("Invalid path 'projects/global project template/name': references non-complex type");
    }

    public void testInstanceErrorsDisplayed() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        browser.loginAsAdmin();
        browser.openAndWaitFor(CompositePage.class, projectPath);
        assertTextPresent("An SCM must be configured to complete this project.");
    }

    public void testSaveNoParentPath() throws Exception
    {
        browser.loginAsAdmin();
        browser.open(urls.adminSettings());
        ServerSettingsForm form = browser.createForm(ServerSettingsForm.class);
        form.waitFor();
        String url = "http://somehelpurl.com/" + random;
        form.applyFormElements(null, null, url, null, null, null, null, null, null);

        form.waitFor();
        assertFormElements(form, null, null, url, null, null, null, null, null, null);
    }

    public void testCancelNoParentPath() throws Exception
    {
        browser.loginAsAdmin();
        browser.open(urls.adminSettings());
        ServerSettingsForm form = browser.createForm(ServerSettingsForm.class);
        form.waitFor();
        String originalUrl = form.getFieldValue("baseHelpUrl");
        form.resetFormElements(null, null, "http://somehelpurl.com/" + random, null, null, null, null, null, null);

        form.waitFor();
        assertFormElements(form, null, null, originalUrl, null, null, null, null, null, null);
    }

    public void testComboListing()
    {
        addProject(random, true);
        checkListedRecipes("", "default");
    }

    public void testComboInvalidVersionedProject() throws Exception
    {
        Hashtable<String, Object> versionedType = xmlRpcHelper.createDefaultConfig(VersionedTypeConfiguration.class);
        versionedType.put(Constants.Project.VersionedType.PULSE_FILE_NAME, "invalid.xml");
        xmlRpcHelper.insertProject(random, GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), versionedType);

        checkListedRecipes("");
    }

    public void testPunctuatedProjectName()
    {
        browser.loginAsAdmin();

        String punctuatedName = ".;.,." + random;
        addProject(punctuatedName, false);

        // Check the hierarchy, config and such.
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, punctuatedName, false);
        assertTextNotPresent("invalid");
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        configPage.clickBuildOptionsAndWait();
    }
    
    public void testMoveLinkPresence() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String template = random + "-template";
        String concrete = random + "-concrete";
        
        xmlRpcHelper.insertTrivialUser(user);
        xmlRpcHelper.insertTrivialProject(template, true);
        xmlRpcHelper.insertTrivialProject(concrete, false);

        browser.loginAsAdmin();

        ProjectHierarchyPage globalPage = browser.openAndWaitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        assertFalse(globalPage.isMovePresent());

        ProjectHierarchyPage templatePage = browser.openAndWaitFor(ProjectHierarchyPage.class, template, true);
        assertTrue(templatePage.isMovePresent());

        ProjectHierarchyPage concretePage = browser.openAndWaitFor(ProjectHierarchyPage.class, concrete, false);
        assertTrue(concretePage.isMovePresent());

        browser.logout();
        browser.login(user, "");

        templatePage.openAndWaitFor();
        assertFalse(templatePage.isMovePresent());
        
        concretePage.openAndWaitFor();
        assertFalse(concretePage.isMovePresent());
    }
    
    public void testMove() throws Exception
    {
        String random = randomName();
        String newTemplateParent = random + "-newtp";
        String toMove = random + "-tomove";

        xmlRpcHelper.insertTrivialProject(newTemplateParent, true);
        String toMovePath = xmlRpcHelper.insertSimpleProject(toMove, ProjectManager.GLOBAL_PROJECT_NAME, false);

        browser.loginAsAdmin();

        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, toMove, false);
        hierarchyPage.clickMove();
        
        MoveForm moveForm = new MoveForm(browser);
        moveForm.waitFor();
        moveForm.moveFormElements(newTemplateParent);
        
        hierarchyPage.waitFor();
        assertTrue(hierarchyPage.isTreeItemPresent(toMove));
        assertEquals(newTemplateParent, xmlRpcHelper.getTemplateParent(toMovePath));
    }

    public void testMoveWithConfirmation() throws Exception
    {
        String random = randomName();
        String newTemplateParent = random + "-newtp";
        String toMove = random + "-tomove";

        xmlRpcHelper.insertProject(newTemplateParent, ProjectManager.GLOBAL_PROJECT_NAME, true, xmlRpcHelper.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), xmlRpcHelper.createVersionedConfig("path"));
        String toMovePath = xmlRpcHelper.insertSimpleProject(toMove, ProjectManager.GLOBAL_PROJECT_NAME, false);

        browser.loginAsAdmin();

        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, toMove, false);
        hierarchyPage.clickMove();
        
        MoveForm moveForm = new MoveForm(browser);
        moveForm.waitFor();
        moveForm.moveFormElements(newTemplateParent);
        
        MoveConfirmPage confirmPage = new MoveConfirmPage(browser, urls, toMovePath);
        confirmPage.waitFor();
        assertEquals(asList(getPath(toMovePath, "type")), confirmPage.getDeletedPaths());
        confirmPage.clickMove();        
        
        hierarchyPage.waitFor();
        assertTrue(hierarchyPage.isTreeItemPresent(toMove));
        assertEquals(newTemplateParent, xmlRpcHelper.getTemplateParent(toMovePath));
    }
    
    public void testTemplateNavigation() throws Exception
    {
        final String PROCESSOR_NAME = "xcodebuild output processor";

        String templateName = random + "-template";
        String concreteName = random + "-concrete";

        xmlRpcHelper.insertTrivialProject(templateName, true);
        xmlRpcHelper.insertSimpleProject(concreteName, templateName, false);

        browser.loginAsAdmin();

        CompositePage rootPage = browser.openAndWaitFor(CompositePage.class, getPath(PROJECTS_SCOPE, GLOBAL_PROJECT_NAME, Constants.Project.POST_PROCESSORS, PROCESSOR_NAME));
        assertFalse(rootPage.isAncestorNavigationPresent());
        assertTrue(rootPage.isDescendantNavigationPresent());
        
        CompositePage concretePage = rootPage.navigateToDescendantAndWait(concreteName);
        assertTrue(concretePage.isAncestorNavigationPresent());
        assertEquals(asList(templateName, GLOBAL_PROJECT_NAME), concretePage.getAncestorNavigationOptions());
        assertFalse(concretePage.isDescendantNavigationPresent());
        
        final CompositePage templatePage = concretePage.navigateToAncestorAndWait(templateName);
        assertTrue(templatePage.isAncestorNavigationPresent());
        assertEquals(asList(GLOBAL_PROJECT_NAME), templatePage.getAncestorNavigationOptions());
        assertTrue(templatePage.isDescendantNavigationPresent());
        assertEquals(asList(concreteName), templatePage.getDescendantNavigationOptions());

        // Check the tree state is correct (CIB-2566).
        templatePage.clickCollapseAll();
        AcceptanceTestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return !templatePage.isTreeLinkVisible("properties");
            }
        }, SeleniumBrowser.WAITFOR_TIMEOUT, "tree to collapse");
    }
    
    public void testPasswordSuppression() throws Exception
    {
        final String FIELD_PASSWORD = "password";

        Hashtable<String, Object> p4Config = PerforceUtils.createSpecConfig(xmlRpcHelper);
        String projectName = random;
        xmlRpcHelper.insertSingleCommandProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false, p4Config, xmlRpcHelper.getAntConfig());
        
        browser.loginAsAdmin();
        browser.open(urls.adminProject(WebUtils.uriComponentEncode(projectName)) + "scm/");
        PerforceForm form = new PerforceForm(browser);
        form.waitFor();
        assertEquals(ToveUtils.SUPPRESSED_PASSWORD, form.getFieldValue(FIELD_PASSWORD));
        
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkFormElementsAndWait();
        assertTrue(checkForm.isResultOk());
        
        form.applyNamedFormElements(asPair(FIELD_PASSWORD, ""));
        form.waitFor();
        assertEquals("", form.getFieldValue(FIELD_PASSWORD));

        checkForm.checkFormElementsAndWait();
        assertFalse(checkForm.isResultOk());
        
        form.applyNamedFormElements(asPair(FIELD_PASSWORD, "broken"));
        form.waitFor();
        assertEquals(ToveUtils.SUPPRESSED_PASSWORD, form.getFieldValue(FIELD_PASSWORD));
        
        checkForm.checkFormElementsAndWait();
        assertFalse(checkForm.isResultOk());

        form.applyNamedFormElements(asPair(FIELD_PASSWORD, P4PASSWD));
        form.waitFor();
        assertEquals(ToveUtils.SUPPRESSED_PASSWORD, form.getFieldValue(FIELD_PASSWORD));
        
        checkForm.checkFormElementsAndWait();
        assertTrue(checkForm.isResultOk());
    }
    
    private void checkListedRecipes(String... expectedRecipes)
    {
        browser.loginAsAdmin();
        browser.open(urls.adminProject(WebUtils.uriComponentEncode(random)) + Constants.Project.STAGES + "/" + ProjectConfigurationWizard.DEFAULT_STAGE + "/");

        BuildStageForm stageForm = browser.createForm(BuildStageForm.class, false);
        stageForm.waitFor();
        String[] stages = stageForm.getComboBoxOptions(Constants.Project.Stage.RECIPE);
        assertEquals(asList(expectedRecipes), asList(stages));
    }

    private void insertProperty(String projectPath) throws Exception
    {
        insertProperty(projectPath, "p1");
    }

    private void insertProperty(String projectPath, String name) throws Exception
    {
        Hashtable<String, Object> property = xmlRpcHelper.createEmptyConfig(ResourcePropertyConfiguration.class);
        property.put("name", name);
        xmlRpcHelper.insertConfig(getPropertiesPath(projectPath), property);
    }

    private String getPropertiesPath(String projectPath)
    {
        return getPath(projectPath, "properties");
    }
}