package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.forms.admin.*;
import com.zutubi.pulse.acceptance.pages.admin.*;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.prototype.config.LabelConfiguration;
import com.zutubi.pulse.prototype.config.project.changeviewer.CustomChangeViewerConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.ScmBuildTriggerConfiguration;

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
        addProject(random, true, ProjectManager.GLOBAL_PROJECT_NAME);
        goTo(urls.adminProject(random) + "scm/");
        SubversionForm form = new SubversionForm(selenium);
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
        ListPage labelsPage = new ListPage(selenium, urls, labelsPath);
        labelsPage.goTo();
        labelsPage.clickAdd();

        LabelForm labelForm = new LabelForm(selenium);
        labelForm.waitFor();
        labelForm.finishFormElements("my-label");

        labelsPage.waitFor();
        String baseName = getNewestListItem(labelsPath);
        labelsPage.assertItemPresent(baseName, null, "view", "delete");
        DeleteConfirmPage deleteConfirmPage = labelsPage.clickDelete(baseName);
        deleteConfirmPage.waitFor();
        labelsPage = deleteConfirmPage.confirmDeleteListItem();

        labelsPage.assertItemNotPresent(baseName);
    }

    public void testCancelAddListItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ListPage labelsPage = new ListPage(selenium, urls, PathUtils.getPath(projectPath, "labels"));
        labelsPage.goTo();
        labelsPage.clickAdd();

        LabelForm labelForm = new LabelForm(selenium);
        labelForm.waitFor();
        labelForm.cancelFormElements("my-label");

        labelsPage.waitFor();
        labelsPage.assertItemNotPresent("my-label");
    }

    public void testCancelViewListItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String labelBaseName = insertLabel(projectPath);

        loginAsAdmin();
        ListPage labelsPage = new ListPage(selenium, urls, PathUtils.getPath(projectPath, "labels"));
        labelsPage.goTo();
        labelsPage.clickView(labelBaseName);

        LabelForm labelForm = new LabelForm(selenium);
        labelForm.waitFor();
        labelForm.cancelFormElements("");

        labelsPage.waitFor();
        labelsPage.assertItemPresent(labelBaseName, null);
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
        goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
        SubversionForm form = new SubversionForm(selenium);
        form.waitFor();
        form.setFormElement("url", "svn://localhost:3088/");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkAndAssertResult(true, "ok");
    }

    public void testCheckFormFailure() throws Exception
    {
        loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
        SubversionForm form = new SubversionForm(selenium);
        form.waitFor();
        form.setFormElement("url", "svn://localhost:9999/foo");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkAndAssertResult(false, "Connection refused");
    }

    public void testCheckFormValidationFailure() throws Exception
    {
        loginAsAdmin();
        ensureProject(CHECK_PROJECT);
        goTo(urls.adminProject(CHECK_PROJECT) + "scm/");
        SubversionForm form = new SubversionForm(selenium);
        form.waitFor();
        form.setFormElement("url", "");
        CheckForm checkForm = new CheckForm(form);
        checkForm.checkAndAssertResult(false, "unable to check configuration due to validation errors");
        assertTextPresent("url requires a value");
    }

    public void testCheckFormCheckFieldValidationFailure() throws Exception
    {
        loginAsAdmin();
        goTo(urls.admin() + "settings/email/");
        EmailSettingsForm form = new EmailSettingsForm(selenium);
        form.waitFor();
        EmailSettingsCheckForm checkForm = new EmailSettingsCheckForm(form);
        checkForm.checkAndAssertResult(false, "unable to check configuration due to validation errors", "");
        assertTextPresent("recipient address requires a value");
    }

    public void testCheckFormInWizard() throws Exception
    {
        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, false);
        hierarchyPage.goTo();
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(selenium);
        projectState.waitFor();
        projectState.nextFormElements(random, null, null);

        SelectTypeState scmTypeState = new SelectTypeState(selenium);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(selenium);
        subversionState.waitFor();

        subversionState.setFormElement("url", "svn://localhost:3088/");
        CheckForm checkForm = new CheckForm(subversionState);
        checkForm.checkAndAssertResult(true, "ok");

        subversionState.cancelFormElements(null, null, null, null, null, null);
    }


    public void testClearItemPicker() throws Exception
    {
        ensureProject(random);

        loginAsAdmin();
        ProjectConfigPage configPage = new ProjectConfigPage(selenium, urls, random, false);
        configPage.goTo();
        ListPage listPage = configPage.clickCollection("permissions", "permissions");
        listPage.waitFor();
        listPage.assertCellContent(0, 1, "[view]");

        selenium.click("link=view");
        ProjectAclForm form = new ProjectAclForm(selenium);
        form.waitFor();
        form.assertFormElements("all users", "view");
        form.saveFormElements(null, "");
        listPage.waitFor();

        listPage.assertCellContent(0, 1, "[]");
        selenium.click("link=view");
        form = new ProjectAclForm(selenium);
        form.waitFor();
        form.assertFormElements("all users", "");
    }

    public void testClearMultiSelect() throws Exception
    {
        ensureProject(random);

        loginAsAdmin();
        ProjectConfigPage configPage = new ProjectConfigPage(selenium, urls, random, false);
        configPage.goTo();
        configPage.clickComposite("type", "ant command and artifacts");
        AntTypeForm form = new AntTypeForm(selenium);
        form.waitFor();
        form.assertFormElements("", "build.xml", "", "", "");
        form.applyFormElements(null, null, null, null, "ant");
        form.waitFor();
        form.assertFormElements("", "build.xml", "", "", "ant");

        form.applyFormElements(null, null, null, null, "");
        form.waitFor();
        form.assertFormElements("", "build.xml", "", "", "");
    }

    public void testNameValidationDuplicate() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        String propertiesPath = getPropertiesPath(projectPath);
        insertProperty(projectPath);

        loginAsAdmin();
        ListPage propertiesPage = new ListPage(selenium, urls, propertiesPath);
        propertiesPage.goTo();
        propertiesPage.clickAdd();

        ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.assertFormPresent();
        assertTextPresent("name is already in use, please select another name");
    }

    public void testNameValidationDuplicateInherited() throws Exception
    {
        String parentName = random + "-parent";
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        insertProperty(parentPath);
        String childPath = xmlRpcHelper.insertTrivialProject(random + "-child", parentName, false);

        loginAsAdmin();
        ListPage propertiesPage = new ListPage(selenium, urls, getPropertiesPath(childPath));
        propertiesPage.goTo();
        propertiesPage.clickAdd();

        ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.assertFormPresent();
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
        ListPage propertiesPage = new ListPage(selenium, urls, getPropertiesPath(parentPath));
        propertiesPage.goTo();
        propertiesPage.assertItemNotPresent("p1");
        propertiesPage.clickAdd();

        ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.assertFormPresent();
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
        ListPage propertiesPage = new ListPage(selenium, urls, getPropertiesPath(parentPath));
        propertiesPage.goTo();
        propertiesPage.assertItemNotPresent("p1");
        propertiesPage.clickAdd();

        ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.assertFormPresent();
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
        ListPage propertiesPage = new ListPage(selenium, urls, childPropertiesPath);
        propertiesPage.goTo();
        propertiesPage.assertItemPresent("p1", ListPage.ANNOTATION_HIDDEN);
        propertiesPage.clickAdd();

        ResourcePropertyForm form = new ResourcePropertyForm(selenium, false);
        form.waitFor();
        form.finishFormElements("p1", "value", null, null, null);
        form.assertFormPresent();
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
        CompositePage compositePage = new CompositePage(selenium, urls, PathUtils.getPath(parentPath, "changeViewer"));
        compositePage.goTo();
        assertTrue(compositePage.isConfigureLinkPresent());

        String childChangeViewerPath = PathUtils.getPath(childPath, "changeViewer");
        Hashtable<String, Object> changeViewer = xmlRpcHelper.createEmptyConfig(CustomChangeViewerConfiguration.class);
        changeViewer.put("changesetURL", "dummy");
        xmlRpcHelper.insertConfig(childChangeViewerPath, changeViewer);

        // Now the child exists we should no longer be able to configure
        // in the parent.
        compositePage.goTo();
        assertFalse(compositePage.isConfigureLinkPresent());
        compositePage.assertConfiguredDescendents(childName);
    }

    public void testOrderLinks() throws Exception
    {
        String path = orderPrelude();

        loginAsAdmin();
        ListPage listPage = new ListPage(selenium, urls, getPropertiesPath(path));
        listPage.goTo();
        listPage.assertItemPresent("p1", null, ACTION_DOWN);
        listPage.assertActionsNotPresent("p1", ACTION_UP);
        listPage.assertItemPresent("p2", null, ACTION_DOWN, ACTION_UP);
        listPage.assertItemPresent("p3", null, ACTION_UP);
        listPage.assertActionsNotPresent("p3", ACTION_DOWN);
    }

    public void testMoveUp() throws Exception
    {
        String path = orderPrelude();

        loginAsAdmin();
        ListPage listPage = new ListPage(selenium, urls, getPropertiesPath(path));
        listPage.goTo();
        listPage.assertCellContent(1, 0, "p2");
        listPage.assertCellContent(2, 0, "p3");
        listPage.clickUp("p3");
        listPage.waitFor();
        listPage.assertCellContent(1, 0, "p3");
        listPage.assertCellContent(2, 0, "p2");
    }

    public void testMoveDown() throws Exception
    {
        String path = orderPrelude();

        loginAsAdmin();
        ListPage listPage = new ListPage(selenium, urls, getPropertiesPath(path));
        listPage.goTo();
        listPage.assertCellContent(1, 0, "p2");
        listPage.assertCellContent(2, 0, "p3");
        listPage.clickDown("p2");
        listPage.waitFor();
        listPage.assertCellContent(2, 0, "p2");
        listPage.assertCellContent(1, 0, "p3");
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
        ListPage listPage = new ListPage(selenium, urls, triggersPath);
        listPage.goTo();
        assertTextNotPresent("order");
        listPage.assertItemPresent("t1", null);
        listPage.assertItemPresent("t2", null);
        listPage.assertActionsNotPresent("t1", ACTION_UP, ACTION_DOWN);
        listPage.assertActionsNotPresent("t2", ACTION_UP, ACTION_DOWN);
    }

    public void testOrderLinksNotPresentWithNoWritePermission() throws Exception
    {
        String path = orderPrelude();

        xmlRpcHelper.insertTrivialUser(random);
        login(random, "");
        ListPage listPage = new ListPage(selenium, urls, getPropertiesPath(path));
        listPage.goTo();
        assertTextNotPresent("order");
        listPage.assertItemPresent("p1", null);
        listPage.assertItemPresent("p2", null);
        listPage.assertItemPresent("p3", null);
        listPage.assertActionsNotPresent("p1", ACTION_UP, ACTION_DOWN);
        listPage.assertActionsNotPresent("p2", ACTION_UP, ACTION_DOWN);
        listPage.assertActionsNotPresent("p3", ACTION_UP, ACTION_DOWN);
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
        ListPage listPage = new ListPage(selenium, urls, getPropertiesPath(childPath));
        listPage.goTo();
        assertFalse(listPage.isOrderInheritedPresent());
        assertFalse(listPage.isOrderOverriddenPresent());

        xmlRpcHelper.setConfigOrder(getPropertiesPath(parentPath), "p2", "p1");
        listPage.goTo();
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
        ListPage listPage = new ListPage(selenium, urls, getPropertiesPath(childPath));
        listPage.goTo();
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

        ProjectHierarchyPage childHierarchyPage = new ProjectHierarchyPage(selenium, urls, childName, false);
        childHierarchyPage.waitFor();
        ProjectConfigPage configPage = childHierarchyPage.clickConfigure();
        configPage.waitFor();
        SeleniumUtils.waitForLocator(selenium, configPage.getTreeLinkLocator("subversion configuration"));
        CompositePage scmPage = configPage.clickComposite("scm", "subversion configuration");
        scmPage.waitFor();
        SubversionForm subversionForm = new SubversionForm(selenium);
        subversionForm.waitFor();
        subversionForm.assertFormElements("svn://localhost:3088/accept/trunk/triviant", null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private void addInheritingProject(String parentName, String childName)
    {
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, parentName, true);
        hierarchyPage.goTo();
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(selenium);
        projectState.waitFor();
        projectState.nextFormElements(childName, null, null);
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(selenium);
        subversionState.waitFor();
        subversionState.nextFormElements(null, null, null, null, null, null);
        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(selenium);
        antState.waitFor();
        antState.finishFormElements(null, null, null, null);
    }

    public void testWizardOverridingScrubRequired() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        xmlRpcHelper.insertSimpleProject(parentName, true);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, parentName, true);
        hierarchyPage.goTo();
        hierarchyPage.clickAdd();
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(selenium);
        projectState.waitFor();
        projectState.nextFormElements(childName, null, null);
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(selenium);
        subversionState.waitFor();
        subversionState.nextFormElements("", null, null, null, null, null);
        subversionState.assertFormPresent();
        assertTextPresent("url requires a value");
    }

    public void testDefaultProjectConfigCreated()
    {
        loginAsAdmin();
        goTo(urls.adminProjects());
        addProject(random);

        ListPage listPage = new ListPage(selenium, urls, PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, random, "stages"));
        listPage.goTo();
        listPage.assertItemPresent("default", null, "view", "delete");

        listPage = new ListPage(selenium, urls, PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, random, "triggers"));
        listPage.goTo();
        listPage.assertItemPresent("scm trigger", null, "view", "delete", "pause");
    }

    public void testDefaultProjectConfigNotCreatedWhenAlreadyInherited()
    {
        String parentName = random + "-parent";
        String childName = random + "-child";

        loginAsAdmin();
        addProject(parentName, true, ProjectManager.GLOBAL_PROJECT_NAME);
        addInheritingProject(parentName, childName);

        ListPage listPage = new ListPage(selenium, urls, PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, childName, "stages"));
        listPage.goTo();
        listPage.assertItemPresent("default", ListPage.ANNOTATION_INHERITED, "view", "delete");

        listPage = new ListPage(selenium, urls, PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, childName, "triggers"));
        listPage.goTo();
        listPage.assertItemPresent("scm trigger", ListPage.ANNOTATION_INHERITED, "view", "delete", "pause");
    }

    public void testValidationInWizard()
    {
        loginAsAdmin();
        ProjectHierarchyPage projectsPage = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, true);
        projectsPage.goTo();
        projectsPage.clickAdd();

        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(selenium);
        projectState.waitFor();
        assertTrue(projectState.isMarkedRequired("name"));
        projectState.nextFormElements(random, "", "");

        SelectTypeState scmTypeState = new SelectTypeState(selenium);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        // URL is required.
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(selenium);
        subversionState.waitFor();
        assertTrue(subversionState.isMarkedRequired("url"));
        subversionState.nextFormElements("", null, null, null, null, "CLEAN_CHECKOUT");
        subversionState.assertFormPresent();
        assertTextPresent("url requires a value");
        subversionState.cancelFormElements(null, null, null, null, null, "CLEAN_CHECKOUT");
    }

    public void testTemplateValidationInWizard()
    {
        loginAsAdmin();
        ProjectHierarchyPage projectsPage = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, true);
        projectsPage.goTo();
        projectsPage.clickAddTemplate();

        // Despite the fact we are adding a template, we must specify the
        // name.
        AddProjectWizard.ProjectState projectState = new AddProjectWizard.ProjectState(selenium);
        projectState.waitFor();
        assertTrue(projectState.isMarkedRequired("name"));
        projectState.nextFormElements("", "", "");
        projectState.assertFormPresent();
        assertTextPresent("the projects name is a required field");
        projectState.nextFormElements(random, "", "");

        SelectTypeState scmTypeState = new SelectTypeState(selenium);
        scmTypeState.waitFor();
        scmTypeState.nextFormElements("zutubi.subversionConfig");

        // Here, we should get away without having a URL.
        AddProjectWizard.SubversionState subversionState = new AddProjectWizard.SubversionState(selenium);
        subversionState.waitFor();
        assertFalse(subversionState.isMarkedRequired("url"));
        subversionState.nextFormElements("", null, null, null, null, "CLEAN_CHECKOUT");

        SelectTypeState projectTypeState = new SelectTypeState(selenium);
        projectTypeState.waitFor();
        projectTypeState.nextFormElements("zutubi.antTypeConfig");

        AddProjectWizard.AntState antState = new AddProjectWizard.AntState(selenium);
        antState.waitFor();
        antState.finishFormElements(null, "build.xml", null, null);

        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, true);
        hierarchyPage.waitFor();
    }

    public void testValidationOnSave() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();

        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        configPage.waitFor();
        configPage.clickComposite("scm", "subversion configuration");

        SubversionForm subversionForm = new SubversionForm(selenium);
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
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, true);
        hierarchyPage.goTo();

        ProjectConfigPage configPage = hierarchyPage.clickConfigure();
        String treeLink = configPage.getTreeLinkLocator("subversion configuration");
        SeleniumUtils.waitForLocator(selenium, treeLink);
        selenium.click(treeLink);

        SubversionForm subversionForm = new SubversionForm(selenium);
        subversionForm.waitFor();

        subversionForm.applyFormElements("", null, null, null, null, null, null, null, null, null, null, null, null);
        subversionForm.waitFor();
        assertFalse(subversionForm.isMarkedRequired("url"));
        assertTextNotPresent("url requires a value");
    }

    public void testInvalidPathNonExistantScope() throws Exception
    {
        loginAsAdmin();
        goTo(urls.admin() + "scopish/");
        assertGenericError("Invalid path 'scopish': references non-existant root scope 'scopish'");
    }

    public void testInvalidPathNonExistantCollectionItem() throws Exception
    {
        loginAsAdmin();
        goTo(urls.adminUser("nosuchuser"));
        assertGenericError("Invalid path 'users/nosuchuser': references unknown child 'nosuchuser' of collection");
    }

    public void testInvalidPathNonExistantTemplateItem() throws Exception
    {
        loginAsAdmin();
        goTo(urls.adminProject("nosuchproject"));
        assertGenericError("Invalid path 'projects/nosuchproject': references unknown child 'nosuchproject' of collection");
    }

    public void testInvalidPathNonExistantProperty() throws Exception
    {
        loginAsAdmin();
        goTo(urls.adminProject(ProjectManager.GLOBAL_PROJECT_NAME) + "nosuchproperty/");
        assertGenericError("Invalid path 'projects/global project template/nosuchproperty': references unknown property 'nosuchproperty' of type 'zutubi.projectConfig'");
    }

    public void testInvalidPathSimpleProperty() throws Exception
    {
        loginAsAdmin();
        goTo(urls.adminProject(ProjectManager.GLOBAL_PROJECT_NAME) + "name/");
        assertGenericError("Invalid path 'projects/global project template/name': references non-complex type");
    }

    public void testInstanceErrorsDisplayed() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        loginAsAdmin();
        CompositePage compositePage = new CompositePage(selenium, urls, projectPath);
        compositePage.goTo();
        assertTrue(compositePage.areErrorsPresent());
        assertTextPresent("An SCM must be configured to complete this project.");
    }

    private void insertProperty(String projectPath) throws Exception
    {
        insertProperty(projectPath, "p1");
    }

    private void insertProperty(String projectPath, String name) throws Exception
    {
        Hashtable<String, Object> property = xmlRpcHelper.createEmptyConfig(ResourceProperty.class);
        property.put("name", name);
        xmlRpcHelper.insertConfig(getPropertiesPath(projectPath), property);
    }

    private String getPropertiesPath(String projectPath)
    {
        return PathUtils.getPath(projectPath, "properties");
    }
}