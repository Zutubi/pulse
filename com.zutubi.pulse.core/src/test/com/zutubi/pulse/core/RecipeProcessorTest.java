package com.zutubi.pulse.core;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.commands.CommandGroupConfiguration;
import com.zutubi.pulse.core.commands.DefaultArtifactFactory;
import com.zutubi.pulse.core.commands.DefaultCommandFactory;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.LinkArtifactConfiguration;
import com.zutubi.pulse.core.dependency.ivy.IvyClient;
import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.pulse.core.engine.FixedPulseFileProvider;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.PropertyConfiguration;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.events.*;
import com.zutubi.pulse.core.model.PersistentFeature;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorFactory;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.util.io.IOUtils;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RecipeProcessorTest extends PulseTestCase implements EventListener
{
    private File baseDir;
    private File outputDir;
    private RecipePaths paths;
    private RecipeProcessor recipeProcessor;
    private EventManager eventManager;
    private WiringObjectFactory objectFactory;
    private DefaultCommandFactory commandFactory;
    private DefaultArtifactFactory outputFactory;
    private DefaultPostProcessorFactory postProcessorFactory;
    private BlockingQueue<Event> events;
    private boolean waitMode = false;
    private Semaphore semaphore = new Semaphore(0);
    private ResourceRepository resourceRepository = new InMemoryResourceRepository();
    private TypeRegistry typeRegistry;

    public void setUp() throws Exception
    {
        super.setUp();
        baseDir = FileSystemUtils.createTempDir(getClass().getName(), ".base");
        outputDir = FileSystemUtils.createTempDir(getClass().getName(), ".out");
        paths = new SimpleRecipePaths(baseDir, outputDir);

        objectFactory = new WiringObjectFactory();
        commandFactory = new DefaultCommandFactory();
        commandFactory.setObjectFactory(objectFactory);
        outputFactory = new DefaultArtifactFactory();
        outputFactory.setObjectFactory(objectFactory);
        postProcessorFactory = new DefaultPostProcessorFactory();
        postProcessorFactory.setObjectFactory(objectFactory);

        recipeProcessor = new RecipeProcessor();
        eventManager = new DefaultEventManager();
        recipeProcessor.setEventManager(eventManager);
        recipeProcessor.setCommandFactory(commandFactory);
        recipeProcessor.setOutputFactory(outputFactory);
        recipeProcessor.setPostProcessorFactory(postProcessorFactory);
        events = new LinkedBlockingQueue<Event>(10);
        eventManager.register(this);

        typeRegistry = new TypeRegistry();
        typeRegistry.register(ProjectRecipesConfiguration.class);
        typeRegistry.register(RecipeConfiguration.class);
        typeRegistry.register(PropertyConfiguration.class);
        typeRegistry.register(DirectoryArtifactConfiguration.class);
        typeRegistry.register(LinkArtifactConfiguration.class);
        typeRegistry.register(FileArtifactConfiguration.class);
        typeRegistry.register(CommandGroupConfiguration.class);
        typeRegistry.register(NoopCommandConfiguration.class);
        typeRegistry.register(FailureCommandConfiguration.class);
        typeRegistry.register(ExceptionCommandConfiguration.class);
        typeRegistry.register(UnexpectedExceptionCommandConfiguration.class);

        objectFactory.initProperties(this);

        PulseFileLoaderFactory fileLoaderFactory = new PulseFileLoaderFactory();
        fileLoaderFactory.setTypeRegistry(typeRegistry);
        fileLoaderFactory.setObjectFactory(objectFactory);
        fileLoaderFactory.init();
        fileLoaderFactory.register("command", CommandGroupConfiguration.class);
        fileLoaderFactory.register("noop", NoopCommandConfiguration.class);
        fileLoaderFactory.register("failure", FailureCommandConfiguration.class);
        fileLoaderFactory.register("exception", ExceptionCommandConfiguration.class);
        fileLoaderFactory.register("unexpected-exception", UnexpectedExceptionCommandConfiguration.class);

        recipeProcessor.setFileLoaderFactory(fileLoaderFactory);

        IvyManager ivyManager = mock(IvyManager.class);
        IvyClient ivyClient = mock(IvyClient.class);
        stub(ivyClient.hasDependencies((ModuleDescriptor) anyObject())).toReturn(false);
        stub(ivyManager.createIvyClient(anyString())).toReturn(ivyClient);
        recipeProcessor.setIvyManager(ivyManager);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(baseDir);
        removeDirectory(outputDir);

        super.tearDown();
    }

    public void testBasicRecipe() throws Exception
    {
        runBasicRecipe("default");
        assertRecipeCommenced(1, "default");
        assertCommandsCompleted(ResultState.SUCCESS, "bootstrap", "greeting");
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
    }

    public void testExceptionDuringBootstrap() throws Exception
    {
        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
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
        assertCommandCommenced(1, "bootstrap");
        assertCommandCompleted(1, ResultState.SUCCESS);
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

    private PulseExecutionContext runBasicRecipe(String recipeName) throws IOException
    {
        PulseExecutionContext context = makeContext(1, recipeName);
        recipeProcessor.build(new RecipeRequest(new SimpleBootstrapper(), getPulseFile("basic"), context));
        return context;
    }

    private PulseExecutionContext makeContext(long id, String recipeName)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.setWorkingDir(paths.getBaseDir());
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, paths);
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, resourceRepository);
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_ID, Long.toString(id));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE, recipeName);
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_PROJECT, "project");
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_STAGE, "stage");
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_ORGANISATION, "org");
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_MASTER_URL, "http://localhost:8080");
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
        Thread.yield();
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
    }

    public void testCommandBackwardsCompatibility() throws Exception
    {
        PulseExecutionContext context = makeContext(1, "default");
        recipeProcessor.build(new RecipeRequest(new SimpleBootstrapper(), getPulseFile("command"), context));
        assertRecipeCommenced(1, "default");
        assertCommandsCompleted(ResultState.SUCCESS, "bootstrap", "nested-name", "outer-name");
        assertRecipeCompleted(1, ResultState.SUCCESS);
        assertNoMoreEvents();
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
        PersistentFeature feature = e.getResult().getFeatures().get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals(message, feature.getSummary());
    }

    private void assertCommandError(long id, String message)
    {
        CommandCompletedEvent e = assertCommandCompleted(id, ResultState.ERROR);
        PersistentFeature feature = e.getResult().getFeatures().get(0);
        assertEquals(Feature.Level.ERROR, feature.getLevel());
        assertEquals(message, feature.getSummary());
    }

    private void assertCommandsCompleted(ResultState result, String... commandNames)
    {
        for (String commandName : commandNames)
        {
            assertCommandCommenced(1, commandName);
            assertCommandCompleted(1, result);
        }
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
        PersistentFeature feature = e.getResult().getFeatures().get(0);
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

    private PulseFileProvider getPulseFile(String name) throws IOException
    {
        return new FixedPulseFileProvider(IOUtils.inputStreamToString(getInput(name, "xml")));
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof RecipeStatusEvent)
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
        private Bootstrapper bootstrapper;
        private PulseFileProvider provider;
        private String recipe;

        public AsyncRunner(RecipeProcessor recipeProcessor, long id, Bootstrapper bootstrapper, PulseFileProvider provider, String recipe)
        {
            this.recipeProcessor = recipeProcessor;
            this.id = id;
            this.bootstrapper = bootstrapper;
            this.provider = provider;
            this.recipe = recipe;
        }

        public void run()
        {
            recipeProcessor.build(new RecipeRequest(bootstrapper, provider, makeContext(id, recipe)));
        }
    }
}
