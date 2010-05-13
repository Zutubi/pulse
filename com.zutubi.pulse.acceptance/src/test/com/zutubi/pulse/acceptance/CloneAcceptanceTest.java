package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.forms.admin.CloneForm;
import com.zutubi.pulse.acceptance.forms.admin.ResourcePropertyForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.hooks.PostStageHookConfiguration;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getParentPath;
import com.zutubi.util.WebUtils;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Hashtable;

/**
 * Tests for cloning both of top-level template collection items and of
 * normal map elements.
 */
public class CloneAcceptanceTest extends SeleniumTestBase
{
    private static final String TEST_PROPERTY_NAME   = "aprop";
    private static final String TEST_PROPERTY_VALUE  = "value";
    private static final String CLONE_PROPERTY_NAME  = "aclone";
    private static final String PARENT_PROPERTY_NAME = "atemplate";

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.deleteAllConfigs(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testCloneLinkNotPresentForListItems() throws Exception
    {
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);
        Hashtable<String, Object> label = xmlRpcHelper.createDefaultConfig(LabelConfiguration.class);
        label.put("label", "foo");
        String labelsPath = PathUtils.getPath(projectPath, "labels");
        String labelPath = xmlRpcHelper.insertConfig(labelsPath, label);
        
        loginAsAdmin();
        assertCloneAvailability(labelPath, false);
    }

    public void testCloneLinkPresenceDependsOnWritePermission() throws Exception
    {
        String project = random + "-project";
        String user = random + "-user";
        String projectPath = xmlRpcHelper.insertTrivialProject(project, false);
        String propertyPath = xmlRpcHelper.insertProjectProperty(project, TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);
        String userPath = xmlRpcHelper.insertTrivialUser(user);

        login(user, "");
        assertCloneAvailability(propertyPath, false);

        String groupPath = xmlRpcHelper.insertGroup(random + "-group", asList(userPath));
        xmlRpcHelper.addProjectPermissions(projectPath, groupPath, AccessManager.ACTION_WRITE);

        logout();
        login(user, "");
        assertCloneAvailability(propertyPath, true);
    }

    public void testProjectCloneLinkNotPresentForTemplateRoot() throws Exception
    {
        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        assertFalse(hierarchyPage.isClonePresent());
    }

    public void testProjectCloneLinkPresenceDependsOnCreatePermission() throws Exception
    {
        String project = random + "-project";
        String user = random + "-user";
        xmlRpcHelper.insertTrivialProject(project, false);
        String userPath = xmlRpcHelper.insertTrivialUser(user);

        login(user, "");
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, project, false);
        assertFalse(hierarchyPage.isClonePresent());

        xmlRpcHelper.insertGroup(random, asList(userPath), ServerPermission.CREATE_PROJECT.toString());

        logout();
        login(user, "");
        hierarchyPage.openAndWaitFor();
        assertTrue(hierarchyPage.isClonePresent());
    }

    private void assertCloneAvailability(String path, boolean expectedAvailable)
    {
        ListPage labelsPage = browser.openAndWaitFor(ListPage.class, PathUtils.getParentPath(path));
        String baseName = PathUtils.getBaseName(path);
        assertTrue(labelsPage.isItemPresent(baseName));
        assertEquals(expectedAvailable, browser.isElementIdPresent(labelsPage.getActionId(ListPage.ACTION_CLONE, baseName)));
    }

    public void testCloneMapItem() throws Exception
    {
        ListPage labelList = prepareMapItem();
        CloneForm cloneForm = labelList.clickClone(TEST_PROPERTY_NAME);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(CLONE_PROPERTY_NAME);

        ResourcePropertyForm propertyForm = browser.createForm(ResourcePropertyForm.class);
        propertyForm.waitFor();
        assertFormElements(propertyForm, CLONE_PROPERTY_NAME, TEST_PROPERTY_VALUE, "", "false", "false", "false");

        labelList.openAndWaitFor();
        assertTrue(labelList.isItemPresent(CLONE_PROPERTY_NAME));
    }

    public void testCloneMapItemValidation() throws Exception
    {
        ListPage labelList = prepareMapItem();
        CloneForm cloneForm = labelList.clickClone(TEST_PROPERTY_NAME);
        cloneForm.waitFor();

        cloneForm.cloneFormElements("");
        cloneForm.waitFor();
        assertTextPresent("name is required");

        cloneForm.cloneFormElements(TEST_PROPERTY_NAME);
        cloneForm.waitFor();
        assertTextPresent("name is already in use");
    }

    public void testCloneMapItemCancel() throws Exception
    {
        ListPage labelList = prepareMapItem();
        CloneForm cloneForm = labelList.clickClone(TEST_PROPERTY_NAME);
        cloneForm.waitFor();

        cloneForm.cancelFormElements(CLONE_PROPERTY_NAME);
        labelList.waitFor();
        assertTrue(labelList.isItemPresent(TEST_PROPERTY_NAME));
        assertFalse(labelList.isItemPresent(CLONE_PROPERTY_NAME));
    }

    private ListPage prepareMapItem() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);
        String propertyPath = xmlRpcHelper.insertProjectProperty(random, TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);

        loginAsAdmin();
        ListPage labelList = browser.openAndWaitFor(ListPage.class, getParentPath(propertyPath));
        assertTrue(labelList.isItemPresent(TEST_PROPERTY_NAME));
        assertTrue(labelList.isActionLinkPresent(TEST_PROPERTY_NAME, ConfigurationRefactoringManager.ACTION_CLONE));
        return labelList;
    }

    public void testCloneProject() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, false);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME);
        
        browser.waitFor(ProjectHierarchyPage.class, random + CLONE_PROPERTY_NAME, false);
    }

    public void testCloneProjectValidation() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, false);
        cloneForm.waitFor();

        cloneForm.cloneFormElements("");
        cloneForm.waitFor();
        assertTextPresent("name is required");

        cloneForm.cloneFormElements(random);
        cloneForm.waitFor();
        assertTextPresent("name is already in use");
    }

    public void testCloneProjectHierarchyNoChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, false);
        cloneForm.addDescendant(childName);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(parentName + CLONE_PROPERTY_NAME, "false", null);

        ProjectHierarchyPage cloneHierarchyPage = browser.createPage(ProjectHierarchyPage.class, parentName + CLONE_PROPERTY_NAME, true);
        cloneHierarchyPage.waitFor();

        assertEquals(0, xmlRpcHelper.getTemplateChildren(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, parentName + CLONE_PROPERTY_NAME)).size());
    }

    public void testCloneProjectHierarchyWithChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, false);
        cloneForm.addDescendant(childName);
        cloneForm.waitFor();
        String parentCloneName = parentName + CLONE_PROPERTY_NAME;
        String childCloneName = childName + CLONE_PROPERTY_NAME;
        cloneForm.cloneFormElements(parentCloneName, "true", childCloneName);

        ProjectHierarchyPage cloneHierarchyPage = browser.createPage(ProjectHierarchyPage.class, parentCloneName, true);
        cloneHierarchyPage.waitFor();

        assertEquals(asList(childCloneName), xmlRpcHelper.getTemplateChildren(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, parentCloneName)));

        assertTrue(hierarchyPage.isTreeItemPresent(parentCloneName));
        assertFalse(hierarchyPage.isTreeItemPresent(childCloneName));
        hierarchyPage.expandTreeItem(parentCloneName);
        browser.waitForLocator(hierarchyPage.getTreeItemLocator(childCloneName));
    }

    public void testCloneProjectHierarchyValidation() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, false);
        cloneForm.addDescendant(childName);
        cloneForm.waitFor();
        String parentCloneName = parentName + CLONE_PROPERTY_NAME;
        cloneForm.cloneFormElements(parentCloneName, "true", "");
        cloneForm.waitFor();
        assertTextPresent("name is required");

        cloneForm.cloneFormElements(parentCloneName, "true", parentName);
        cloneForm.waitFor();
        assertTextPresent("name is already in use");

        cloneForm.cloneFormElements(random, "true", random);
        cloneForm.waitFor();
        assertTextPresent("duplicate name, all names must be unique");
    }

    public void testSmartCloneProject() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);

        ProjectHierarchyPage cloneHierarchyPage = doSmartClone();
        assertTrue(cloneHierarchyPage.isTreeItemPresent(random + PARENT_PROPERTY_NAME));
        assertTrue(cloneHierarchyPage.isTreeItemPresent(random + CLONE_PROPERTY_NAME));
    }

    public void testSmartCloneProjectValidation() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickSmartClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, true);
        cloneForm.waitFor();

        cloneForm.cloneFormElements("", random + PARENT_PROPERTY_NAME);
        cloneForm.waitFor();
        assertTextPresent("name is required");

        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME, "");
        cloneForm.waitFor();
        assertTextPresent("name is required");

        cloneForm.cloneFormElements(random, random + PARENT_PROPERTY_NAME);
        cloneForm.waitFor();
        assertTextPresent("name is already in use");

        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME, random);
        cloneForm.waitFor();
        assertTextPresent("name is already in use");
    }

    public void testSmartCloneProjectHierarchyWithChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickSmartClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, true);
        cloneForm.addDescendant(childName);
        cloneForm.waitFor();
        String parentTemplateName = parentName + PARENT_PROPERTY_NAME;
        String parentCloneName = parentName + CLONE_PROPERTY_NAME;
        String childCloneName = childName + CLONE_PROPERTY_NAME;
        cloneForm.cloneFormElements(parentCloneName, parentTemplateName, "true", childCloneName);

        ProjectHierarchyPage cloneHierarchyPage = browser.createPage(ProjectHierarchyPage.class, parentCloneName, true);
        cloneHierarchyPage.waitFor();

        assertEquals(asList(childCloneName), xmlRpcHelper.getTemplateChildren(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, parentCloneName)));

        assertTrue(hierarchyPage.isTreeItemPresent(parentTemplateName));
        assertTrue(hierarchyPage.isTreeItemPresent(parentCloneName));
        assertFalse(hierarchyPage.isTreeItemPresent(childCloneName));
        hierarchyPage.expandTreeItem(parentCloneName);
        browser.waitForLocator(hierarchyPage.getTreeItemLocator(childCloneName));
    }

    public void testSmartCloneWithInternalReference() throws Exception
    {
        xmlRpcHelper.insertSimpleProject(random, false);
        xmlRpcHelper.insertPostStageHook(random, "stagey", "default");

        doSmartClone();

        browser.open(urls.adminProject(WebUtils.uriComponentEncode(random)) + Constants.Project.HOOKS + "/stagey");
        ConfigurationForm hookForm = browser.createForm(ConfigurationForm.class, PostStageHookConfiguration.class);
        hookForm.waitFor();

        assertTrue("Stages field should have been pulled up to extracted template", hookForm.isInherited("stages"));
    }

    private ProjectHierarchyPage doSmartClone()
    {
        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = browser.openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickSmartClone();

        CloneForm cloneForm = browser.createForm(CloneForm.class, true);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME, random + PARENT_PROPERTY_NAME);

        ProjectHierarchyPage cloneHierarchyPage = browser.createPage(ProjectHierarchyPage.class, random + CLONE_PROPERTY_NAME, false);
        cloneHierarchyPage.waitFor();
        return cloneHierarchyPage;
    }

    private ProjectHierarchyPage setupHierarchy(String parentName, String childName) throws Exception
    {
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        xmlRpcHelper.insertTrivialProject(childName, parentName, false);
        assertEquals(asList(childName), new ArrayList<String>(xmlRpcHelper.getTemplateChildren(parentPath)));

        loginAsAdmin();
        return browser.openAndWaitFor(ProjectHierarchyPage.class, parentName, true);
    }
}
