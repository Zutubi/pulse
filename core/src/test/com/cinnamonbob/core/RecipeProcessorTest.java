package com.cinnamonbob.core;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.Feature;
import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.events.DefaultEventManager;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventListener;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.build.*;
import com.cinnamonbob.test.BobTestCase;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 
 *
 */
public class RecipeProcessorTest extends BobTestCase implements EventListener
{
    private File baseDir;
    private File outputDir;
    private RecipeProcessor recipeProcessor;
    private EventManager eventManager;
    private BlockingQueue<Event> events;
    private boolean waitMode = false;
    private Semaphore semaphore = new Semaphore(0);
    private Semaphore eventSemaphore = new Semaphore(0);

    public void setUp() throws Exception
    {
        super.setUp();
        baseDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".base");
        outputDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".out");
        recipeProcessor = new RecipeProcessor();
        eventManager = new DefaultEventManager();
        recipeProcessor.setEventManager(eventManager);
        recipeProcessor.setResourceRepository(new FileResourceRepository());
        events = new LinkedBlockingQueue<Event>(10);
        eventManager.register(this);
        recipeProcessor.init();
        recipeProcessor.getFileLoader().register("failure", FailureCommand.class);
        recipeProcessor.getFileLoader().register("exception", ExceptionCommand.class);
        recipeProcessor.getFileLoader().register("unexpected-exception", UnexpectedExceptionCommand.class);
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.removeDirectory(baseDir);
        FileSystemUtils.removeDirectory(outputDir);
        recipeProcessor = null;
        eventManager = null;
        events = null;
        super.tearDown();
    }

    public void testBasicRecipe() throws Exception
    {
        recipeProcessor.build(1, new SimpleRecipePaths(), new SimpleBootstrapper(), getBobFile("basic"), "default");
        assertRecipeCommenced(1, "default");
        assertCommandCommenced(1, "greeting");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
        assertOutputFile(0, "greeting", "hello world\n");
    }

    public void testExceptionDuringBootstrap() throws Exception
    {
        ErrorBootstrapper bootstrapper = new ErrorBootstrapper(new BuildException("test exception"));
        recipeProcessor.build(1, new SimpleRecipePaths(), bootstrapper, getBobFile("basic"), "default");
        assertRecipeCommenced(1, "default");
        assertRecipeError(1, "test exception");
        assertNoMoreEvents();
    }

    public void testNoDefaultRecipe() throws Exception
    {
        recipeProcessor.build(1, new SimpleRecipePaths(), new SimpleBootstrapper(), getBobFile("nodefault"), null);
        assertRecipeCommenced(1, null);
        assertRecipeError(1, "Please specify a default recipe for your project.");
        assertNoMoreEvents();
    }

    public void testCommandFailure() throws Exception
    {
        recipeProcessor.build(1, new SimpleRecipePaths(), new SimpleBootstrapper(), getBobFile("basic"), "failure");
        assertRecipeCommenced(1, "failure");
        assertCommandCommenced(1, "born to fail");
        assertCommandFailure(1, "failure command");
        // Counter intuitive perhaps: the state is maintained by
        // RecipeControllers so it is not used from this event
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
    }

    public void testCommandException() throws Exception
    {
        recipeProcessor.build(1, new SimpleRecipePaths(), new SimpleBootstrapper(), getBobFile("basic"), "exception");
        assertRecipeCommenced(1, "exception");
        assertCommandCommenced(1, "predictable");
        assertCommandError(1, "exception command");
        // Counter intuitive perhaps: the state is maintained by
        // RecipeControllers so it is not used from this event
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
    }

    public void testCommandUnexpectedException() throws Exception
    {
        recipeProcessor.build(1, new SimpleRecipePaths(), new SimpleBootstrapper(), getBobFile("basic"), "unexpected exception");
        assertRecipeCommenced(1, "unexpected exception");
        assertCommandCommenced(1, "oops");
        assertCommandError(1, "Unexpected error: unexpected exception command");
        // Counter intuitive perhaps: the state is maintained by
        // RecipeControllers so it is not used from this event
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
    }

    public void testTerminate() throws Exception
    {
        waitMode = true;
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, new SimpleRecipePaths(), new SimpleBootstrapper(), getBobFile("basic"), "default");
        Thread thread = new Thread(runner);
        thread.start();
        assertRecipeCommenced(1, "default");
        recipeProcessor.terminateRecipe(1);
        semaphore.release();
        // Counter intuitive perhaps: the state is maintained by
        // RecipeControllers so it is not used from this event
        assertRecipeCompleted(1, ResultState.SUCCESS);
        semaphore.release();
        assertNoMoreEvents();
        thread.join();
    }

    public void testTerminateRaceWithCommand() throws Exception
    {
        waitMode = true;
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, new SimpleRecipePaths(), new SimpleBootstrapper(), getBobFile("basic"), "default");
        Thread thread = new Thread(runner);
        thread.start();
        assertRecipeCommenced(1, "default");
        semaphore.release();
        // Try and make the race fair...
        Thread.sleep(500);
        recipeProcessor.terminateRecipe(1);

        RecipeEvent e = assertEvent(1);
        if (e instanceof CommandCommencedEvent)
        {
            // The command got in first
            CommandCommencedEvent ce = (CommandCommencedEvent) e;
            assertEquals("greeting", ce.getName());
            semaphore.release();
            e = assertEvent(1);
            assertTrue(e instanceof CommandCompletedEvent);
            semaphore.release();
            e = assertEvent(1);
        }

        assertTrue(e instanceof RecipeCompletedEvent);
        semaphore.release();
        assertNoMoreEvents();
        thread.join();
    }

    public void testTerminateDuringCommand() throws Exception
    {
        waitMode = true;
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, new SimpleRecipePaths(), new SimpleBootstrapper(), getBobFile("basic"), "default");
        Thread thread = new Thread(runner);
        thread.start();
        assertRecipeCommenced(1, "default");
        semaphore.release();
        assertCommandCommenced(1, "greeting");
        recipeProcessor.terminateRecipe(1);
        semaphore.release();
        assertCommandCompleted(1, ResultState.ERROR);
        semaphore.release();
        // Counter intuitive perhaps: the state is maintained by
        // RecipeControllers so it is not used from this event
        assertRecipeCompleted(1, ResultState.SUCCESS);
        semaphore.release();
        assertNoMoreEvents();
        thread.join();
    }

    private void assertOutputFile(int commandIndex, String commandName, String contents) throws IOException
    {
        String dirName = RecipeProcessor.getCommandDirName(commandIndex, new CommandResult(commandName));
        File outDir = new File(outputDir, dirName);
        assertTrue(outDir.isDirectory());
        File outFile = new File(outDir, FileSystemUtils.composeFilename(ExecutableCommand.OUTPUT_NAME, "output.txt"));
        assertTrue(outFile.isFile());
        String actualContents = IOUtils.fileToString(outFile);
        assertEquals(contents, actualContents);
    }

    private void assertNoMoreEvents()
    {
        assertEquals(0, events.size());
    }

    private void assertRecipeCommenced(long id, String name)
    {
        Event e = assertEvent(id);
        assertTrue(e instanceof RecipeCommencedEvent);

        RecipeCommencedEvent ce = (RecipeCommencedEvent) e;
        assertEquals(name, ce.getName());
    }

    private void assertCommandCommenced(long id, String name)
    {
        Event e = assertEvent(id);
        assertTrue(e instanceof CommandCommencedEvent);

        CommandCommencedEvent ce = (CommandCommencedEvent) e;
        assertEquals(name, ce.getName());
    }

    private CommandCompletedEvent assertCommandCompleted(long id, ResultState state)
    {
        Event e = assertEvent(id);
        assertTrue(e instanceof CommandCompletedEvent);

        CommandCompletedEvent ce = (CommandCompletedEvent) e;
        assertEquals(state, ce.getResult().getState());
        return ce;
    }

    private void assertCommandFailure(long id, String message)
    {
        CommandCompletedEvent e = assertCommandCompleted(id, ResultState.FAILURE);
        Feature feature = e.getResult().getFeatures().get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals(message, feature.getSummary());
    }

    private void assertCommandError(long id, String message)
    {
        CommandCompletedEvent e = assertCommandCompleted(id, ResultState.ERROR);
        Feature feature = e.getResult().getFeatures().get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals(message, feature.getSummary());
    }

    private RecipeCompletedEvent assertRecipeCompleted(long id, ResultState state)
    {
        Event e = assertEvent(id);
        assertTrue(e instanceof RecipeCompletedEvent);

        RecipeCompletedEvent ce = (RecipeCompletedEvent) e;
        assertEquals(state, ce.getResult().getState());
        return ce;
    }

    private void assertRecipeError(long id, String message)
    {
        RecipeCompletedEvent e = assertRecipeCompleted(id, ResultState.ERROR);
        Feature feature = e.getResult().getFeatures().get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals(message, feature.getSummary());
    }

    private void assertRecipeFailure(long id, String message)
    {
        RecipeCompletedEvent e = assertRecipeCompleted(id, ResultState.FAILURE);
        Feature feature = e.getResult().getFeatures().get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals(message, feature.getSummary());
    }

    private RecipeEvent assertEvent(long id)
    {
        Event e = null;

        try
        {
            e = events.poll(30, TimeUnit.SECONDS);
        }
        catch (InterruptedException e1)
        {
            e1.printStackTrace();
            fail();
        }

        assertNotNull(e);
        assertTrue(e instanceof RecipeEvent);
        RecipeEvent re = (RecipeEvent) e;
        assertEquals(id, re.getRecipeId());
        return re;
    }

    private String getBobFile(String name) throws IOException
    {
        return IOUtils.inputStreamToString(getInput(name));
    }

    public void handleEvent(Event evt)
    {
        events.add(evt);

        if (waitMode)
        {
            try
            {
                semaphore.acquire();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{Event.class};
    }

    public class SimpleRecipePaths implements RecipePaths
    {
        public File getBaseDir()
        {
            return baseDir;
        }

        public File getOutputDir()
        {
            return outputDir;
        }
    }

    public class SimpleBootstrapper extends BootstrapperSupport
    {
        public void bootstrap(long recipeId, RecipePaths paths) throws BuildException
        {
            // Do nothing.
        }
    }

    public class ErrorBootstrapper extends BootstrapperSupport
    {
        private BuildException exception;

        public ErrorBootstrapper(BuildException exception)
        {
            this.exception = exception;
        }

        public void bootstrap(long recipeId, RecipePaths paths) throws BuildException
        {
            throw exception;
        }

        public Exception getException()
        {
            return exception;
        }
    }

    public class AsyncRunner implements Runnable
    {
        private RecipeProcessor recipeProcessor;
        private long id;
        private RecipePaths paths;
        private Bootstrapper bootstrapper;
        private String source;
        private String recipe;

        public AsyncRunner(RecipeProcessor recipeProcessor, long id, RecipePaths paths, Bootstrapper bootstrapper, String source, String recipe)
        {
            this.recipeProcessor = recipeProcessor;
            this.id = id;
            this.paths = paths;
            this.bootstrapper = bootstrapper;
            this.source = source;
            this.recipe = recipe;
        }

        public void run()
        {
            recipeProcessor.build(id, paths, bootstrapper, source, recipe);
        }
    }
}
