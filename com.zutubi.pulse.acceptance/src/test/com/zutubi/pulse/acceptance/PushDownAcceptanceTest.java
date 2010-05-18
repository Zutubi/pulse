package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.PushDownForm;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getParentPath;

import java.util.ArrayList;
import static java.util.Arrays.asList;

public class PushDownAcceptanceTest extends SeleniumTestBase
{
    private static final String TEST_PROPERTY_NAME   = "aprop";
    private static final String TEST_PROPERTY_VALUE  = "value";

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

    public void testPushDownLinkNotPresentForInheritedItem() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";
        setupHierarchy(parent, child);

        String labelPath = insertLabel(parent, createLabel("foo"));
        String childLabelPath = labelPath.replace(parent, child);

        browser.loginAsAdmin();
        assertPushDownAvailability(childLabelPath, false);
    }

    public void testPushDownLinkPresent() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";
        setupHierarchy(parent, child);

        String labelPath = insertLabel(parent, createLabel("foo"));

        browser.loginAsAdmin();
        assertPushDownAvailability(labelPath, true);
    }

    private void assertPushDownAvailability(String path, boolean expectedAvailable)
    {
        ListPage labelsPage = browser.openAndWaitFor(ListPage.class, PathUtils.getParentPath(path));
        String baseName = PathUtils.getBaseName(path);
        assertTrue(labelsPage.isItemPresent(baseName));
        assertEquals(expectedAvailable, browser.isElementIdPresent(labelsPage.getActionId(ConfigurationRefactoringManager.ACTION_PUSH_DOWN, baseName)));
    }

    public void testPushDown() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";

        ListPage listPage = prepareProperty(parent, child);
        PushDownForm pushDownForm = listPage.clickPushDown(TEST_PROPERTY_NAME);
        pushDownForm.waitFor();
        pushDownForm.pushDownFormElements(child);

        listPage.waitFor();
        assertFalse(listPage.isItemPresent(TEST_PROPERTY_NAME));

        listPage = browser.openAndWaitFor(ListPage.class, listPage.getPath().replace(parent, child));
        assertTrue(listPage.isItemPresent(TEST_PROPERTY_NAME));
        assertFalse(listPage.isAnnotationPresent(TEST_PROPERTY_NAME, ListPage.ANNOTATION_INHERITED));
    }

    public void testPushDownCancel() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";

        // First try from collection page.
        ListPage listPage = prepareProperty(parent, child);
        PushDownForm pushDownForm = listPage.clickPushDown(TEST_PROPERTY_NAME);
        pushDownForm.waitFor();
        pushDownForm.cancelFormElements(parent);

        listPage.waitFor();
        assertTrue(listPage.isItemPresent(TEST_PROPERTY_NAME));

        // Also try from property's own page.
        CompositePage propertyPage = browser.openAndWaitFor(CompositePage.class, PathUtils.getPath(listPage.getPath(), TEST_PROPERTY_NAME));
        propertyPage.clickAction(ConfigurationRefactoringManager.ACTION_PUSH_DOWN);
        pushDownForm.waitFor();
        pushDownForm.cancelFormElements(parent);
        propertyPage.waitFor();
    }

    private ListPage prepareProperty(String parent, String child) throws Exception
    {
        setupHierarchy(parent, child);
        String propertyPath = xmlRpcHelper.insertProjectProperty(parent, TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);

        browser.loginAsAdmin();
        ListPage propertyList = browser.openAndWaitFor(ListPage.class, getParentPath(propertyPath));
        assertTrue(propertyList.isItemPresent(TEST_PROPERTY_NAME));
        assertTrue(propertyList.isActionLinkPresent(TEST_PROPERTY_NAME, ConfigurationRefactoringManager.ACTION_PUSH_DOWN));
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