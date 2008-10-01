package com.zutubi.pulse.core;

import com.zutubi.events.DefaultEventManager;
import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.BuildProperties.PROPERTY_RECIPE_PATHS;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.core.util.FileSystemUtils;

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
    private File tmpDir;

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

        tmpDir = FileSystemUtils.createTempDir(getClass().getName(), ".tmp");
        File baseDir = new File(tmpDir, "base");
        File outputDir = new File(tmpDir, "out");
        paths = new SimpleRecipePaths(baseDir, outputDir);

        context = new ExecutionContext();
        context.setWorkingDir(paths.getBaseDir());
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, paths);
    }

    protected void tearDown() throws Exception
    {
        eventManager = null;
        recipe = null;
        paths = null;
        context = null;

        removeDirectory(tmpDir);
        tmpDir = null;

        super.tearDown();
    }

    public void testExecuteRecipe()
    {
        recipe.add(new NoopCommand(), null);
        recipe.execute(context);

        for (Command command : recipe.getCommands())
        {
            assertTrue(((NoopCommand)command).hasExecuted());
        }
    }
}
