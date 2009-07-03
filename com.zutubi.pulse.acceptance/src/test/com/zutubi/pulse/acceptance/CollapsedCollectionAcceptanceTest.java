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
public class CollapsedCollectionAcceptanceTest extends SeleniumTestBase
{
    private static final String RECIPE_DEFAULT = "default";
    private static final String ELEMENT_RECIPES = "recipes";

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

    public void testCollectionCollapsed() throws Exception
    {
        ConfigPage typePage = addProjectAndNavigateToType();
        assertTrue(typePage.isCollapsedCollectionPresent());
        assertFalse(typePage.isLinksBoxPresent());
    }

    public void testNavigateToCollapsedCollectionItem() throws Exception
    {
        addProjectAndNavigateToType();

        ListPage listPage = browser.createPage(ListPage.class, getRecipesPath(random));
        listPage.clickView(RECIPE_DEFAULT);
        waitForRecipePage(random, RECIPE_DEFAULT);
    }

    public void testAddCollapsedCollectionItem() throws Exception
    {
        final String RECIPE = "new recipe";

        addProjectAndNavigateToType();
        ListPage listPage = browser.createPage(ListPage.class, getRecipesPath(random));
        listPage.clickAdd();

        ConfigurationForm recipeForm = browser.createForm(ConfigurationForm.class, RecipeConfiguration.class, true);
        recipeForm.waitFor();
        recipeForm.finishNamedFormElements(asPair("name", RECIPE));

        waitForRecipePage(random, RECIPE);
    }

    public void testDeleteCollapsedCollectionItemFromParent() throws Exception
    {
        ConfigPage typePage = addProjectAndNavigateToType();

        ListPage listPage = browser.createPage(ListPage.class, getRecipesPath(random));
        DeleteConfirmPage confirmPage = listPage.clickDelete(RECIPE_DEFAULT);
        confirmPage.waitFor();
        confirmPage.clickDelete();
        typePage.waitFor();
        assertFalse(listPage.isItemPresent(RECIPE_DEFAULT));
    }

    public void testDeleteCollapsedCollectionItemFromItem() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();

        String recipePath = getRecipePath(random, RECIPE_DEFAULT);
        CompositePage recipePage = browser.openAndWaitFor(CompositePage.class, recipePath);

        recipePage.clickAction(AccessManager.ACTION_DELETE);
        DeleteConfirmPage confirmPage = browser.createPage(DeleteConfirmPage.class, recipePath, false);
        confirmPage.waitFor();
        confirmPage.clickDelete();

        browser.openAndWaitFor(CompositePage.class, PathUtils.getPath(projectPath, Constants.Project.TYPE));

        ListPage listPage = browser.createPage(ListPage.class, getRecipesPath(random));
        assertFalse(listPage.isItemPresent(RECIPE_DEFAULT));
    }

    public void testCloneCollapsedCollectionItem() throws Exception
    {
        final String CLONE_NAME = "new recipe";

        addProjectAndNavigateToType();
        ListPage listPage = browser.createPage(ListPage.class, getRecipesPath(random));
        listPage.clickClone(RECIPE_DEFAULT);

        CloneForm cloneForm = browser.createForm(CloneForm.class, false);
        cloneForm.waitFor();
        cloneForm.cloneFormElements(CLONE_NAME);

        waitForRecipePage(random, CLONE_NAME);
    }

    public void testRenameCollapsedCollectionItem() throws Exception
    {
        final String NEW_NAME = "new recipe";

        xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();

        String recipePath = getRecipePath(random, RECIPE_DEFAULT);
        browser.openAndWaitFor(CompositePage.class, recipePath);

        ConfigurationForm recipeForm = browser.createForm(ConfigurationForm.class, RecipeConfiguration.class, true);
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
        String projectPath = xmlRpcHelper.insertTrivialProject(random, false);

        loginAsAdmin();
        CompositePage typePage = browser.openAndWaitFor(CompositePage.class, PathUtils.getPath(projectPath, Constants.Project.TYPE));
        assertTrue(typePage.isConfigureLinkPresent());
        typePage.clickConfigure();

        SelectTypeState state = new SelectTypeState(browser);
        state.waitFor();
        state.nextFormElements("zutubi.multiRecipeTypeConfig");

        typePage.waitFor();
        assertTrue(typePage.isCollapsedCollectionPresent());

        ListPage listPage = browser.createPage(ListPage.class, getRecipesPath(random));
        listPage.clickAdd();

        ConfigurationForm recipeForm = browser.createForm(ConfigurationForm.class, RecipeConfiguration.class, true);
        recipeForm.waitFor();
        recipeForm.finishNamedFormElements(asPair("name", RECIPE_DEFAULT));

        CompositePage recipePage = waitForRecipePage(random, RECIPE_DEFAULT);
        recipePage.clickAction(AccessManager.ACTION_DELETE);

        DeleteConfirmPage confirmPage = browser.createPage(DeleteConfirmPage.class, getRecipePath(random, RECIPE_DEFAULT), false);
        confirmPage.waitFor();
        confirmPage.clickDelete();
        
        typePage.waitFor();
        assertFalse(listPage.isItemPresent(RECIPE_DEFAULT));
    }

    private ConfigPage addProjectAndNavigateToType() throws Exception
    {
        String projectPath = xmlRpcHelper.insertSimpleProject(random, false);

        loginAsAdmin();

        return browser.openAndWaitFor(CompositePage.class, PathUtils.getPath(projectPath, Constants.Project.TYPE));
    }

    private CompositePage waitForRecipePage(String project, String recipeName)
    {
        return browser.openAndWaitFor(CompositePage.class, getRecipePath(project, recipeName));
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