package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.forms.admin.CloneForm;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.DeleteConfirmPage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Tests for automatically-collapsed collections in the configuration tree.
 * This triggers extra code for dealing with the extra differences between
 * paths in the tree and config paths.
 */
public class CollapsedCollectionAcceptanceTest extends AcceptanceTestBase
{
    private static final String RECIPE_DEFAULT = "default";
    private static final String ELEMENT_RECIPES = "recipes";

    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testCollectionCollapsed() throws Exception
    {
        ConfigPage typePage = addProjectAndNavigateToType();
        assertTrue(typePage.isCollapsedCollectionPresent());
        assertFalse(typePage.isLinksBoxPresent());
    }

    public void testNavigateToCollapsedCollectionItem() throws Exception
    {
        addProjectAndNavigateToType();

        ListPage listPage = getBrowser().createPage(ListPage.class, getRecipesPath(random));
        listPage.clickView(RECIPE_DEFAULT);
        waitForRecipePage(random, RECIPE_DEFAULT);
    }

    public void testAddCollapsedCollectionItem() throws Exception
    {
        final String RECIPE = "new recipe";

        addProjectAndNavigateToType();
        ListPage listPage = getBrowser().createPage(ListPage.class, getRecipesPath(random));
        listPage.clickAdd();

        ConfigurationForm recipeForm = getBrowser().createForm(ConfigurationForm.class, RecipeConfiguration.class, true);
        recipeForm.waitFor();
        recipeForm.finishNamedFormElements(asPair("name", RECIPE));

        waitForRecipePage(random, RECIPE);
    }

    public void testDeleteCollapsedCollectionItemFromParent() throws Exception
    {
        ConfigPage typePage = addProjectAndNavigateToType();

        ListPage listPage = getBrowser().createPage(ListPage.class, getRecipesPath(random));
        DeleteConfirmPage confirmPage = listPage.clickDelete(RECIPE_DEFAULT);
        confirmPage.waitFor();
        confirmPage.clickDelete();
        typePage.waitFor();
        assertFalse(listPage.isItemPresent(RECIPE_DEFAULT));
    }

    public void testDeleteCollapsedCollectionItemFromItem() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random, false);

        getBrowser().loginAsAdmin();

        String recipePath = getRecipePath(random, RECIPE_DEFAULT);
        CompositePage recipePage = getBrowser().openAndWaitFor(CompositePage.class, recipePath);

        recipePage.clickAction(AccessManager.ACTION_DELETE);
        DeleteConfirmPage confirmPage = getBrowser().createPage(DeleteConfirmPage.class, recipePath, false);
        confirmPage.waitFor();
        confirmPage.clickDelete();

        getBrowser().openAndWaitFor(CompositePage.class, PathUtils.getPath(projectPath, Constants.Project.TYPE));

        ListPage listPage = getBrowser().createPage(ListPage.class, getRecipesPath(random));
        assertFalse(listPage.isItemPresent(RECIPE_DEFAULT));
    }

    public void testCloneCollapsedCollectionItem() throws Exception
    {
        final String CLONE_NAME = "new recipe";

        addProjectAndNavigateToType();
        ListPage listPage = getBrowser().createPage(ListPage.class, getRecipesPath(random));
        listPage.clickClone(RECIPE_DEFAULT);

        CloneForm cloneForm = getBrowser().createForm(CloneForm.class, false);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(CLONE_NAME);

        waitForRecipePage(random, CLONE_NAME);
    }

    public void testRenameCollapsedCollectionItem() throws Exception
    {
        final String NEW_NAME = "new recipe";

        rpcClient.RemoteApi.insertSimpleProject(random, false);

        getBrowser().loginAsAdmin();

        String recipePath = getRecipePath(random, RECIPE_DEFAULT);
        getBrowser().openAndWaitFor(CompositePage.class, recipePath);

        ConfigurationForm recipeForm = getBrowser().createForm(ConfigurationForm.class, RecipeConfiguration.class, true);
        recipeForm.waitFor();
        recipeForm.applyFormElements(NEW_NAME);
        
        waitForRecipePage(random, NEW_NAME);
    }

    public void testTreeItemWithCollapsedDynamicallyRenamed() throws Exception
    {
        // The point here is that the tree node for the project type exists
        // before the type is configured - at this point it has no collapsed
        // collection.  If we later configure the type to be multi-recipe,
        // this node is dynamically renamed, and we need to ensure it knows
        // it now has a collapsed collection.
        String projectPath = rpcClient.RemoteApi.insertTrivialProject(random, false);

        getBrowser().loginAsAdmin();
        CompositePage typePage = getBrowser().openAndWaitFor(CompositePage.class, PathUtils.getPath(projectPath, Constants.Project.TYPE));
        assertTrue(typePage.isConfigureLinkPresent());
        typePage.clickConfigure();

        SelectTypeState state = new SelectTypeState(getBrowser());
        state.waitFor();
        state.nextFormElements("zutubi.multiRecipeTypeConfig");

        typePage.waitFor();
        assertTrue(typePage.isCollapsedCollectionPresent());

        ListPage listPage = getBrowser().createPage(ListPage.class, getRecipesPath(random));
        listPage.clickAdd();

        ConfigurationForm recipeForm = getBrowser().createForm(ConfigurationForm.class, RecipeConfiguration.class, true);
        recipeForm.waitFor();
        recipeForm.finishNamedFormElements(asPair("name", RECIPE_DEFAULT));

        CompositePage recipePage = waitForRecipePage(random, RECIPE_DEFAULT);
        recipePage.clickAction(AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = getBrowser().createPage(DeleteConfirmPage.class, getRecipePath(random, RECIPE_DEFAULT), false);
        confirmPage.waitFor();
        confirmPage.clickDelete();
        
        typePage.waitFor();
        assertFalse(listPage.isItemPresent(RECIPE_DEFAULT));
    }

    private ConfigPage addProjectAndNavigateToType() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random, false);

        getBrowser().loginAsAdmin();

        return getBrowser().openAndWaitFor(CompositePage.class, PathUtils.getPath(projectPath, Constants.Project.TYPE));
    }

    private CompositePage waitForRecipePage(String project, String recipeName)
    {
        return getBrowser().openAndWaitFor(CompositePage.class, getRecipePath(project, recipeName));
    }

    private String getRecipePath(String project, String recipeName)
    {
        return PathUtils.getPath(getRecipesPath(project), recipeName);
    }

    private String getRecipesPath(String project)
    {
        return PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project, Constants.Project.TYPE, ELEMENT_RECIPES);
    }
}