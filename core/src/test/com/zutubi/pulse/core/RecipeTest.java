package com.zutubi.pulse.core;

import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;

/**
 * <class comment/>
 */
public class RecipeTest extends PulseTestCase
{
    private Recipe recipe;
    private DefaultEventManager eventManager;
    private RecipePaths paths;
    private ExecutionContext context;

    public RecipeTest()
    {
    }

    public RecipeTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        eventManager = new DefaultEventManager();

        recipe = new Recipe();
        recipe.setEventManager(eventManager);

        File baseDir = FileSystemUtils.createTempDir(getClass().getName(), ".base");
        File outputDir = FileSystemUtils.createTempDir(getClass().getName(), ".out");
        paths = new SimpleRecipePaths(baseDir, outputDir);

        context = new ExecutionContext();
        context.setWorkingDir(paths.getBaseDir());
        context.addInternalValue(BuildProperties.PROPERTY_RECIPE_PATHS, paths);
    }

    protected void tearDown() throws Exception
    {
        eventManager = null;
        recipe = null;
        paths = null;
        context = null;

        super.tearDown();
    }

    public void testExecuteRecipe()
    {
        recipe.add(new NoopCommand());
        recipe.execute(context);

        for (Command command : recipe.getCommands())
        {
            assertTrue(((NoopCommand)command).hasExecuted());
        }
    }
}
