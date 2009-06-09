package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.admin.*;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.CustomChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.io.IOUtils;

import static java.util.Arrays.asList;
import java.util.Hashtable;

/**
 * Acceptance tests that verify operation of the configuration UI by trying
 * some real cases against a running server.
 */
public class ConfigUIAcceptanceTest extends SeleniumTestBase
{
    private static final String CHECK_PROJECT = "config-check-project";

    private static final String ACTION_DOWN = "down";
    private static final String ACTION_UP   = "up";
    private static final String SYMBOLIC_NAME_MULTI_RECIPE = "zutubi.multiRecipeTypeConfig";
    private static final String SYMBOLIC_NAME_CUSTOM       = "zutubi.customTypeConfig";

    protected void setUp() throws Exception
    {
        super.setUp();
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
        loginAsAdmin();
        addProject(random, true, GLOBAL_PROJECT_NAME, false);
        browser.goTo(urls.adminProject(random) + "scm/");
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
        String labelsPath = PathUtils.getPath(childPath, "labels");

        loginAsAdmin();
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

        loginAsAdmin();
        ListPage labelsPage = browser.openAndWaitFor(ListPage.class, PathUtils.getPath(projectPath, "labels"));
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

        loginAsAdmin();
        ListPage labelsPage = browser.openAndWaitFor(ListPage.class, PathUtils.getPath(projectPath, "labels"));
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
        return PathUtils.getBaseName(xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, "labels"), label));
    }

    public void testCheckForm() throws Exception
    {
        loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        browser.goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
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
        loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        browser.goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
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
        loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        browser.goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
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
        loginAsAdmin();
        browser.goTo(urls.admin() + "settings/email/");
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
        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, false);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser.getSelenium());
        projectState.waitFor();
        projectState.nextFormElements(random, null, null);

        SelectTypeState scmTypeState = new SelectTypeState(browser.getSelenium());
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser.getSelenium());
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

        loginAsAdmin();
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
        loginAsAdmin();

        browser.goTo(urls.adminGroup(UserManager.ANONYMOUS_USERS_GROUP_NAME));
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

        loginAsAdmin();
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

        loginAsAdmin();
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(childPath));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = browser.createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null, null);
        assertTrue(form.isFormPresent());
        assertTextPresent("name is already in use, please select another name");
    }

    public void testNameValidationDuplicateInDescendent() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);
        insertProperty(childPath);

        loginAsAdmin();
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(parentPath));
        assertFalse(propertiesPage.isItemPresent("p1"));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = browser.createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null, null);
        assertTrue(form.isFormPresent());
        assertTextPresent("name is already in use in descendent \"" + childName + "\", please select another name");
    }

    public void testNameValidationDuplicateInDescendents() throws Exception
    {
        String parentName = random + "-parent";
        String child1Name = random + "-child1";
        String child2Name = random + "-child2";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String child1Path = xmlRpcHelper.insertTrivialProject(child1Name, parentName, false);
        String child2Path = xmlRpcHelper.insertTrivialProject(child2Name, parentName, false);
        insertProperty(child1Path);
        insertProperty(child2Path);

        loginAsAdmin();
        ListPage propertiesPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(parentPath));
        assertFalse(propertiesPage.isItemPresent("p1"));
        propertiesPage.clickAdd();

        ResourcePropertyForm form = browser.createForm(ResourcePropertyForm.class);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null, null);
        assertTrue(form.isFormPresent());
        assertTextPresent("name is already in use in descendents [" + child1Name + ", " + child2Name + "], please select another name");
    }

    public void testNameValidationDuplicateInAncestor() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        String childPath = xmlRpcHelper.insertTrivialProject(childName, parentName, false);
        insertProperty(parentPath);

        String childPropertiesPath = getPropertiesPath(childPath);
        xmlRpcHelper.deleteConfig(PathUtils.getPath(childPropertiesPath, "p1"));

        loginAsAdmin();
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
        loginAsAdmin();
        CompositePage compositePage = browser.openAndWaitFor(CompositePage.class, PathUtils.getPath(parentPath, "changeViewer"));
        assertTrue(compositePage.isConfigureLinkPresent());

        String childChangeViewerPath = PathUtils.getPath(childPath, "changeViewer");
        Hashtable<String, Object> changeViewer = xmlRpcHelper.createEmptyConfig(CustomChangeViewerConfiguration.class);
        changeViewer.put("changesetURL", "dummy");
        xmlRpcHelper.insertConfig(childChangeViewerPath, changeViewer);

        // Now the child exists we should no longer be able to configure
        // in the parent.
        compositePage.openAndWaitFor();
        assertFalse(compositePage.isConfigureLinkPresent());
        assertTrue(compositePage.isConfiguredDescendentPresent(childName));
    }

    public void testOrderLinks() throws Exception
    {
        String path = orderPrelude();

        loginAsAdmin();
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

        loginAsAdmin();
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

        loginAsAdmin();
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
        String triggersPath = PathUtils.getPath(path, "triggers");
        xmlRpcHelper.insertConfig(triggersPath, trigger);
        trigger.put("name", "t2");
        xmlRpcHelper.insertConfig(triggersPath, trigger);

        loginAsAdmin();
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
        login(random, "");
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
        
        loginAsAdmin();
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
        
        loginAsAdmin();
        ListPage listPage = browser.openAndWaitFor(ListPage.class, getPropertiesPath(childPath));
        assertFalse(listPage.isOrderInheritedPresent());
        assertTrue(listPage.isOrderOverriddenPresent());
    }

    public void testWizardOverridingConfigured() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        xmlRpcHelper.insertSimpleProject(parentName, true);

        loginAsAdmin();
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
        assertFormElements(subversionForm, Constants.TRIVIAL_ANT_REPOSITORY, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private void addInheritingProject(String parentName, String childName)
    {
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, parentName, true);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser.getSelenium());
        projectState.waitFor();
        projectState.nextFormElements(childName, null, null);
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser.getSelenium());
        subversionState.waitFor();
        subversionState.nextFormElements(null, null, null, null, null, null);
        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(browser.getSelenium());
        antState.waitFor();
        antState.finishFormElements(null, null, null, null, null, null);
    }

    public void testWizardMultiRecipeProject() throws Exception
    {
        loginAsAdmin();

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

        String projectTypePath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, Constants.Project.TYPE);
        Hashtable<String, Object> type = xmlRpcHelper.getConfig(projectTypePath);
        assertEquals(SYMBOLIC_NAME_MULTI_RECIPE, type.get(XmlRpcHelper.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingMultiRecipeProject() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";
        
        xmlRpcHelper.insertProject(parent, GLOBAL_PROJECT_NAME, true, xmlRpcHelper.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), xmlRpcHelper.getMultiRecipeTypeConfig());

        loginAsAdmin();

        ProjectHierarchyPage parentHierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, parent, true);
        parentHierarchyPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser.getSelenium());
        projectState.waitFor();
        projectState.nextFormElements(child, null, null);

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser.getSelenium());
        subversionState.waitFor();
        subversionState.finishFormElements(subversionState.getUnchangedValues());

        ProjectHierarchyPage childHierarchyPage = browser.createPage(ProjectHierarchyPage.class, child, false);
        childHierarchyPage.waitFor();

        String childTypePath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, child, Constants.Project.TYPE);
        Hashtable<String, Object> type = xmlRpcHelper.getConfig(childTypePath);
        assertEquals(SYMBOLIC_NAME_MULTI_RECIPE, type.get(XmlRpcHelper.SYMBOLIC_NAME_KEY));
    }

    public void testCustomProject() throws Exception
    {
        loginAsAdmin();

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

        String projectTypePath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, Constants.Project.TYPE);
        Hashtable<String, Object> type = xmlRpcHelper.getConfig(projectTypePath);
        assertEquals(SYMBOLIC_NAME_CUSTOM, type.get(XmlRpcHelper.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingCustomProject() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";

        String pulseFileString = IOUtils.inputStreamToString(getInput("pulseFile", "xml"));
        xmlRpcHelper.insertProject(parent, GLOBAL_PROJECT_NAME, true, xmlRpcHelper.getSubversionConfig(Constants.TEST_ANT_REPOSITORY), xmlRpcHelper.getCustomTypeConfig(pulseFileString));

        loginAsAdmin();

        ProjectHierarchyPage parentHierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, parent, true);
        parentHierarchyPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser.getSelenium());
        projectState.waitFor();
        projectState.nextFormElements(child, null, null);

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser.getSelenium());
        subversionState.waitFor();
        subversionState.nextFormElements(subversionState.getUnchangedValues());

        AddProjectWizard.CustomTypeState customTypeState = new AddProjectWizard.CustomTypeState(browser.getSelenium());
        customTypeState.waitFor();
        customTypeState.finishFormElements(pulseFileString);
        
        ProjectHierarchyPage childHierarchyPage = browser.createPage(ProjectHierarchyPage.class, child, false);
        childHierarchyPage.waitFor();

        String childTypePath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, child, Constants.Project.TYPE);
        Hashtable<String, Object> type = xmlRpcHelper.getConfig(childTypePath);
        assertEquals(SYMBOLIC_NAME_CUSTOM, type.get(XmlRpcHelper.SYMBOLIC_NAME_KEY));
    }

    public void testWizardOverridingScrubRequired() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        xmlRpcHelper.insertSimpleProject(parentName, true);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, parentName, true);
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser.getSelenium());
        projectState.waitFor();
        projectState.nextFormElements(childName, null, null);
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser.getSelenium());
        subversionState.waitFor();
        subversionState.nextFormElements("", null, null, null, null, null);
        assertTrue(subversionState.isFormPresent());
        assertTextPresent("url requires a value");
    }

    public void testDefaultProjectConfigCreated()
    {
        loginAsAdmin();
        addProject(random, false);

        ListPage listPage = browser.openAndWaitFor(ListPage.class, PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, "stages"));
        assertItemPresent(listPage, "default", null, "view", "delete");

        listPage = browser.openAndWaitFor(ListPage.class, PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, "triggers"));
        assertItemPresent(listPage, "scm trigger", null, "view", "delete", "pause");
    }

    public void testDefaultProjectConfigNotCreatedWhenAlreadyInherited()
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        loginAsAdmin();
        addProject(parentName, true, GLOBAL_PROJECT_NAME, false);
        addInheritingProject(parentName, childName);

        ListPage listPage = browser.openAndWaitFor(ListPage.class, PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, childName, "stages"));
        assertItemPresent(listPage, "default", ListPage.ANNOTATION_INHERITED, "view", "delete");

        listPage = browser.openAndWaitFor(ListPage.class, PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, childName, "triggers"));
        assertItemPresent(listPage, "scm trigger", ListPage.ANNOTATION_INHERITED, "view", "delete", "pause");
    }

    public void testValidationInWizard()
    {
        loginAsAdmin();
        ProjectHierarchyPage projectsPage = browser.openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);
        projectsPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser.getSelenium());
        projectState.waitFor();
        assertTrue(projectState.isMarkedRequired("name"));
        projectState.nextFormElements(random, "", "");

        SelectTypeState scmTypeState = new SelectTypeState(browser.getSelenium());
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        // URL is required.
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser.getSelenium());
        subversionState.waitFor();
        assertTrue(subversionState.isMarkedRequired("url"));
        subversionState.nextFormElements("", null, null, null, null, "CLEAN_CHECKOUT");
        assertTrue(subversionState.isFormPresent());
        assertTextPresent("url requires a value");
        subversionState.cancelFormElements(null, null, null, null, null, "CLEAN_CHECKOUT");
    }

    public void testTemplateValidationInWizard()
    {
        loginAsAdmin();
        ProjectHierarchyPage projectsPage = browser.openAndWaitFor(ProjectHierarchyPage.class, GLOBAL_PROJECT_NAME, true);
        projectsPage.clickAddTemplate();

        // Despite the fact we are adding a template, we must specify the
        // name.
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(browser.getSelenium());
        projectState.waitFor();
        assertTrue(projectState.isMarkedRequired("name"));
        projectState.nextFormElements("", "", "");
        assertTrue(projectState.isFormPresent());
        assertTextPresent("name is a required field");
        projectState.nextFormElements(random, "", "");

        SelectTypeState scmTypeState = new SelectTypeState(browser.getSelenium());
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        // Here, we should get away without having a URL.
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(browser.getSelenium());
        subversionState.waitFor();
        assertFalse(subversionState.isMarkedRequired("url"));
        subversionState.nextFormElements("", null, null, null, null, "CLEAN_CHECKOUT");

        ProjectTypeSelectState projectTypeState = new ProjectTypeSelectState(browser.getSelenium());
        projectTypeState.waitFor();
        projectTypeState.nextFormElements(ProjectTypeSelectionConfiguration.TYPE_SINGLE_STEP, "zutubi.antCommandConfig");

        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(browser.getSelenium());
        antState.waitFor();
        antState.finishFormElements("build", null, "build.xml", null, null, null);

        ProjectHierarchyPage hierarchyPage = browser.createPage(ProjectHierarchyPage.class, random, true);
        hierarchyPage.waitFor();
    }

    public void testValidationOnSave() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);

        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        configPage.clickComposite("scm", "subversion configuration");

        SubversionForm subversionForm = browser.createForm(SubversionForm.class);
        subversionForm.waitFor();
        assertTrue(subversionForm.isMarkedRequired("url"));
        subversionForm.applyFormElements("", null, null, null, null, null, null, null, null, null, null, null, null);
        subversionForm.waitFor();
        assertTextPresent("url requires a value");
    }

    public void testTemplateValidationOnSave() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, true);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, true);

        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        String treeLink = configPage.getTreeLinkLocator("subversion configuration");
        browser.waitForLocator(treeLink);
        browser.click(treeLink);

        SubversionForm subversionForm = browser.createForm(SubversionForm.class);
        subversionForm.waitFor();

        subversionForm.applyFormElements("", null, null, null, null, null, null, null, null, null, null, null, null);
        subversionForm.waitFor();
        assertFalse(subversionForm.isMarkedRequired("url"));
        assertTextNotPresent("url requires a value");
    }

    public void testInvalidPathNonExistantScope() throws Exception
    {
        loginAsAdmin();
        browser.goTo(urls.admin() + "scopish/");
        assertGenericError("Invalid path 'scopish': references non-existant root scope 'scopish'");
    }

    public void testInvalidPathNonExistantCollectionItem() throws Exception
    {
        loginAsAdmin();
        browser.goTo(urls.adminUser("nosuchuser"));
        assertGenericError("Invalid path 'users/nosuchuser': references unknown child 'nosuchuser' of collection");
    }

    public void testInvalidPathNonExistantTemplateItem() throws Exception
    {
        loginAsAdmin();
        browser.goTo(urls.adminProject("nosuchproject"));
        assertGenericError("Invalid path 'projects/nosuchproject': references unknown child 'nosuchproject' of collection");
    }

    public void testInvalidPathNonExistantProperty() throws Exception
    {
        loginAsAdmin();
        browser.goTo(urls.adminProject(GLOBAL_PROJECT_NAME) + "nosuchproperty/");
        assertGenericError("Invalid path 'projects/global project template/nosuchproperty': references unknown property 'nosuchproperty' of type 'zutubi.projectConfig'");
    }

    public void testInvalidPathSimpleProperty() throws Exception
    {
        loginAsAdmin();
        browser.goTo(urls.adminProject(GLOBAL_PROJECT_NAME) + "name/");
        assertGenericError("Invalid path 'projects/global project template/name': references non-complex type");
    }

    public void testInstanceErrorsDisplayed() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        loginAsAdmin();
        browser.openAndWaitFor(CompositePage.class, projectPath);
        assertTextPresent("An SCM must be configured to complete this project.");
    }

    public void testSaveNoParentPath() throws Exception
    {
        loginAsAdmin();
        browser.goTo(urls.adminSettings());
        ServerSettingsForm form = browser.createForm(ServerSettingsForm.class);
        form.waitFor();
        String url = "http://somehelpurl.com/" + random;
        form.applyFormElements(null, null, url, null, null, null, null, null, null);

        form.waitFor();
        assertFormElements(form, null, null, url, null, null, null, null, null, null);
    }

    public void testCancelNoParentPath() throws Exception
    {
        loginAsAdmin();
        browser.goTo(urls.adminSettings());
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
        loginAsAdmin();

        String punctuatedName = ".;.,." + random;
        addProject(punctuatedName, false);

        // Check the hierarchy, config and such.
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, punctuatedName, false);
        assertTextNotPresent("invalid");
        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        configPage.clickBuildOptionsAndWait();
    }

    private void checkListedRecipes(String... expectedRecipes)
    {
        loginAsAdmin();
        browser.goTo(urls.adminProject(random) + Constants.Project.STAGES + "/" + ProjectConfigurationWizard.DEFAULT_STAGE + "/");

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
        return PathUtils.getPath(projectPath, "properties");
    }
}