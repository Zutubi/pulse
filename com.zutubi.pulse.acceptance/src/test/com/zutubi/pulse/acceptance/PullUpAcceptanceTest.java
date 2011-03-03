package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.PullUpForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getParentPath;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Hashtable;

public class PullUpAcceptanceTest extends SeleniumTestBase
{
    private static final String TEST_PROPERTY_NAME   = "aprop";
    private static final String TEST_PROPERTY_VALUE  = "value";

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.deleteAllConfigs(getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testPullUpLinkNotPresentForInheritedItem() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";
        setupHierarchy(parent, child);

        String labelPath = insertLabel(parent, createLabel("foo"));
        String childLabelPath = labelPath.replace(parent, child);

        browser.loginAsAdmin();
        assertPullUpAvailability(childLabelPath, false);
    }

    public void testPullUpLinkPresent() throws Exception
    {
        xmlRpcHelper.insertTrivialProject(random, false);
        String labelPath = insertLabel(random, createLabel("foo"));

        browser.loginAsAdmin();
        assertPullUpAvailability(labelPath, true);
    }

    private void assertPullUpAvailability(String path, boolean expectedAvailable)
    {
        ListPage labelsPage = browser.openAndWaitFor(ListPage.class, PathUtils.getParentPath(path));
        String baseName = PathUtils.getBaseName(path);
        assertTrue(labelsPage.isItemPresent(baseName));
        assertEquals(expectedAvailable, browser.isElementIdPresent(labelsPage.getActionId(ConfigurationRefactoringManager.ACTION_PULL_UP, baseName)));
    }

    public void testPullUp() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";

        ListPage listPage = prepareProperty(parent, child);
        PullUpForm pullUpForm = listPage.clickPullUp(TEST_PROPERTY_NAME);
        pullUpForm.waitFor();
        pullUpForm.pullUpFormElements(parent);

        listPage.waitFor();
        assertTrue(listPage.isItemPresent(TEST_PROPERTY_NAME));
        assertTrue(listPage.isAnnotationPresent(TEST_PROPERTY_NAME, ListPage.ANNOTATION_INHERITED));
        
        listPage = browser.openAndWaitFor(ListPage.class, listPage.getPath().replace(child, parent));
        assertTrue(listPage.isItemPresent(TEST_PROPERTY_NAME));
    }

    public void testPullUpCancel() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";

        ListPage listPage = prepareProperty(parent, child);
        PullUpForm pullUpForm = listPage.clickPullUp(TEST_PROPERTY_NAME);
        pullUpForm.waitFor();
        pullUpForm.cancelFormElements(parent);

        listPage.waitFor();
        assertTrue(listPage.isItemPresent(TEST_PROPERTY_NAME));
        assertFalse(listPage.isAnnotationPresent(TEST_PROPERTY_NAME, ListPage.ANNOTATION_INHERITED));
    }

    public void testPullUpTriggerClearsTriggerId() throws Exception
    {
        // CIB-2673
        String parent = random + "-parent";
        String child = random + "-child";
        setupHierarchy(parent, child);

        Hashtable<String, Object> trigger = xmlRpcHelper.createDefaultConfig(ScmBuildTriggerConfiguration.class);
        trigger.put("name", "scm");
        String childTriggersPath = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, child, "triggers");
        String childTriggerPath = xmlRpcHelper.insertConfig(childTriggersPath, trigger);
        String parentTriggerPath = xmlRpcHelper.pullUpConfig(childTriggerPath, parent);

        browser.loginAsAdmin();
        ListPage triggersPage = browser.openAndWaitFor(ListPage.class, childTriggersPath);
        assertEquals("scheduled", triggersPage.getCellContent(0, 2));
        triggersPage = browser.openAndWaitFor(ListPage.class, getParentPath(parentTriggerPath));
        assertEquals("n/a", triggersPage.getCellContent(0, 2));
    }
    
    private ListPage prepareProperty(String parent, String child) throws Exception
    {
        setupHierarchy(parent, child);
        String propertyPath = xmlRpcHelper.insertProjectProperty(child, TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);

        browser.loginAsAdmin();
        ListPage propertyList = browser.openAndWaitFor(ListPage.class, getParentPath(propertyPath));
        assertTrue(propertyList.isItemPresent(TEST_PROPERTY_NAME));
        assertTrue(propertyList.isActionLinkPresent(TEST_PROPERTY_NAME, ConfigurationRefactoringManager.ACTION_PULL_UP));
        return propertyList;
    }

    private String setupHierarchy(String parentName, String childName) throws Exception
    {
        String parentPath = xmlRpcHelper.insertTrivialProject(parentName, true);
        xmlRpcHelper.insertTrivialProject(childName, parentName, false);
        assertEquals(asList(childName), new ArrayList<String>(xmlRpcHelper.getTemplateChildren(parentPath)));
        return parentPath;
    }
}