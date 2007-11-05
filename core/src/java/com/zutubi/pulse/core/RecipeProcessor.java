package com.zutubi.pulse.core;

import com.opensymphony.util.TextUtils;
import static com.zutubi.pulse.core.BuildProperties.*;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.TestSuitePersister;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.RecipeCommencedEvent;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.events.build.RecipeStatusEvent;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The recipe processor, as the name suggests, is responsible for running recipies.
 *
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
        
        TestSuiteResult testResults = new TestSuiteResult();

        runningRecipe = recipeResult.getId();
        eventManager.publish(new RecipeCommencedEvent(this, recipeResult.getId(), recipeResult.getRecipeName(), recipeResult.getStartTime()));

        ExecutionContext context = request.getContext();
        long recipeStartTime = recipeResult.getStartTime();
        ResourceRepository resourceRepository = context.getInternalValue(PROPERTY_RESOURCE_REPOSITORY, ResourceRepository.class);
        pushRecipeContext(context, resourceRepository, testResults, recipeStartTime, request);
        try
        {
            // Wrap bootstrapper in a command and run it.
            BootstrapCommand bootstrapCommand = new BootstrapCommand(request.getBootstrapper());

            // Now we can load the recipe from the pulse file
            PulseFile pulseFile = loadPulseFile(request, context, resourceRepository);

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

            recipe.addFirstCommand(bootstrapCommand);

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

            RecipePaths paths = context.getInternalValue(PROPERTY_RECIPE_PATHS, RecipePaths.class);
            writeTestResults(paths, testResults);
            recipeResult.setTestSummary(testResults.getSummary());
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Test results stored."));

            compressResults(paths, context.getInternalBoolean(PROPERTY_COMPRESS_ARTIFACTS, false), context.getInternalBoolean(PROPERTY_COMPRESS_WORKING_DIR, false));

            recipeResult.complete();
            RecipeCompletedEvent completedEvent = new RecipeCompletedEvent(this, recipeResult);
            if(context.getVersion() != null)
            {
                completedEvent.setBuildVersion(context.getVersion());
            }

            eventManager.publish(completedEvent);

            context.popInternalScope();
            context.popScope();

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

    private void writeTestResults(RecipePaths paths, TestSuiteResult testResults)
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

    private void pushRecipeContext(ExecutionContext context, ResourceRepository resourceRepository, TestSuiteResult testResults, long recipeStartTime, RecipeRequest request)
    {
        context.pushInternalScope();
        context.addInternalString(PROPERTY_BASE_DIR, context.getWorkingDir().getAbsolutePath());
        context.addInternalString(PROPERTY_RECIPE_TIMESTAMP, BuildProperties.TIMESTAMP_FORMAT.format(new Date(recipeStartTime)));
        context.addInternalString(PROPERTY_RECIPE_TIMESTAMP_MILLIS, Long.toString(recipeStartTime));
        context.addInternalValue(PROPERTY_TEST_RESULTS, testResults);

        context.pushScope();
        importResources(resourceRepository, request.getResourceRequirements(), context);
        if(context.getInternalString(PROPERTY_RECIPE) == null)
        {
            context.addString(PROPERTY_RECIPE, "[default]");
        }
    }

    private PulseFile loadPulseFile(RecipeRequest request, ExecutionContext context, ResourceRepository resourceRepository) throws BuildException
    {
        Scope globalScope = new Scope(context.asScope());
        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> var: env.entrySet())
        {
            globalScope.addEnvironmentProperty(var.getKey(), var.getValue());
        }

        // import the required resources into the pulse files scope.
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

    private void importResources(ResourceRepository resourceRepository, List<ResourceRequirement> resourceRequirements, ExecutionContext context)
    {
        if (resourceRequirements != null)
        {
            for(ResourceRequirement requirement: resourceRequirements)
            {
                Resource resource = resourceRepository.getResource(requirement.getResource());
                if(resource == null)
                {
                    throw new BuildException("Unable to import required resource '" + requirement.getResource() + "'");
                }

                for(ResourceProperty property: resource.getProperties().values())
                {
                    context.add(property);
                }

                String importVersion = requirement.getVersion();
                if(importVersion == null)
                {
                    importVersion = resource.getDefaultVersion();
                }

                if(TextUtils.stringSet(importVersion))
                {
                    ResourceVersion version = resource.getVersion(importVersion);
                    if(version == null)
                    {
                        throw new BuildException("Reference to non-existant version '" + importVersion + "' of resource '" + requirement.getResource() + "'");
                    }

                    for(ResourceProperty property: version.getProperties().values())
                    {
                        context.add(property);
                    }
                }
            }
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

    public long getBuildingRecipe()
    {
        return runningRecipe;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
