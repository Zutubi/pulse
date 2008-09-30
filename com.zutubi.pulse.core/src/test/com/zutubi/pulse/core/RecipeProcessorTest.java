package com.zutubi.pulse.core;

import static com.zutubi.pulse.core.BuildProperties.*;
import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.DefaultEventManager;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.io.IOUtils;

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

        // just a little bit of wiring tomfoolary to inject the event manager into the recipe.
        ObjectFactory factory = new DefaultObjectFactory()
        {
            public <T> T buildBean(Class<? extends T> clazz) throws Exception
            {
                T bean = super.buildBean(clazz);
                if (bean instanceof Recipe)
                {
                    ((Recipe)bean).setEventManager(eventManager);
                }
                return bean;
            }
        };

        PulseFileLoaderFactory fileLoaderFactory = new PulseFileLoaderFactory();
        fileLoaderFactory.setObjectFactory(factory);
        fileLoaderFactory.register("noop", NoopCommand.class);
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
        runBasicRecipe("default");
        assertRecipeCommenced(1, "default");
        assertCommandCommenced(1, "bootstrap");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertCommandCommenced(1, "greeting");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
    }

    public void testVersion() throws Exception
    {
        ExecutionContext context = runBasicRecipe("version");
        assertRecipeCommenced(1, "version");
        assertCommandCommenced(1, "bootstrap");
        assertCommandCompleted(1, ResultState.SUCCESS);
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
        assertEquals("test version", context.getVersion());
    }

    public void testExceptionDuringBootstrap() throws Exception
    {
        ErrorBootstrapper bootstrapper = new ErrorBootstrapper(new BuildException("test exception"));
        recipeProcessor.build(new RecipeRequest(bootstrapper, getPulseFile("basic"), makeContext(1, "default")));
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
        recipeProcessor.build(new RecipeRequest(new SimpleBootstrapper(), getPulseFile("nodefault"), makeContext(1, null)));
        assertRecipeCommenced(1, null);
        assertRecipeError(1, "Please specify a default recipe for your project.");
        assertNoMoreEvents();
    }

    public void testCommandFailure() throws Exception
    {
        runBasicRecipe("failure");
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
        runBasicRecipe("exception");
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
        runBasicRecipe("unexpected exception");
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

    private ExecutionContext runBasicRecipe(String recipeName) throws IOException
    {
        ExecutionContext context = makeContext(1, recipeName);
        recipeProcessor.build(new RecipeRequest(new SimpleBootstrapper(), getPulseFile("basic"), context));
        return context;
    }

    private ExecutionContext makeContext(long id, String recipeName)
    {
        ExecutionContext context = new ExecutionContext();
        context.setWorkingDir(paths.getBaseDir());
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, paths);
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, resourceRepository);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, Long.toString(id));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE, recipeName);
        return context;
    }

    public void testTerminate() throws Exception
    {
        waitMode = true;
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, new SimpleBootstrapper(), getPulseFile("basic"), "default");
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
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, new SimpleBootstrapper(), getPulseFile("basic"), "default");
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
        AsyncRunner runner = new AsyncRunner(recipeProcessor, 1, new SimpleBootstrapper(), getPulseFile("basic"), "default");
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
        if(evt instanceof RecipeStatusEvent)
        {
            // We ignore status events as they do not affect behaviour.
            return;
        }
        
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
        public void bootstrap(ExecutionContext context) throws BuildException
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

        public void bootstrap(ExecutionContext context) throws BuildException
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
        private Bootstrapper bootstrapper;
        private String source;
        private String recipe;

        public AsyncRunner(RecipeProcessor recipeProcessor, long id, Bootstrapper bootstrapper, String source, String recipe)
        {
            this.recipeProcessor = recipeProcessor;
            this.id = id;
            this.bootstrapper = bootstrapper;
            this.source = source;
            this.recipe = recipe;
        }

        public void run()
        {
            recipeProcessor.build(new RecipeRequest(bootstrapper, source, makeContext(id, recipe)));
        }
    }
}
