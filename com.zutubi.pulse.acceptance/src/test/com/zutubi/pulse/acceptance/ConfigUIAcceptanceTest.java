package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.admin.*;
import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.acceptance.support.PerforceUtils;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.CustomChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

import java.util.*;

import static com.zutubi.pulse.acceptance.pages.admin.ListPage.ANNOTATION_INHERITED;
import static com.zutubi.pulse.acceptance.pages.admin.ListPage.ANNOTATION_NONE;
import static com.zutubi.pulse.acceptance.support.PerforceUtils.P4PASSWD;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfigurationActions.ACTION_PAUSE;
import static com.zutubi.tove.security.AccessManager.ACTION_DELETE;
import static com.zutubi.tove.security.AccessManager.ACTION_VIEW;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static com.zutubi.util.CollectionUtils.asPair;
import static com.zutubi.util.WebUtils.uriComponentEncode;
import static java.util.Arrays.asList;

/**
 * Acceptance tests that verify operation of the configuration UI by trying
 * some real cases against a running server.
 */
public class ConfigUIAcceptanceTest extends AcceptanceTestBase
{
    public static final String SPECIAL_CHARACTERS = " #%&<>'\"()!@";

    private static final String CHECK_PROJECT = "config-check-project" + SPECIAL_CHARACTERS;

    private static final String ACTION_DOWN = "down";
    private static final String ACTION_UP   = "up";
    private static final String SYMBOLIC_NAME_MULTI_RECIPE = "zutubi.multiRecipeTypeConfig";
    private static final String SYMBOLIC_NAME_CUSTOM       = "zutubi.customTypeConfig";

    protected void setUp() throws Exception
    {
        super.setUp();
        random = randomName() + SPECIAL_CHARACTERS;
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testEmptyOptionsAddedForSelects() throws Exception
    {
        // When configuring a template and a single select is shown, that
        // single select should have an empty option added.
        getBrowser().loginAsAdmin();
        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.addProject(random, true, GLOBAL_PROJECT_NAME);
        getBrowser().open(urls.adminProject(uriComponentEncode(random)) + Constants.Project.BOOTSTRAP);
        BootstrapForm form = getBrowser().createForm(BootstrapForm.class);
        form.waitFor();
        List<String> options = form.getComboBoxOptions(Constants.Project.Bootstrap.CHECKOUT_TYPE);
        assertEquals("", options.get(0));
    }

    public void testDeleteListItemFromTemplateChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        String childPath = rpcClient.RemoteApi.insertSimpleProject(childName, parentName, false);
        String labelsPath = getPath(childPath, "labels");

        getBrowser().loginAsAdmin();
        ListPage labelsPage = getBrowser().openAndWaitFor(ListPage.class, labelsPath);
        labelsPage.clickAdd();

        LabelForm labelForm = getBrowser().createForm(LabelForm.class);
        labelForm.waitFor();
        labelForm.finishFormElements("my-label");

        labelsPage.waitFor();
        String baseName = getNewestListItem(labelsPath);
        labelsPage.waitForItem(baseName, ANNOTATION_NONE, ACTION_VIEW, ACTION_DELETE);
        DeleteConfirmPage deleteConfirmPage = labelsPage.clickDelete(baseName);
        deleteConfirmPage.waitFor();
        labelsPage = deleteConfirmPage.confirmDeleteListItem();

        assertFalse(labelsPage.isItemPresent(baseName));
    }

    public void testCancelAddListItem() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertTrivialProject(random, false);

        getBrowser().loginAsAdmin();
        ListPage labelsPage = getBrowser().openAndWaitFor(ListPage.class, getPath(projectPath, "labels"));
        labelsPage.clickAdd();

        LabelForm labelForm = getBrowser().createForm(LabelForm.class);
        labelForm.waitFor();
        labelForm.cancelFormElements("my-label");

        labelsPage.waitFor();
        assertFalse(labelsPage.isItemPresent("my-label"));
    }

    public void testCancelViewListItem() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertTrivialProject(random, false);
        String labelBaseName = insertLabel(projectPath);

        getBrowser().loginAsAdmin();
        ListPage labelsPage = getBrowser().openAndWaitFor(ListPage.class, getPath(projectPath, "labels"));
        labelsPage.clickView(labelBaseName);

        LabelForm labelForm = getBrowser().createForm(LabelForm.class);
        labelForm.waitFor();
        labelForm.cancelFormElements("");

        labelsPage.waitFor();
        labelsPage.waitForItem(labelBaseName, ANNOTATION_NONE);
    }

    private String insertLabel(String projectPath) throws Exception
    {
        Hashtable<String, Object> label = rpcClient.RemoteApi.createEmptyConfig(LabelConfiguration.class);
        label.put("label", "test");
        return PathUtils.getBaseName(rpcClient.RemoteApi.insertConfig(getPath(projectPath, "labels"), label));
    }

    public void testCheckForm() throws Exception
    {
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(CHECK_PROJECT);
        getBrowser().open(urls.adminProject(WebUtils.uriComponentEncode(CHECK_PROJECT)) + "scm/");
        SubversionForm form = getBrowser().createForm(SubversionForm.class);
        form.waitFor();
        form.setFieldValue("url", "svn://localhost:3088/");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkFormElementsAndWait();
        assertTrue(checkForm.isResultOk());
        assertEquals("configuration ok", checkForm.getResultMessage());
    }

    public void testCheckFormFailure() throws Exception
    {
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(CHECK_PROJECT);
        getBrowser().open(urls.adminProject(WebUtils.uriComponentEncode(CHECK_PROJECT)) + "scm/");
        SubversionForm form = getBrowser().createForm(SubversionForm.class);
        form.waitFor();
        form.setFieldValue("url", "svn://localhost:9999/foo");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkFormElementsAndWait();
        assertFalse(checkForm.isResultOk());
        assertTrue(checkForm.getResultMessage().contains("connection refused"));
    }

    public void testCheckFormValidationFailure() throws Exception
    {
        getBrowser().loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(CHECK_PROJECT);
        getBrowser().open(urls.adminProject(uriComponentEncode(CHECK_PROJECT)) + "scm/");
        SubversionForm form = getBrowser().createForm(SubversionForm.class);
        form.waitFor();
        form.setFieldValue("url", "");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkFormElementsAndWait();
        assertFalse(checkForm.isResultOk());
        assertEquals("unable to check configuration due to validation errors", checkForm.getResultMessage());
        getBrowser().waitForTextPresent("url requires a value");
    }

    public void testCheckFormCheckFieldValidationFailure() throws Exception
    {
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.admin() + "settings/email/");
        EmailSettingsForm form = getBrowser().createForm(EmailSettingsForm.class);
        form.waitFor();
        EmailSettingsCheckForm checkForm = new EmailSettingsCheckForm(form);
        checkForm.checkFormElementsAndWait("");
        assertFalse(checkForm.isResultOk());
        assertEquals("unable to check configuration due to validation errors", checkForm.getResultMessage());
        getBrowser().waitForTextPresent("recipient address requires a value");
    }

    public void testCheckFormInWizard() throws Exception
    {
        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, false);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(getBrowser());
        projectState.waitFor();
        projectState.nextFormElements(random, null, null);

        SelectTypeState scmTypeState = new SelectTypeState(getBrowser());
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(getBrowser());
        subversionState.waitFor();

        subversionState.setFieldValue("url", "svn://localhost:3088/");
        CheckForm checkForm = new CheckForm(subversionState);
        checkForm.checkFormElementsAndWait();
        assertTrue(checkForm.isResultOk());
        assertEquals("configuration ok", checkForm.getResultMessage());

        subversionState.cancelNamedFormElements();
    }

    public void testClearItemPicker() throws Exception
    {
        rpcClient.RemoteApi.ensureProject(random);

        getBrowser().loginAsAdmin();
        ProjectConfigPage configPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, random, false);
        ListPage listPage = configPage.clickCollection("permissions", "permissions");
        listPage.waitFor();
        assertEquals("[view]", listPage.getCellContent(0, 1));

        By viewLink = By.linkText("view");
        getBrowser().click(viewLink);
        ProjectAclForm form = getBrowser().createForm(ProjectAclForm.class);
        form.waitFor();
        assertTrue(form.checkFormValues("all users", "view"));
        form.saveFormElements(null, "");
        listPage.waitFor();

        assertEquals("[]", listPage.getCellContent(0, 1));
        getBrowser().click(viewLink);
        form = getBrowser().createForm(ProjectAclForm.class);
        form.waitFor();
        assertTrue(form.checkFormValues("all users", ""));
    }

    public void testClearMultiSelect() throws Exception
    {
        getBrowser().loginAsAdmin();

        getBrowser().open(urls.adminGroup(UserManager.ANONYMOUS_USERS_GROUP_NAME));
        BuiltinGroupForm groupForm = getBrowser().createForm(BuiltinGroupForm.class);
        groupForm.waitFor();
        groupForm.applyFormElements(null, ServerPermission.PERSONAL_BUILD.toString());
        groupForm.waitFor();
        assertTrue(groupForm.checkFormValues(null, ServerPermission.PERSONAL_BUILD.toString()));

        groupForm.applyFormElements(null, "");
        groupForm.waitFor();
        assertTrue(groupForm.checkFormValues(null, ""));
    }

    public void testNameValidationDuplicate() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertTrivialProject(random, false);
        String propertiesPath = getPropertiesPath(projectPath);
        insertProperty(projectPath);

        getBrowser().loginAsAdmin();
        ListPage propertiesPage = getBrowser().openAndWaitFor(ListPage.class, propertiesPath);
        propertiesPage.clickAdd();

        ResourcePropertyForm form = getBrowser().createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.waitFor();
        getBrowser().waitForTextPresent("name \"p1\" is already in use, please select another name");
    }

    public void testNameValidationDuplicateInherited() throws Exception
    {
        String parentName = random + "-parent";
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        insertProperty(parentPath);
        String childPath = rpcClient.RemoteApi.insertTrivialProject(random + "-child", parentName, false);

        getBrowser().loginAsAdmin();
        ListPage propertiesPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(childPath));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = getBrowser().createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.waitFor();
        getBrowser().waitForTextPresent("name \"p1\" is already in use, please select another name");
    }

    public void testNameValidationDuplicateInDescendant() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        String childPath = rpcClient.RemoteApi.insertTrivialProject(childName, parentName, false);
        insertProperty(childPath);

        getBrowser().loginAsAdmin();
        ListPage propertiesPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(parentPath));
        assertFalse(propertiesPage.isItemPresent("p1"));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = getBrowser().createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.waitFor();
        getBrowser().waitForTextPresent("name is already in use in descendant \"" + childName + "\", please select another name");
    }

    public void testNameValidationDuplicateInDescendants() throws Exception
    {
        String parentName = random + "-parent";
        String child1Name = random + "-child1";
        String child2Name = random + "-child2";
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        String child1Path = rpcClient.RemoteApi.insertTrivialProject(child1Name, parentName, false);
        String child2Path = rpcClient.RemoteApi.insertTrivialProject(child2Name, parentName, false);
        insertProperty(child1Path);
        insertProperty(child2Path);

        getBrowser().loginAsAdmin();
        ListPage propertiesPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(parentPath));
        assertFalse(propertiesPage.isItemPresent("p1"));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = getBrowser().createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.waitFor();
        getBrowser().waitForTextPresent("name is already in use in descendants [" + child1Name + ", " + child2Name + "], please select another name");
    }

    public void testNameValidationDuplicateInAncestor() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        String childPath = rpcClient.RemoteApi.insertTrivialProject(childName, parentName, false);
        insertProperty(parentPath);

        String childPropertiesPath = getPropertiesPath(childPath);
        rpcClient.RemoteApi.deleteConfig(getPath(childPropertiesPath, "p1"));

        getBrowser().loginAsAdmin();
        ListPage propertiesPage = getBrowser().openAndWaitFor(ListPage.class, childPropertiesPath);
        propertiesPage.waitForItem("p1", ListPage.ANNOTATION_HIDDEN);
        propertiesPage.clickAdd();

        ResourcePropertyForm form = getBrowser().createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.waitFor();
        getBrowser().waitForTextPresent("name is already in use in ancestor \"" + parentName + "\", please select another name");
    }

    public void testCannotConfigureOverriddenPath() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        String childPath = rpcClient.RemoteApi.insertTrivialProject(childName, parentName, false);

        // At this point we should be allowed to configure in the parent
        getBrowser().loginAsAdmin();
        CompositePage compositePage = getBrowser().openAndWaitFor(CompositePage.class, getPath(parentPath, "changeViewer"));
        assertTrue(compositePage.isConfigureLinkPresent());

        String childChangeViewerPath = getPath(childPath, "changeViewer");
        Hashtable<String, Object> changeViewer = rpcClient.RemoteApi.createEmptyConfig(CustomChangeViewerConfiguration.class);
        changeViewer.put("changesetURL", "dummy");
        rpcClient.RemoteApi.insertConfig(childChangeViewerPath, changeViewer);

        // Now the child exists we should no longer be able to configure
        // in the parent.
        compositePage.openAndWaitFor();
        assertFalse(compositePage.isConfigureLinkPresent());
        assertTrue(compositePage.isConfiguredDescendantPresent(childName));
    }

    public void testOrderLinks() throws Exception
    {
        String path = orderPrelude();

        getBrowser().loginAsAdmin();
        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(path));
        listPage.waitForItem("p1", ANNOTATION_NONE, ACTION_DOWN);
        assertFalse(listPage.isActionLinkPresent("p1", ACTION_UP));
        listPage.waitForItem("p2", ANNOTATION_NONE, ACTION_DOWN, ACTION_UP);
        listPage.waitForItem("p3", ANNOTATION_NONE, ACTION_UP);
        assertFalse(listPage.isActionLinkPresent("p3", ACTION_DOWN));
    }

    public void testMoveUp() throws Exception
    {
        String path = orderPrelude();

        getBrowser().loginAsAdmin();
        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(path));
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

        getBrowser().loginAsAdmin();
        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(path));
        assertEquals("p2", listPage.getCellContent(1, 0));
        assertEquals("p3", listPage.getCellContent(2, 0));
        listPage.clickDown("p2");
        listPage.waitFor();
        assertEquals("p2", listPage.getCellContent(2, 0));
        assertEquals("p3", listPage.getCellContent(1, 0));
    }

    private String orderPrelude() throws Exception
    {
        String path = rpcClient.RemoteApi.insertTrivialProject(random, false);
        insertProperty(path, "p1");
        insertProperty(path, "p2");
        insertProperty(path, "p3");
        return path;
    }

    public void testOrderLinksNotPresentForUnorderedCollection() throws Exception
    {
        String path = rpcClient.RemoteApi.insertTrivialProject(random, false);
        Hashtable<String, Object> trigger = rpcClient.RemoteApi.createDefaultConfig(ScmBuildTriggerConfiguration.class);
        trigger.put("name", "t1");
        String triggersPath = getPath(path, "triggers");
        rpcClient.RemoteApi.insertConfig(triggersPath, trigger);
        trigger.put("name", "t2");
        rpcClient.RemoteApi.insertConfig(triggersPath, trigger);

        getBrowser().loginAsAdmin();
        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, triggersPath);
        assertFalse(listPage.isOrderColumnPresent(2));
        listPage.waitForItem("t1", ANNOTATION_NONE);
        listPage.waitForItem("t2", ANNOTATION_NONE);
        assertFalse(listPage.isActionLinkPresent("t1", ACTION_UP));
        assertFalse(listPage.isActionLinkPresent("t1", ACTION_DOWN));
        assertFalse(listPage.isActionLinkPresent("t2", ACTION_UP));
        assertFalse(listPage.isActionLinkPresent("t2", ACTION_DOWN));
    }

    public void testOrderLinksNotPresentWithNoWritePermission() throws Exception
    {
        String path = orderPrelude();

        rpcClient.RemoteApi.insertTrivialUser(random);
        getBrowser().loginAndWait(random, "");
        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(path));
        assertFalse(listPage.isOrderColumnPresent(2));
        listPage.waitForItem("p1", ANNOTATION_NONE);
        listPage.waitForItem("p2", ANNOTATION_NONE);
        listPage.waitForItem("p3", ANNOTATION_NONE);
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
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        String childPath = rpcClient.RemoteApi.insertTrivialProject(childName, parentName, false);

        insertProperty(parentPath, "p1");
        insertProperty(parentPath, "p2");

        getBrowser().loginAsAdmin();
        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(childPath));
        assertFalse(listPage.isOrderInheritedPresent());
        assertFalse(listPage.isOrderOverriddenPresent());

        rpcClient.RemoteApi.setConfigOrder(getPropertiesPath(parentPath), "p2", "p1");
        listPage.openAndWaitFor();
        assertTrue(listPage.isOrderInheritedPresent());
        assertFalse(listPage.isOrderOverriddenPresent());
    }

    public void testOverriddenOrder() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        String childPath = rpcClient.RemoteApi.insertTrivialProject(childName, parentName, false);

        insertProperty(parentPath, "p1");
        insertProperty(parentPath, "p2");

        rpcClient.RemoteApi.setConfigOrder(getPropertiesPath(parentPath), "p2", "p1");
        rpcClient.RemoteApi.setConfigOrder(getPropertiesPath(childPath), "p1", "p2");

        getBrowser().loginAsAdmin();
        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, getPropertiesPath(childPath));
        assertFalse(listPage.isOrderInheritedPresent());
        assertTrue(listPage.isOrderOverriddenPresent());
    }

    public void testWizardOverridingConfigured() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        rpcClient.RemoteApi.insertSimpleProject(parentName, true);

        getBrowser().loginAsAdmin();
        addInheritingProject(parentName, childName);

        ProjectHierarchyPage childHierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, childName, false);
        childHierarchyPage.waitFor();
        ProjectConfigPage configPage = childHierarchyPage.clickConfigure();
        configPage.waitFor();
        getBrowser().waitForElement(By.xpath(configPage.getTreeLinkXPath("subversion configuration")));
        CompositePage scmPage = configPage.clickComposite("scm", "subversion configuration");
        scmPage.waitFor();
        SubversionForm subversionForm = getBrowser().createForm(SubversionForm.class);
        subversionForm.waitFor();
        assertTrue(subversionForm.checkFormValues(Constants.TRIVIAL_ANT_REPOSITORY, null, null, null, null, null, null, null, null, null, null, null, null));
    }

    public void testWizardOverridingConfiguredWithNonDefaultName() throws Exception
    {
        final String NEW_RECIPE_NAME = "edited";
        final String NEW_COMMAND_NAME = "edited";

        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = rpcClient.RemoteApi.insertSimpleProject(parentName, true);
        String recipePath = getPath(parentPath, Constants.Project.TYPE, Constants.Project.MultiRecipeType.RECIPES, Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME);
        Hashtable<String, Object> recipe = rpcClient.RemoteApi.getConfig(recipePath);
        recipe.put("name", NEW_RECIPE_NAME);
        @SuppressWarnings({"unchecked"})
        Hashtable<String, Object> command = (Hashtable<String, Object>) ((Hashtable<String, Object>) recipe.get(Constants.Project.MultiRecipeType.Recipe.COMMANDS)).get(Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND);
        command.put("name", NEW_COMMAND_NAME);
        rpcClient.RemoteApi.saveConfig(recipePath, recipe, true);

        getBrowser().loginAsAdmin();
        addInheritingProject(parentName, childName);

        String childRecipesPath = getPath(PROJECTS_SCOPE, childName, Constants.Project.TYPE, Constants.Project.MultiRecipeType.RECIPES);
        assertEquals(asList(NEW_RECIPE_NAME), new LinkedList<String>(rpcClient.RemoteApi.getConfigListing(childRecipesPath)));

        String childCommandsPath = getPath(childRecipesPath, NEW_RECIPE_NAME, Constants.Project.MultiRecipeType.Recipe.COMMANDS);
        assertEquals(asList(NEW_COMMAND_NAME), new LinkedList<String>(rpcClient.RemoteApi.getConfigListing(childCommandsPath)));
        
        getBrowser().openAndWaitFor(CompositePage.class, getPath(childCommandsPath, NEW_COMMAND_NAME));
        AntCommandForm form = getBrowser().createForm(AntCommandForm.class);
        form.waitFor();
    }

    private void addInheritingProject(String parentName, String childName)
    {
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, parentName, true);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(getBrowser());
        projectState.waitFor();
        projectState.nextFormElements(childName, null, null);
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(getBrowser());
        subversionState.waitFor();
        subversionState.nextNamedFormElements();
        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(getBrowser());
        antState.waitFor();
        antState.finishNamedFormElements();
    }

    public void testWizardMultiRecipeProject() throws Exception
    {
        getBrowser().loginAsAdmin();

        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.runAddProjectWizard(new AddProjectWizard.DefaultProjectWizardDriver(GLOBAL_PROJECT_NAME, random, false)
        {
            @Override
            public String selectType()
            {
                return ProjectTypeSelectionConfiguration.TYPE_MULTI_STEP;
            }
        });

        ProjectHierarchyPage hierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, random, false);
        hierarchyPage.waitFor();

        String projectTypePath = getPath(PROJECTS_SCOPE, random, Constants.Project.TYPE);
        Hashtable<String, Object> type = rpcClient.RemoteApi.getConfig(projectTypePath);
        assertEquals(SYMBOLIC_NAME_MULTI_RECIPE, type.get(RemoteApiClient.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingMultiRecipeProject() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";
        
        rpcClient.RemoteApi.insertProject(parent, GLOBAL_PROJECT_NAME, true, rpcClient.RemoteApi.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), rpcClient.RemoteApi.getMultiRecipeTypeConfig());

        getBrowser().loginAsAdmin();

        ProjectHierarchyPage parentHierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, parent, true);
        parentHierarchyPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(getBrowser());
        projectState.waitFor();
        projectState.nextFormElements(child, null, null);

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(getBrowser());
        subversionState.waitFor();
        subversionState.finishFormElements(subversionState.getUnchangedValues());

        ProjectHierarchyPage childHierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, child, false);
        childHierarchyPage.waitFor();

        String childTypePath = getPath(PROJECTS_SCOPE, child, Constants.Project.TYPE);
        Hashtable<String, Object> type = rpcClient.RemoteApi.getConfig(childTypePath);
        assertEquals(SYMBOLIC_NAME_MULTI_RECIPE, type.get(RemoteApiClient.SYMBOLIC_NAME_KEY));
    }

    public void testCustomProject() throws Exception
    {
        getBrowser().loginAsAdmin();

        final String pulseFileString = readInputFully("pulseFile", "xml");

        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.runAddProjectWizard(new AddProjectWizard.DefaultProjectWizardDriver(GLOBAL_PROJECT_NAME, random, false)
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

        ProjectHierarchyPage hierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, random, false);
        hierarchyPage.waitFor();

        String projectTypePath = getPath(PROJECTS_SCOPE, random, Constants.Project.TYPE);
        Hashtable<String, Object> type = rpcClient.RemoteApi.getConfig(projectTypePath);
        assertEquals(SYMBOLIC_NAME_CUSTOM, type.get(RemoteApiClient.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingCustomProject() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";

        String pulseFileString = readInputFully("pulseFile", "xml");
        rpcClient.RemoteApi.insertProject(parent, GLOBAL_PROJECT_NAME, true, rpcClient.RemoteApi.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), rpcClient.RemoteApi.getCustomTypeConfig(pulseFileString));

        getBrowser().loginAsAdmin();

        ProjectHierarchyPage parentHierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, parent, true);
        parentHierarchyPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(getBrowser());
        projectState.waitFor();
        projectState.nextFormElements(child, null, null);

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(getBrowser());
        subversionState.waitFor();
        subversionState.nextFormElements(subversionState.getUnchangedValues());

        AddProjectWizard.CustomTypeState customTypeState = new AddProjectWizard.CustomTypeState(getBrowser());
        customTypeState.waitFor();
        customTypeState.finishFormElements(pulseFileString);
        
        ProjectHierarchyPage childHierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, child, false);
        childHierarchyPage.waitFor();

        String childTypePath = getPath(PROJECTS_SCOPE, child, Constants.Project.TYPE);
        Hashtable<String, Object> type = rpcClient.RemoteApi.getConfig(childTypePath);
        assertEquals(SYMBOLIC_NAME_CUSTOM, type.get(RemoteApiClient.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingScrubRequired() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        rpcClient.RemoteApi.insertSimpleProject(parentName, true);

        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, parentName, true);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(getBrowser());
        projectState.waitFor();
        projectState.nextFormElements(childName, null, null);
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(getBrowser());
        subversionState.waitFor();
        subversionState.nextNamedFormElements(asPair("url", ""));
        subversionState.waitFor();
        getBrowser().waitForTextPresent("url requires a value");
    }

    public void testDefaultProjectConfigCreated()
    {
        getBrowser().loginAsAdmin();
        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.addProject(random);

        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, getPath(PROJECTS_SCOPE, random, "stages"));
        listPage.waitForItem("default", ANNOTATION_NONE, ACTION_VIEW, ACTION_DELETE);

        listPage = getBrowser().openAndWaitFor(ListPage.class, getPath(PROJECTS_SCOPE, random, "triggers"));
        listPage.waitForItem("scm trigger", ANNOTATION_NONE, ACTION_VIEW, ACTION_DELETE, ACTION_PAUSE);
    }

    public void testDefaultProjectConfigNotCreatedWhenAlreadyInherited()
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        getBrowser().loginAsAdmin();
        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.addProject(parentName, true, GLOBAL_PROJECT_NAME);
        
        addInheritingProject(parentName, childName);

        ListPage listPage = getBrowser().openAndWaitFor(ListPage.class, getPath(PROJECTS_SCOPE, childName, "stages"));
        listPage.waitForItem("default", ANNOTATION_INHERITED, ACTION_VIEW, ACTION_DELETE);

        listPage = getBrowser().openAndWaitFor(ListPage.class, getPath(PROJECTS_SCOPE, childName, "triggers"));
        listPage.waitForItem("scm trigger", ANNOTATION_INHERITED, ACTION_VIEW, ACTION_DELETE, ACTION_PAUSE);
    }

    public void testValidationInWizard()
    {
        getBrowser().loginAsAdmin();
        ProjectHierarchyPage projectsPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);
        projectsPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(getBrowser());
        projectState.waitFor();
        getBrowser().waitForTextPresent("add new project");
        assertFalse(getBrowser().isTextPresent("add new project template"));
        assertTrue(projectState.isMarkedRequired("name"));
        projectState.nextFormElements(random, "", "");

        SelectTypeState scmTypeState = new SelectTypeState(getBrowser());
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        // URL is required.
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(getBrowser());
        subversionState.waitFor();
        assertTrue(subversionState.isMarkedRequired("url"));
        subversionState.nextNamedFormElements(asPair("url", ""));
        subversionState.waitFor();
        getBrowser().waitForTextPresent("url requires a value");
        subversionState.cancelNamedFormElements();
    }

    public void testTemplateValidationInWizard()
    {
        getBrowser().loginAsAdmin();
        ProjectHierarchyPage projectsPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);
        projectsPage.clickAddTemplate();

        // Despite the fact we are adding a template, we must specify the
        // name.
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(getBrowser());
        projectState.waitFor();
        getBrowser().waitForTextPresent("add new project template");
        assertTrue(projectState.isMarkedRequired("name"));
        projectState.nextFormElements("", "", "");
        projectState.waitFor();
        getBrowser().waitForTextPresent("name is a required field");
        projectState.nextFormElements(random, "", "");

        SelectTypeState scmTypeState = new SelectTypeState(getBrowser());
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        // Here, we should get away without having a URL.
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(getBrowser());
        subversionState.waitFor();
        assertFalse(subversionState.isMarkedRequired("url"));
        subversionState.nextNamedFormElements(asPair("url", ""));

        ProjectTypeSelectState projectTypeState = new ProjectTypeSelectState(getBrowser());
        projectTypeState.waitFor();
        projectTypeState.nextFormElements(ProjectTypeSelectionConfiguration.TYPE_SINGLE_STEP, "zutubi.antCommandConfig");

        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(getBrowser());
        antState.waitFor();
        antState.finishFormElements("build", null, "build.xml", null, null, null);

        ProjectHierarchyPage hierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, random, true);
        hierarchyPage.waitFor();
    }

    public void testValidationOnSave() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random, false);

        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);

        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        configPage.clickComposite("scm", "subversion configuration");

        SubversionForm subversionForm = getBrowser().createForm(SubversionForm.class);
        subversionForm.waitFor();
        assertTrue(subversionForm.isMarkedRequired("url"));
        subversionForm.applyNamedFormElements(asPair("url", ""));
        subversionForm.waitFor();
        getBrowser().waitForTextPresent("url requires a value");
    }

    public void testTemplateValidationOnSave() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random, true);

        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, true);

        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        By treeLink = By.xpath(configPage.getTreeLinkXPath("subversion configuration"));
        getBrowser().waitForElement(treeLink);
        getBrowser().click(treeLink);

        SubversionForm subversionForm = getBrowser().createForm(SubversionForm.class);
        subversionForm.waitFor();

        subversionForm.applyNamedFormElements(asPair("url", ""));
        subversionForm.waitFor();
        assertFalse(subversionForm.isMarkedRequired("url"));
        assertFalse(getBrowser().isTextPresent("url requires a value"));
    }

    public void testInvalidPathNonExistentScope() throws Exception
    {
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.admin() + "scopish/");
        waitForActionError("Invalid path 'scopish': references non-existent root scope 'scopish'");
    }

    public void testInvalidPathNonExistentCollectionItem() throws Exception
    {
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminUser("nosuchuser"));
        waitForActionError("Invalid path 'users/nosuchuser': references unknown child 'nosuchuser' of collection");
    }

    public void testInvalidPathNonExistentTemplateItem() throws Exception
    {
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminProject("nosuchproject"));
        waitForActionError("Invalid path 'projects/nosuchproject': references unknown child 'nosuchproject' of collection");
    }

    public void testInvalidPathNonExistentProperty() throws Exception
    {
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminProject(GLOBAL_PROJECT_NAME) + "nosuchproperty/");
        waitForActionError("Invalid path 'projects/global project template/nosuchproperty': references unknown property 'nosuchproperty' of type 'zutubi.projectConfig'");
    }

    public void testInvalidPathSimpleProperty() throws Exception
    {
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminProject(GLOBAL_PROJECT_NAME) + "name/");
        waitForActionError("Invalid path 'projects/global project template/name': references non-complex type");
    }

    private void waitForActionError(String message)
    {
        By actionErrorsId = By.id(IDs.ACTION_ERRORS);
        getBrowser().waitForElement(actionErrorsId);
        assertEquals(message, getBrowser().getText(By.id(IDs.ACTION_ERRORS)));
    }

    public void testInstanceErrorsDisplayed() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertTrivialProject(random, false);
        getBrowser().loginAsAdmin();
        getBrowser().openAndWaitFor(CompositePage.class, projectPath);
        getBrowser().waitForTextPresent("An SCM must be configured to complete this project.");
    }

    public void testSaveNoParentPath() throws Exception
    {
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminSettings());
        ServerSettingsForm form = getBrowser().createForm(ServerSettingsForm.class);
        form.waitFor();
        String url = "http://somehelpurl.com/" + random;
        form.applyFormElements(null, null, url, null, null, null, null, null, null);

        form.waitFor();
        assertTrue(form.checkFormValues(null, null, url, null, null, null, null, null, null));
    }

    public void testCancelNoParentPath() throws Exception
    {
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminSettings());
        ServerSettingsForm form = getBrowser().createForm(ServerSettingsForm.class);
        form.waitFor();
        String originalUrl = (String) form.getFieldValue("baseHelpUrl");
        form.resetFormElements(null, null, "http://somehelpurl.com/" + random, null, null, null, null, null, null);

        form.waitFor();
        assertTrue(form.checkFormValues(null, null, originalUrl, null, null, null, null, null, null));
    }

    public void testComboListing() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        checkListedRecipes("", "default");
    }

    public void testComboInvalidVersionedProject() throws Exception
    {
        Hashtable<String, Object> versionedType = rpcClient.RemoteApi.createDefaultConfig(VersionedTypeConfiguration.class);
        versionedType.put(Constants.Project.VersionedType.PULSE_FILE_NAME, "invalid.xml");
        rpcClient.RemoteApi.insertProject(random, GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), versionedType);

        checkListedRecipes();
    }

    public void testPunctuatedProjectName()
    {
        getBrowser().loginAsAdmin();

        String punctuatedName = ".;.,." + random;

        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.addProject(punctuatedName);

        // Check the hierarchy, config and such.
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, punctuatedName, false);
        assertFalse(getBrowser().isTextPresent("invalid"));
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        configPage.clickBuildOptionsAndWait();
    }
    
    public void testIntroduceParentLinkPresence() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String template = random + "-template";
        String concrete = random + "-concrete";
        
        rpcClient.RemoteApi.insertTrivialUser(user);
        rpcClient.RemoteApi.insertTrivialProject(template, true);
        rpcClient.RemoteApi.insertTrivialProject(concrete, false);

        getBrowser().loginAsAdmin();

        ProjectHierarchyPage globalPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        assertFalse(globalPage.isIntroduceParentPresent());

        ProjectHierarchyPage templatePage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, template, true);
        assertTrue(templatePage.isIntroduceParentPresent());

        ProjectHierarchyPage concretePage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, concrete, false);
        assertTrue(concretePage.isIntroduceParentPresent());

        getBrowser().logout();
        getBrowser().loginAndWait(user, "");

        templatePage.openAndWaitFor();
        assertFalse(templatePage.isIntroduceParentPresent());
        
        concretePage.openAndWaitFor();
        assertFalse(concretePage.isIntroduceParentPresent());
    }
    
    public void testIntroduceParent() throws Exception
    {
        String random = randomName();
        String original = random + "-original";
        String newTemplateParent = random + "-newtp";

        String originalPath = rpcClient.RemoteApi.insertSimpleProject(original, ProjectManager.GLOBAL_PROJECT_NAME, false);

        getBrowser().loginAsAdmin();

        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, original, false);
        hierarchyPage.clickIntroduceParent();
        
        IntroduceParentForm form = new IntroduceParentForm(getBrowser());
        form.waitFor();
        form.okFormElements(newTemplateParent, "false");
        
        hierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, newTemplateParent, false);
        hierarchyPage.waitFor();
        assertTrue(hierarchyPage.isTreeItemPresent(newTemplateParent));
        assertEquals(newTemplateParent, rpcClient.RemoteApi.getTemplateParent(originalPath));
    }
    
    public void testIntroduceParentValidation() throws Exception
    {
        String random = randomName();
        rpcClient.RemoteApi.insertSimpleProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false);

        getBrowser().loginAsAdmin();

        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickIntroduceParent();
        
        IntroduceParentForm form = new IntroduceParentForm(getBrowser());
        form.waitFor();
        form.okFormElements(random, "true");
        
        form.waitFor();
        getBrowser().waitForTextPresent("name \"" + random + "\" is already in use");
        form.cancelFormElements(random, "false");
        hierarchyPage.waitFor();
    }
    
    public void testMoveLinkPresence() throws Exception
    {
        String random = randomName();
        String user = random + "-user";
        String template = random + "-template";
        String concrete = random + "-concrete";
        
        rpcClient.RemoteApi.insertTrivialUser(user);
        rpcClient.RemoteApi.insertTrivialProject(template, true);
        rpcClient.RemoteApi.insertTrivialProject(concrete, false);

        getBrowser().loginAsAdmin();

        ProjectHierarchyPage globalPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        assertFalse(globalPage.isMovePresent());

        ProjectHierarchyPage templatePage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, template, true);
        assertTrue(templatePage.isMovePresent());

        ProjectHierarchyPage concretePage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, concrete, false);
        assertTrue(concretePage.isMovePresent());

        getBrowser().logout();
        getBrowser().loginAndWait(user, "");

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

        rpcClient.RemoteApi.insertTrivialProject(newTemplateParent, true);
        String toMovePath = rpcClient.RemoteApi.insertSimpleProject(toMove, ProjectManager.GLOBAL_PROJECT_NAME, false);

        getBrowser().loginAsAdmin();

        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, toMove, false);
        hierarchyPage.clickMove();
        
        MoveForm moveForm = new MoveForm(getBrowser());
        moveForm.waitFor();
        moveForm.moveFormElements(newTemplateParent);
        
        hierarchyPage.waitFor();
        assertTrue(hierarchyPage.isTreeItemPresent(toMove));
        assertEquals(newTemplateParent, rpcClient.RemoteApi.getTemplateParent(toMovePath));
    }

    public void testMoveWithConfirmation() throws Exception
    {
        String random = randomName();
        String newTemplateParent = random + "-newtp";
        String toMove = random + "-tomove";

        rpcClient.RemoteApi.insertProject(newTemplateParent, ProjectManager.GLOBAL_PROJECT_NAME, true, rpcClient.RemoteApi.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), rpcClient.RemoteApi.createVersionedConfig("path"));
        String toMovePath = rpcClient.RemoteApi.insertSimpleProject(toMove, ProjectManager.GLOBAL_PROJECT_NAME, false);

        getBrowser().loginAsAdmin();

        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, toMove, false);
        hierarchyPage.clickMove();
        
        MoveForm moveForm = new MoveForm(getBrowser());
        moveForm.waitFor();
        moveForm.moveFormElements(newTemplateParent);
        
        MoveConfirmPage confirmPage = new MoveConfirmPage(getBrowser(), urls, toMovePath);
        confirmPage.waitFor();
        assertEquals(asList(getPath(toMovePath, "type")), confirmPage.getDeletedPaths());
        confirmPage.clickMove();        
        
        hierarchyPage.waitFor();
        assertTrue(hierarchyPage.isTreeItemPresent(toMove));
        assertEquals(newTemplateParent, rpcClient.RemoteApi.getTemplateParent(toMovePath));
    }
    
    public void testTemplateNavigation() throws Exception
    {
        final String PROCESSOR_NAME = "xcodebuild output processor";

        String templateName = random + "-template";
        String concreteName = random + "-concrete";

        rpcClient.RemoteApi.insertTrivialProject(templateName, true);
        rpcClient.RemoteApi.insertSimpleProject(concreteName, templateName, false);

        getBrowser().loginAsAdmin();

        CompositePage rootPage = getBrowser().openAndWaitFor(CompositePage.class, getPath(PROJECTS_SCOPE, GLOBAL_PROJECT_NAME, Constants.Project.POST_PROCESSORS, PROCESSOR_NAME));
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
        TestUtils.waitForCondition(new Condition()
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

        Hashtable<String, Object> p4Config = PerforceUtils.createSpecConfig(rpcClient.RemoteApi);
        String projectName = random;
        rpcClient.RemoteApi.insertSingleCommandProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false, p4Config, rpcClient.RemoteApi.getAntConfig());
        
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminProject(WebUtils.uriComponentEncode(projectName)) + "scm/");
        PerforceForm form = new PerforceForm(getBrowser());
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
        getBrowser().loginAsAdmin();
        getBrowser().open(urls.adminProject(WebUtils.uriComponentEncode(random)) + Constants.Project.STAGES + "/" + ProjectConfigurationWizard.DEFAULT_STAGE + "/");

        BuildStageForm stageForm = getBrowser().createForm(BuildStageForm.class, false);
        stageForm.waitFor();
        List<String> stages = stageForm.getComboBoxOptions(Constants.Project.Stage.RECIPE);
        assertEquals(asList(expectedRecipes), stages);
    }

    private void insertProperty(String projectPath) throws Exception
    {
        insertProperty(projectPath, "p1");
    }

    private void insertProperty(String projectPath, String name) throws Exception
    {
        Hashtable<String, Object> property = rpcClient.RemoteApi.createEmptyConfig(ResourcePropertyConfiguration.class);
        property.put("name", name);
        rpcClient.RemoteApi.insertConfig(getPropertiesPath(projectPath), property);
    }

    private String getPropertiesPath(String projectPath)
    {
        return getPath(projectPath, "properties");
    }

    protected String getNewestListItem(String labelsPath) throws Exception
    {
        Vector<String> labels = rpcClient.RemoteApi.call("getConfigListing", labelsPath);
        Collections.sort(labels, new Comparator<String>()
        {
            public int compare(String o1, String o2)
            {
                long h1 = Long.parseLong(o1);
                long h2 = Long.parseLong(o2);
                return (int) (h1 - h2);
            }
        });
        return labels.get(labels.size() - 1);
    }
}