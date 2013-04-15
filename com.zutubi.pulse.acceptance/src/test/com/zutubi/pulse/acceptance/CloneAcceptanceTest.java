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
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.WebUtils;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.Hashtable;

import static com.zutubi.tove.type.record.PathUtils.getParentPath;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;

/**
 * Tests for cloning both of top-level template collection items and of
 * normal map elements.
 */
public class CloneAcceptanceTest extends AcceptanceTestBase
{
    private static final String TEST_PROPERTY_NAME   = "aprop";
    private static final String TEST_PROPERTY_VALUE  = "value";
    private static final String CLONE_PROPERTY_NAME  = "aclone";
    private static final String PARENT_PROPERTY_NAME = "atemplate";

    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.deleteAllConfigs(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testCloneLinkNotPresentForListItems() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertTrivialProject(random, false);
        Hashtable<String, Object> label = rpcClient.RemoteApi.createDefaultConfig(LabelConfiguration.class);
        label.put("label", "foo");
        String labelsPath = PathUtils.getPath(projectPath, "labels");
        String labelPath = rpcClient.RemoteApi.insertConfig(labelsPath, label);

        getBrowser().loginAsAdmin();
        assertCloneAvailability(labelPath, false);
    }

    public void testCloneLinkPresenceDependsOnWritePermission() throws Exception
    {
        String project = random + "-project";
        String user = random + "-user";
        String projectPath = rpcClient.RemoteApi.insertTrivialProject(project, false);
        String propertyPath = rpcClient.RemoteApi.insertProjectProperty(project, TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);
        String userPath = rpcClient.RemoteApi.insertTrivialUser(user);

        getBrowser().loginAndWait(user, "");
        assertCloneAvailability(propertyPath, false);

        String groupPath = rpcClient.RemoteApi.insertGroup(random + "-group", asList(userPath));
        rpcClient.RemoteApi.addProjectPermissions(projectPath, groupPath, AccessManager.ACTION_WRITE);

        getBrowser().logout();
        getBrowser().loginAndWait(user, "");
        assertCloneAvailability(propertyPath, true);
    }

    public void testProjectCloneLinkNotPresentForTemplateRoot() throws Exception
    {
        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, ProjectManager.GLOBAL_PROJECT_NAME, true);
        assertFalse(hierarchyPage.isClonePresent());
    }

    public void testProjectCloneLinkPresenceDependsOnCreatePermission() throws Exception
    {
        String project = random + "-project";
        String user = random + "-user";
        rpcClient.RemoteApi.insertTrivialProject(project, false);
        String userPath = rpcClient.RemoteApi.insertTrivialUser(user);

        getBrowser().loginAndWait(user, "");
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, project, false);
        assertFalse(hierarchyPage.isClonePresent());

        rpcClient.RemoteApi.insertGroup(random, asList(userPath), ServerPermission.CREATE_PROJECT.toString());

        getBrowser().logout();
        getBrowser().loginAndWait(user, "");
        hierarchyPage.openAndWaitFor();
        assertTrue(hierarchyPage.isClonePresent());
    }

    private void assertCloneAvailability(String path, boolean expectedAvailable)
    {
        ListPage labelsPage = getBrowser().openAndWaitFor(ListPage.class, PathUtils.getParentPath(path));
        String baseName = PathUtils.getBaseName(path);
        assertTrue(labelsPage.isItemPresent(baseName));
        assertEquals(expectedAvailable, getBrowser().isElementIdPresent(labelsPage.getActionId(ListPage.ACTION_CLONE, baseName)));
    }

    public void testCloneMapItem() throws Exception
    {
        ListPage labelList = prepareMapItem();
        CloneForm cloneForm = labelList.clickClone(TEST_PROPERTY_NAME);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(CLONE_PROPERTY_NAME);

        ResourcePropertyForm propertyForm = getBrowser().createForm(ResourcePropertyForm.class);
        propertyForm.waitFor();
        assertTrue(propertyForm.checkFormValues(CLONE_PROPERTY_NAME, TEST_PROPERTY_VALUE, "", "false", "false"));

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
        getBrowser().waitForTextPresent("name is required");

        cloneForm.cloneFormElements(TEST_PROPERTY_NAME);
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name \"" + TEST_PROPERTY_NAME + "\" is already in use");
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
        rpcClient.RemoteApi.insertTrivialProject(random, false);
        String propertyPath = rpcClient.RemoteApi.insertProjectProperty(random, TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);

        getBrowser().loginAsAdmin();
        ListPage labelList = getBrowser().openAndWaitFor(ListPage.class, getParentPath(propertyPath));
        assertTrue(labelList.isItemPresent(TEST_PROPERTY_NAME));
        assertTrue(labelList.isActionLinkPresent(TEST_PROPERTY_NAME, ConfigurationRefactoringManager.ACTION_CLONE));
        return labelList;
    }

    public void testCloneProject() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialProject(random, false);

        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, false);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME);
        
        getBrowser().waitFor(ProjectHierarchyPage.class, random + CLONE_PROPERTY_NAME, false);
    }

    public void testCloneProjectValidation() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialProject(random, false);

        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, false);
        cloneForm.waitFor();

        cloneForm.cloneFormElements("");
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name is required");

        cloneForm.cloneFormElements(random);
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name \"" + random + "\" is already in use");
    }

    public void testCloneProjectHierarchyNoChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, false);
        cloneForm.addDescendant(childName);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(parentName + CLONE_PROPERTY_NAME, "false", null);

        ProjectHierarchyPage cloneHierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, parentName + CLONE_PROPERTY_NAME, true);
        cloneHierarchyPage.waitFor();

        assertEquals(0, rpcClient.RemoteApi.getTemplateChildren(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, parentName + CLONE_PROPERTY_NAME)).size());
    }

    public void testCloneProjectHierarchyWithChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, false);
        cloneForm.addDescendant(childName);
        cloneForm.waitFor();
        String parentCloneName = parentName + CLONE_PROPERTY_NAME;
        String childCloneName = childName + CLONE_PROPERTY_NAME;
        cloneForm.cloneFormElements(parentCloneName, "true", childCloneName);

        ProjectHierarchyPage cloneHierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, parentCloneName, true);
        cloneHierarchyPage.waitFor();

        assertEquals(asList(childCloneName), rpcClient.RemoteApi.getTemplateChildren(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, parentCloneName)));

        assertTrue(hierarchyPage.isTreeItemPresent(parentCloneName));
        assertFalse(hierarchyPage.isTreeItemPresent(childCloneName));
        hierarchyPage.expandTreeItem(parentCloneName);
        getBrowser().waitForElement(By.linkText(childCloneName));
    }

    public void testCloneProjectHierarchyValidation() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, false);
        cloneForm.addDescendant(childName);
        cloneForm.waitFor();
        String parentCloneName = parentName + CLONE_PROPERTY_NAME;
        cloneForm.cloneFormElements(parentCloneName, "true", "");
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name is required");

        cloneForm.cloneFormElements(parentCloneName, "true", parentName);
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name \"" + parentName + "\" is already in use");

        cloneForm.cloneFormElements(random, "true", random);
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("duplicate name, all names must be unique");
    }

    public void testSmartCloneProject() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialProject(random, false);

        ProjectHierarchyPage cloneHierarchyPage = doSmartClone();
        assertTrue(cloneHierarchyPage.isTreeItemPresent(random + PARENT_PROPERTY_NAME));
        assertTrue(cloneHierarchyPage.isTreeItemPresent(random + CLONE_PROPERTY_NAME));
    }

    public void testSmartCloneProjectValidation() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialProject(random, false);

        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickSmartClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, true);
        cloneForm.waitFor();

        cloneForm.cloneFormElements("", random + PARENT_PROPERTY_NAME);
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name is required");

        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME, "");
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name is required");

        cloneForm.cloneFormElements(random, random + PARENT_PROPERTY_NAME);
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name \"" + random + "\" is already in use");

        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME, random);
        cloneForm.waitFor();
        getBrowser().waitForTextPresent("name \"" + random + "\" is already in use");
    }

    public void testSmartCloneProjectHierarchyWithChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickSmartClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, true);
        cloneForm.addDescendant(childName);
        cloneForm.waitFor();
        String parentTemplateName = parentName + PARENT_PROPERTY_NAME;
        String parentCloneName = parentName + CLONE_PROPERTY_NAME;
        String childCloneName = childName + CLONE_PROPERTY_NAME;
        cloneForm.cloneFormElements(parentCloneName, parentTemplateName, "true", childCloneName);

        ProjectHierarchyPage cloneHierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, parentCloneName, true);
        cloneHierarchyPage.waitFor();

        assertEquals(asList(childCloneName), rpcClient.RemoteApi.getTemplateChildren(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, parentCloneName)));

        assertTrue(hierarchyPage.isTreeItemPresent(parentTemplateName));
        assertTrue(hierarchyPage.isTreeItemPresent(parentCloneName));
        assertFalse(hierarchyPage.isTreeItemPresent(childCloneName));
        hierarchyPage.expandTreeItem(parentCloneName);
        getBrowser().waitForElement(By.linkText(childCloneName));
    }

    public void testSmartCloneWithInternalReference() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random, false);
        rpcClient.RemoteApi.insertPostStageHook(random, "stagey", "default");

        doSmartClone();

        getBrowser().open(urls.adminProject(WebUtils.uriComponentEncode(random)) + Constants.Project.HOOKS + "/stagey");
        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PostStageHookConfiguration.class);
        hookForm.waitFor();

        assertTrue("Stages field should have been pulled up to extracted template", hookForm.isInherited("stages"));
    }

    public void testSmartCloneWithExternalState() throws Exception
    {
        // CIB-2673
        String childPath = rpcClient.RemoteApi.insertSimpleProject(random, false);
        Hashtable<String, Object> trigger = rpcClient.RemoteApi.createDefaultConfig(ScmBuildTriggerConfiguration.class);
        trigger.put("name", "scm");
        String childTriggersPath = getPath(childPath, "triggers");
        rpcClient.RemoteApi.insertConfig(childTriggersPath, trigger);

        doSmartClone();

        ListPage triggersPage = getBrowser().openAndWaitFor(ListPage.class, childTriggersPath);
        assertEquals("scheduled", triggersPage.getCellContent(0, 2));
        triggersPage = getBrowser().openAndWaitFor(ListPage.class, getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random + CLONE_PROPERTY_NAME, "triggers"));
        assertEquals("scheduled", triggersPage.getCellContent(0, 2));
        triggersPage = getBrowser().openAndWaitFor(ListPage.class, getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random + PARENT_PROPERTY_NAME, "triggers"));
        assertEquals("n/a", triggersPage.getCellContent(0, 2));
    }
    
    private ProjectHierarchyPage doSmartClone()
    {
        getBrowser().loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = getBrowser().openAndWaitFor(ProjectHierarchyPage.class, random, false);
        hierarchyPage.clickSmartClone();

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, true);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME, random + PARENT_PROPERTY_NAME);

        ProjectHierarchyPage cloneHierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, random + CLONE_PROPERTY_NAME, false);
        cloneHierarchyPage.waitFor();
        return cloneHierarchyPage;
    }

    private ProjectHierarchyPage setupHierarchy(String parentName, String childName) throws Exception
    {
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        rpcClient.RemoteApi.insertTrivialProject(childName, parentName, false);
        assertEquals(asList(childName), new ArrayList<String>(rpcClient.RemoteApi.getTemplateChildren(parentPath)));

        getBrowser().loginAsAdmin();
        return getBrowser().openAndWaitFor(ProjectHierarchyPage.class, parentName, true);
    }
}
