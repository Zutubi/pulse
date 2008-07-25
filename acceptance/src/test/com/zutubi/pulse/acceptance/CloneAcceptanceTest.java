package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.CloneForm;
import com.zutubi.pulse.acceptance.forms.admin.ResourcePropertyForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.tove.config.LabelConfiguration;
import com.zutubi.pulse.tove.config.group.ServerPermission;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getParentPath;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Hashtable;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

/**
 * Tests for cloning both of top-level template collection items and of
 * normal map elements.
 */
@Test(dependsOnGroups = {"init.*"})
public class CloneAcceptanceTest extends SeleniumTestBase
{
    private static final String TEST_PROPERTY_NAME   = "aprop";
    private static final String TEST_PROPERTY_VALUE  = "value";
    private static final String CLONE_PROPERTY_NAME  = "aclone";
    private static final String PARENT_PROPERTY_NAME = "atemplate";

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
    }

    @AfterMethod
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
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, ProjectManager.GLOBAL_PROJECT_NAME, true);
        hierarchyPage.goTo();
        assertFalse(hierarchyPage.isClonePresent());
    }

    public void testProjectCloneLinkPresenceDependsOnCreatePermission() throws Exception
    {
        String project = random + "-project";
        String user = random + "-user";
        xmlRpcHelper.insertTrivialProject(project, false);
        String userPath = xmlRpcHelper.insertTrivialUser(user);

        login(user, "");
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, project, false);
        hierarchyPage.goTo();
        assertFalse(hierarchyPage.isClonePresent());

        xmlRpcHelper.insertGroup(random, asList(userPath), ServerPermission.CREATE_PROJECT.toString());

        logout();
        login(user, "");
        hierarchyPage.goTo();
        assertTrue(hierarchyPage.isClonePresent());
    }

    private void assertCloneAvailability(String path, boolean expectedAvailable)
    {
        ListPage labelsPage = new ListPage(selenium, urls, PathUtils.getParentPath(path));
        labelsPage.goTo();
        String baseName = PathUtils.getBaseName(path);
        labelsPage.assertItemPresent(baseName, null);
        assertEquals(expectedAvailable, selenium.isElementPresent(labelsPage.getActionId(ListPage.ACTION_CLONE, baseName)));
    }

    public void testCloneMapItem() throws Exception
    {
        ListPage labelList = prepareMapItem();
        CloneForm cloneForm = labelList.clickClone(TEST_PROPERTY_NAME);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(CLONE_PROPERTY_NAME);

        ResourcePropertyForm propertyForm = new ResourcePropertyForm(selenium, false);
        propertyForm.waitFor();
        propertyForm.assertFormElements(CLONE_PROPERTY_NAME, TEST_PROPERTY_VALUE, "false", "false", "false");

        labelList.goTo();
        labelList.assertItemPresent(CLONE_PROPERTY_NAME, null);
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
        labelList.assertItemPresent(TEST_PROPERTY_NAME, null);
        labelList.assertItemNotPresent(CLONE_PROPERTY_NAME);
    }

    private ListPage prepareMapItem() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);
        String propertyPath = xmlRpcHelper.insertProjectProperty(random, TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);

        loginAsAdmin();
        ListPage labelList = new ListPage(selenium, urls, getParentPath(propertyPath));
        labelList.goTo();
        labelList.assertItemPresent(TEST_PROPERTY_NAME, null, AccessManager.ACTION_CLONE);
        return labelList;
    }

    public void testCloneProject() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        hierarchyPage.clickClone();

        CloneForm cloneForm = new CloneForm(selenium, false);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME);
        
        ProjectHierarchyPage cloneHierarchyPage = new ProjectHierarchyPage(selenium, urls, random + CLONE_PROPERTY_NAME, false);
        cloneHierarchyPage.waitFor();
    }

    public void testCloneProjectValidation() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        hierarchyPage.clickClone();

        CloneForm cloneForm = new CloneForm(selenium, false);
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

        CloneForm cloneForm = new CloneForm(selenium, false, childName);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(parentName + CLONE_PROPERTY_NAME, "false", null);

        ProjectHierarchyPage cloneHierarchyPage = new ProjectHierarchyPage(selenium, urls, parentName + CLONE_PROPERTY_NAME, true);
        cloneHierarchyPage.waitFor();

        assertEquals(0, xmlRpcHelper.getTemplateChildren(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, parentName + CLONE_PROPERTY_NAME)).size());
    }

    public void testCloneProjectHierarchyWithChild() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickClone();

        CloneForm cloneForm = new CloneForm(selenium, false, childName);
        cloneForm.waitFor();
        String parentCloneName = parentName + CLONE_PROPERTY_NAME;
        String childCloneName = childName + CLONE_PROPERTY_NAME;
        cloneForm.cloneFormElements(parentCloneName, "true", childCloneName);

        ProjectHierarchyPage cloneHierarchyPage = new ProjectHierarchyPage(selenium, urls, parentCloneName, true);
        cloneHierarchyPage.waitFor();

        assertEquals(asList(childCloneName), xmlRpcHelper.getTemplateChildren(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, parentCloneName)));

        assertTrue(hierarchyPage.isTreeItemPresent(parentCloneName));
        assertFalse(hierarchyPage.isTreeItemPresent(childCloneName));
        hierarchyPage.expandTreeItem(parentCloneName);
        SeleniumUtils.waitForLocator(selenium, hierarchyPage.getTreeItemLocator(childCloneName));
    }

    public void testCloneProjectHierarchyValidation() throws Exception
    {
        String parentName = random + "-parent";
        String childName = random + "-child";
        ProjectHierarchyPage hierarchyPage = setupHierarchy(parentName, childName);
        hierarchyPage.clickClone();

        CloneForm cloneForm = new CloneForm(selenium, false, childName);
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

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        hierarchyPage.clickSmartClone();

        CloneForm cloneForm = new CloneForm(selenium, true);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(random + CLONE_PROPERTY_NAME, random + PARENT_PROPERTY_NAME);

        ProjectHierarchyPage cloneHierarchyPage = new ProjectHierarchyPage(selenium, urls, random + CLONE_PROPERTY_NAME, false);
        cloneHierarchyPage.waitFor();

        assertTrue(cloneHierarchyPage.isTreeItemPresent(random + PARENT_PROPERTY_NAME));
        assertTrue(cloneHierarchyPage.isTreeItemPresent(random + CLONE_PROPERTY_NAME));
    }

    public void testSmartCloneProjectValidation() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, random, false);
        hierarchyPage.goTo();
        hierarchyPage.clickSmartClone();

        CloneForm cloneForm = new CloneForm(selenium, true);
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

        CloneForm cloneForm = new CloneForm(selenium, true, childName);
        cloneForm.waitFor();
        String parentTemplateName = parentName + PARENT_PROPERTY_NAME;
        String parentCloneName = parentName + CLONE_PROPERTY_NAME;
        String childCloneName = childName + CLONE_PROPERTY_NAME;
        cloneForm.cloneFormElements(parentCloneName, parentTemplateName, "true", childCloneName);

        ProjectHierarchyPage cloneHierarchyPage = new ProjectHierarchyPage(selenium, urls, parentCloneName, true);
        cloneHierarchyPage.waitFor();

        assertEquals(asList(childCloneName), xmlRpcHelper.getTemplateChildren(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, parentCloneName)));

        assertTrue(hierarchyPage.isTreeItemPresent(parentTemplateName));
        assertTrue(hierarchyPage.isTreeItemPresent(parentCloneName));
        assertFalse(hierarchyPage.isTreeItemPresent(childCloneName));
        hierarchyPage.expandTreeItem(parentCloneName);
        SeleniumUtils.waitForLocator(selenium, hierarchyPage.getTreeItemLocator(childCloneName));
    }

    private ProjectHierarchyPage setupHierarchy(String parentName, String childName) throws Exception
    {
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        xmlRpcHelper.insertTrivialProject(childName, parentName, false);
        assertEquals(asList(childName), new ArrayList<String>(xmlRpcHelper.getTemplateChildren(parentPath)));

        loginAsAdmin();
        ProjectHierarchyPage hierarchyPage = new ProjectHierarchyPage(selenium, urls, parentName, true);
        hierarchyPage.goTo();
        return hierarchyPage;
    }
}
