package com.zutubi.pulse.core;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.validation.annotations.Required;

import java.util.List;
import java.io.File;

/**
 * <class comment/>
 */
public class RecipeTest extends PulseTestCase
{
    private Recipe recipe;
    private DefaultEventManager eventManager;
    private RecipePaths paths;
    private RecipeContext context;

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

        context = new RecipeContext();
        context.setRecipePaths(paths);
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

    /**
     * Test command that records its execution details. 
     */
    private class NoopCommand extends CommandSupport
    {
        private boolean executed;

        public void execute(CommandContext context, CommandResult result)
        {
            executed = true;
        }

        public boolean hasExecuted()
        {
            return executed;
        }
    }
    
}
