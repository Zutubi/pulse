package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.PullUpForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.config.ConfigurationRefactoringManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.tove.type.record.PathUtils.getParentPath;

import java.util.ArrayList;
import static java.util.Arrays.asList;

public class PullUpAcceptanceTest extends AcceptanceTestBase
{
    private static final String TEST_PROPERTY_NAME   = "aprop";
    private static final String TEST_PROPERTY_VALUE  = "value";

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

    public void testPullUpLinkNotPresentForInheritedItem() throws Exception
    {
        String parent = random + "-parent";
        String child = random + "-child";
        setupHierarchy(parent, child);

        String labelPath = rpcClient.RemoteApi.addLabel(parent, "foo");
        String childLabelPath = labelPath.replace(parent, child);

        getBrowser().loginAsAdmin();
        assertPullUpAvailability(childLabelPath, false);
    }

    public void testPullUpLinkPresent() throws Exception
    {
        rpcClient.RemoteApi.insertTrivialProject(random, false);
        String labelPath = rpcClient.RemoteApi.addLabel(random, "foo");

        getBrowser().loginAsAdmin();
        assertPullUpAvailability(labelPath, true);
    }

    private void assertPullUpAvailability(String path, boolean expectedAvailable)
    {
        ListPage labelsPage = getBrowser().openAndWaitFor(ListPage.class, PathUtils.getParentPath(path));
        String baseName = PathUtils.getBaseName(path);
        assertTrue(labelsPage.isItemPresent(baseName));
        assertEquals(expectedAvailable, getBrowser().isElementIdPresent(labelsPage.getActionId(ConfigurationRefactoringManager.ACTION_PULL_UP, baseName)));
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
        
        listPage = getBrowser().openAndWaitFor(ListPage.class, listPage.getPath().replace(child, parent));
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

    private ListPage prepareProperty(String parent, String child) throws Exception
    {
        setupHierarchy(parent, child);
        String propertyPath = rpcClient.RemoteApi.insertProjectProperty(child, TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);

        getBrowser().loginAsAdmin();
        ListPage propertyList = getBrowser().openAndWaitFor(ListPage.class, getParentPath(propertyPath));
        assertTrue(propertyList.isItemPresent(TEST_PROPERTY_NAME));
        assertTrue(propertyList.isActionLinkPresent(TEST_PROPERTY_NAME, ConfigurationRefactoringManager.ACTION_PULL_UP));
        return propertyList;
    }

    private String setupHierarchy(String parentName, String childName) throws Exception
    {
        String parentPath = rpcClient.RemoteApi.insertTrivialProject(parentName, true);
        rpcClient.RemoteApi.insertTrivialProject(childName, parentName, false);
        assertEquals(asList(childName), new ArrayList<String>(rpcClient.RemoteApi.getTemplateChildren(parentPath)));
        return parentPath;
    }
}