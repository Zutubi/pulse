package com.zutubi.pulse.core;

import com.zutubi.events.EventManager;
import static com.zutubi.pulse.core.RecipeUtils.addResourceProperties;
import com.zutubi.pulse.core.dependency.ivy.IvyProvider;
import com.zutubi.pulse.core.dependency.ivy.IvySupport;
import com.zutubi.pulse.core.dependency.ivy.PublishArtifactsCommand;
import com.zutubi.pulse.core.dependency.ivy.RetrieveDependenciesCommand;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.events.RecipeCommencedEvent;
import com.zutubi.pulse.core.events.RecipeCompletedEvent;
import com.zutubi.pulse.core.events.RecipeStatusEvent;
import com.zutubi.pulse.core.model.PersistentTestSuiteResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.TestSuitePersister;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The recipe processor, as the name suggests, is responsible for running recipies.
 */
public class RecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(RecipeProcessor.class);

    private EventManager eventManager;
    private Lock runningLock = new ReentrantLock();
    private long runningRecipe = 0;
    private Recipe runningRecipeInstance = null;
    private boolean terminating = false;
    private PulseFileLoaderFactory fileLoaderFactory;

    private IvyProvider ivyProvider;

    public RecipeProcessor()
    {
    }

    public void init()
    {
        
    }

    public void build(RecipeRequest request)
    {
        // This result holds only the recipe details (stamps, state etc), not
        // the command results.  A full recipe result with command results is
        // assembled elsewhere.
        RecipeResult recipeResult = new RecipeResult(request.getRecipeName());
        recipeResult.setId(request.getId());
        recipeResult.commence();
        
        PersistentTestSuiteResult testResults = new PersistentTestSuiteResult();

        runningRecipe = recipeResult.getId();
        eventManager.publish(new RecipeCommencedEvent(this, recipeResult.getId(), recipeResult.getRecipeName(), recipeResult.getStartTime()));

        PulseExecutionContext context = request.getContext();
        long recipeStartTime = recipeResult.getStartTime();
        pushRecipeContext(context, request, testResults, recipeStartTime);
        try
        {
            // Wrap bootstrapper in a command and run it.
            BootstrapCommand bootstrapCommand = new BootstrapCommand(request.getBootstrapper());

            // Now we can load the recipe from the pulse file
            PulseFile pulseFile = loadPulseFile(request, context);

            String recipeName = request.getRecipeName();
            if (!TextUtils.stringSet(recipeName))
            {
                recipeName = pulseFile.getDefaultRecipe();
                if (!TextUtils.stringSet(recipeName))
                {
                    throw new BuildException("Please specify a default recipe for your project.");
                }
            }

            Recipe recipe = pulseFile.getRecipe(recipeName);
            if (recipe == null)
            {
                throw new BuildException("Undefined recipe '" + recipeName + "'");
            }

            IvySupport ivy = ivyProvider.getIvySupport();
            Command retrieveCommand = ivy.getRetrieveCommandWrapper();
            Command publishCommand = ivy.getPublishCommandWrapper(request);

            recipe.addFirstCommand(retrieveCommand);
            recipe.addFirstCommand(bootstrapCommand);
            recipe.addLastCommand(publishCommand);

            runningRecipeInstance = recipe;

            recipe.execute(context);
        }
        catch (BuildException e)
        {
            recipeResult.error(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            recipeResult.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Storing test results..."));

            RecipePaths paths = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class);
            writeTestResults(paths, testResults);
            recipeResult.setTestSummary(testResults.getSummary());
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Test results stored."));

            compressResults(paths, context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_COMPRESS_ARTIFACTS, false), context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_COMPRESS_WORKING_DIR, false));

            recipeResult.complete();
            RecipeCompletedEvent completedEvent = new RecipeCompletedEvent(this, recipeResult);
            if(context.getVersion() != null)
            {
                completedEvent.setBuildVersion(context.getVersion());
            }

            eventManager.publish(completedEvent);

            context.pop();

            runningLock.lock();
            runningRecipe = 0;
            runningRecipeInstance = null;
            if (terminating)
            {
                terminating = false;
            }
            runningLock.unlock();
        }
    }

    private void compressResults(RecipePaths paths, boolean compressArtifacts, boolean compressWorkingCopy)
    {
        if (compressArtifacts)
        {
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Compressing recipe artifacts..."));
            if(zipDir(paths.getOutputDir()))
            {
                eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Artifacts compressed."));
            }
        }

        if(compressWorkingCopy)
        {
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Compressing working copy snapshot..."));
            if(zipDir(paths.getBaseDir()))
            {
                eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Working copy snapshot compressed."));
            }
        }
    }

    private boolean zipDir(File dir)
    {
        try
        {
            File zipFile = new File(dir.getAbsolutePath() + ".zip");
            ZipUtils.createZip(zipFile, dir, null);
            return true;
        }
        catch (IOException e)
        {
            LOG.severe(e);
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Compression failed: " + e.getMessage() + "."));
            return false;
        }
    }

    private void writeTestResults(RecipePaths paths, PersistentTestSuiteResult testResults)
    {
        try
        {
            TestSuitePersister persister = new TestSuitePersister();
            File testDir = new File(paths.getOutputDir(), RecipeResult.TEST_DIR);
            FileSystemUtils.createDirectory(testDir);
            persister.write(testResults, testDir);
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write out test results", e);
        }
    }

    private void pushRecipeContext(PulseExecutionContext context, RecipeRequest request, PersistentTestSuiteResult testResults, long recipeStartTime)
    {
        context.push();
        context.addString(NAMESPACE_INTERNAL, PROPERTY_BASE_DIR, context.getWorkingDir().getAbsolutePath());
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP, new SimpleDateFormat(TIMESTAMP_FORMAT_STRING).format(new Date(recipeStartTime)));
        context.addString(NAMESPACE_INTERNAL, PROPERTY_RECIPE_TIMESTAMP_MILLIS, Long.toString(recipeStartTime));
        context.addValue(NAMESPACE_INTERNAL, PROPERTY_TEST_RESULTS, testResults);

        if(context.getString(NAMESPACE_INTERNAL, PROPERTY_RECIPE) == null)
        {
            context.addString(PROPERTY_RECIPE, "[default]");
        }

        PulseScope scope = context.getScope();
        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> var: env.entrySet())
        {
            scope.addEnvironmentProperty(var.getKey(), var.getValue());
        }

        addResourceProperties(context, request.getResourceRequirements(), context.getValue(PROPERTY_RESOURCE_REPOSITORY, ResourceRepository.class));
        for(ResourceProperty property: request.getProperties())
        {
            context.add(property);
        }
    }

    private PulseFile loadPulseFile(RecipeRequest request, PulseExecutionContext context) throws BuildException
    {
        context.setLabel(SCOPE_RECIPE);
        PulseScope globalScope = new PulseScope(context.getScope());

        // CIB-286: special case empty file for better reporting
        String pulseFileSource = request.getPulseFileSource();
        if(!TextUtils.stringSet(pulseFileSource))
        {
            throw new BuildException("Unable to parse pulse file: File is empty");
        }

        // load the pulse file from the source.
        InputStream stream = null;
        try
        {
            stream = new ByteArrayInputStream(pulseFileSource.getBytes());

            ResourceRepository resourceRepository = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RESOURCE_REPOSITORY, ResourceRepository.class);
            PulseFile result = new PulseFile();
            PulseFileLoader fileLoader = fileLoaderFactory.createLoader();
            fileLoader.load(stream, result, globalScope, resourceRepository, new RecipeLoadPredicate(result, request.getRecipeName()));
            return result;
        }
        catch (Exception e)
        {
            throw new BuildException("Unable to parse pulse file: " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(stream);
        }
    }

    public void terminateRecipe(long id) throws InterruptedException
    {
        // Preconditions:
        //   - this call is only made after the processor has sent the recipe
        //     commenced event
        // Responsibilities of this method:
        //   - after this call, no further command should be started
        //   - if a command is running during this call, it should be
        //     terminated
        runningLock.lock();
        try
        {
            // Check the id as it is possible for a request to come in after
            // the recipe has completed (which does no harm so long as we
            // don't terminate the next recipe!).
            if (runningRecipe == id)
            {
                terminating = true;
                if (runningRecipeInstance != null)
                {
                    runningRecipeInstance.terminate();
                }
            }
        }
        finally
        {
            runningLock.unlock();
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }

    public void setIvyProvider(IvyProvider ivyProvider)
    {
        this.ivyProvider = ivyProvider;
    }
}
