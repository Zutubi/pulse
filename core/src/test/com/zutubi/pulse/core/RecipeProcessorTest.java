package com.zutubi.pulse.core;

import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.*;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 
 *
 */
public class RecipeProcessorTest extends PulseTestCase implements EventListener
{
    private File baseDir;
    private File outputDir;
    private RecipePaths paths;
    private RecipeProcessor recipeProcessor;
    private EventManager eventManager;
    private BlockingQueue<Event> events;
    private boolean waitMode = false;
    private Semaphore semaphore = new Semaphore(0);
    private ResourceRepository resourceRepository = new FileResourceRepository();

    public void setUp() throws Exception
    {
        super.setUp();
        baseDir = FileSystemUtils.createTempDir(getClass().getName(), ".base");
        outputDir = FileSystemUtils.createTempDir(getClass().getName(), ".out");
        paths = new SimpleRecipePaths(baseDir, outputDir);

        recipeProcessor = new RecipeProcessor();
        eventManager = new DefaultEventManager();
        recipeProcessor.setEventManager(eventManager);
        events = new LinkedBlockingQueue<Event>(10);
        eventManager.register(this);

// just a little bit of wiring tomfoolary.
        ObjectFactory factory = new ObjectFactory()
        {
            public Object buildBean(Class clazz) throws Exception
            {
                Object bean = super.buildBean(clazz);
                if (bean instanceof Recipe)
                {
                    ((Recipe)bean).setEventManager(eventManager);
                }
                return bean;
            }
        };

        PulseFileLoaderFactory fileLoaderFactory = new PulseFileLoaderFactory();
        fileLoaderFactory.setObjectFactory(factory);
        fileLoaderFactory.register("failure", FailureCommand.class);
        fileLoaderFactory.register("exception", ExceptionCommand.class);
        fileLoaderFactory.register("unexpected-exception", UnexpectedExceptionCommand.class);

        recipeProcessor.setFileLoaderFactory(fileLoaderFactory);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(baseDir);
        removeDirectory(outputDir);
        paths = null;
        recipeProcessor = null;
        eventManager = null;
        events = null;
        super.tearDown();
    }

    public void testBasicRecipe() throws Exception
    {
        recipeProcessor.build(new BuildContext(), new RecipeRequest(1, new SimpleBootstrapper(), getPulseFile("basic"), "default"), paths, resourceRepository, false);
        assertRecipeCommenced(1, "default");
        assertCommandCommenced(1, "bootstrap");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertCommandCommenced(1, "greeting");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
        assertOutputFile(1, "greeting", "hello world" + System.getProperty("line.separator"));
    }

    public void testExceptionDuringBootstrap() throws Exception
    {
        ErrorBootstrapper bootstrapper = new ErrorBootstrapper(new BuildException("test exception"));
        recipeProcessor.build(new BuildContext(), new RecipeRequest(1, bootstrapper, getPulseFile("basic"), "default"), paths, resourceRepository, false);
        assertRecipeCommenced(1, "default");
        assertCommandCommenced(1, "bootstrap");
        assertCommandError(1, "test exception");
        // Counter intuitive perhaps: the state is maintained by
        // RecipeControllers so it is not used from this event
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
    }

    public void testNoDefaultRecipe() throws Exception
    {
        recipeProcessor.build(new BuildContext(), new RecipeRequest(1, new SimpleBootstrapper(), getPulseFile("nodefault"), null), paths, resourceRepository, false);
        assertRecipeCommenced(1, null);
        assertRecipeError(1, "Please specify a default recipe for your project.");
        assertNoMoreEvents();
    }

    public void testCommandFailure() throws Exception
    {
        recipeProcessor.build(new BuildContext(), new RecipeRequest(1, new SimpleBootstrapper(), getPulseFile("basic"), "failure"), paths, resourceRepository, false);
        assertRecipeCommenced(1, "failure");
        assertCommandCommenced(1, "bootstrap");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertCommandCommenced(1, "born to fail");
        assertCommandFailure(1, "failure command");
        // Counter intuitive perhaps: the state is maintained by
        // RecipeControllers so it is not used from this event
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
    }

    public void testCommandException() throws Exception
    {
        recipeProcessor.build(new BuildContext(), new RecipeRequest(1, new SimpleBootstrapper(), getPulseFile("basic"), "exception"), paths, resourceRepository, false);
        assertRecipeCommenced(1, "exception");
        assertCommandCommenced(1, "bootstrap");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertCommandCommenced(1, "predictable");
        assertCommandError(1, "exception command");
        // Counter intuitive perhaps: the state is maintained by
        // RecipeControllers so it is not used from this event
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
    }

    public void testCommandUnexpectedException() throws Exception
    {
        recipeProcessor.build(new BuildContext(), new RecipeRequest(1, new SimpleBootstrapper(), getPulseFile("basic"), "unexpected exception"), paths, resourceRepository, false);
        assertRecipeCommenced(1, "unexpected exception");
        assertCommandCommenced(1, "bootstrap");
        assertCommandCompleted(1, ResultState.SUCCESS);
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
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, paths, new SimpleBootstrapper(), getPulseFile("basic"), "default");
        Thread thread = new Thread(runner);
        thread.start();
        assertRecipeCommenced(1, "default");
        semaphore.release();
        assertCommandCommenced(1, "bootstrap");
        semaphore.release();
        assertCommandCompleted(1, ResultState.SUCCESS);
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
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, paths, new SimpleBootstrapper(), getPulseFile("basic"), "default");
        Thread thread = new Thread(runner);
        thread.start();
        assertRecipeCommenced(1, "default");
        semaphore.release();
        assertCommandCommenced(1, "bootstrap");
        semaphore.release();
        assertCommandCompleted(1, ResultState.SUCCESS);
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

        // there are some file resources that are not being cleaned up in time for the
        // remove directory call in the tearDown. So, we sleep briefly here to give the
        // terminated child process (?) a chance to release its resources.
        Thread.sleep(100);
    }

    public void testTerminateDuringCommand() throws Exception
    {
        waitMode = true;
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, paths, new SimpleBootstrapper(), getPulseFile("basic"), "default");
        Thread thread = new Thread(runner);
        thread.start();
        assertRecipeCommenced(1, "default");
        semaphore.release();
        assertCommandCommenced(1, "bootstrap");
        semaphore.release();
        assertCommandCompleted(1, ResultState.SUCCESS);
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

        // there are some file resources that are not being cleaned up in time for the
        // remove directory call in the tearDown. So, we sleep briefly here to give the
        // terminated child process (?) a chance to release its resources.
        Thread.sleep(100);
    }

    public void testPropertiesImported() throws Exception
    {
        List<ResourceProperty> properties = new ArrayList<ResourceProperty>(1);
        properties.add(new ResourceProperty("property1", "propvalue", true, true, true));
        recipeProcessor.build(new BuildContext(), new RecipeRequest("project", "spec", 1, new SimpleBootstrapper(), getPulseFile("properties"), "default", false, null, properties), paths, resourceRepository, false);
        assertRecipeCommenced(1, "default");
        assertCommandCommenced(1, "bootstrap");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertCommandCommenced(1, "property1");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
        assertOutputFile(1, "property1", "propvalue" + System.getProperty("line.separator"));
    }

    private void assertOutputFile(int commandIndex, String commandName, String contents) throws IOException
    {
        String dirName = Recipe.getCommandDirName(commandIndex, new CommandResult(commandName));
        File outDir = new File(outputDir, dirName);
        assertTrue(outDir.isDirectory());
        File outFile = new File(outDir, FileSystemUtils.composeFilename(Command.OUTPUT_ARTIFACT_NAME, "output.txt"));
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

    private String getPulseFile(String name) throws IOException
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

    public class SimpleBootstrapper extends BootstrapperSupport
    {
        public void bootstrap(CommandContext context) throws BuildException
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

        public void bootstrap(CommandContext context) throws BuildException
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
            recipeProcessor.build(new BuildContext(), new RecipeRequest(id, bootstrapper, source, recipe), paths, resourceRepository, false);
        }
    }
}
