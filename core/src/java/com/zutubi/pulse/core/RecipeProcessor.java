package com.zutubi.pulse.core;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.model.Property;
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

    public void build(BuildContext context, RecipeRequest request, RecipePaths paths, ResourceRepository resourceRepository, boolean capture)
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

        CommandOutputStream outputStream = null;
        Scope globalScope = new Scope();
        try
        {
            long recipeStartTime = recipeResult.getStartTime();

            RecipeContext recipeContext = createRecipeContext(paths, testResults, globalScope, recipeStartTime, context, request.getId());
            if(capture)
            {
                outputStream = new CommandOutputStream(eventManager, runningRecipe, true);
                recipeContext.setOutputStream(outputStream);
            }

            // Wrap bootstrapper in a command and run it.
            BootstrapCommand bootstrapCommand = new BootstrapCommand(request.getBootstrapper());

            // Now we can load the recipe from the pulse file
            PulseFile pulseFile = loadPulseFile(request, paths.getBaseDir(), resourceRepository, globalScope, context, recipeStartTime);

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

            recipe.execute(recipeContext);
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
            IOUtils.close(outputStream);
            
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Storing test results..."));
            writeTestResults(paths, testResults);
            recipeResult.setTestSummary(testResults.getSummary());
            eventManager.publish(new RecipeStatusEvent(this, runningRecipe, "Test results stored."));

            compressResults(paths, request.getCompressArtifacts(), request.getCompressWorkingCopy());

            recipeResult.complete();
            RecipeCompletedEvent completedEvent = new RecipeCompletedEvent(this, recipeResult);
            if(context != null)
            {
                completedEvent.setBuildVersion(context.getBuildVersion());
            }
            
            eventManager.publish(completedEvent);

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

    private RecipeContext createRecipeContext(RecipePaths paths, TestSuiteResult testResults, Scope globalScope, long recipeStartTime, BuildContext buildContext, long recipeId)
    {
        RecipeContext context = new RecipeContext();
        context.setTestResults(testResults);
        context.setRecipePaths(paths);
        context.setGlobalScope(globalScope);
        context.setRecipeStartTime(recipeStartTime);
        context.setRecipeId(recipeId);
        if (buildContext != null && buildContext.getBuildNumber() != -1)
        {
            context.setBuildContext(buildContext);
        }
        return context;
    }

    private PulseFile loadPulseFile(RecipeRequest request, File baseDir, ResourceRepository resourceRepository, Scope globalScope, BuildContext buildContext, long recipeStartTime) throws BuildException
    {
        globalScope.add(new Property("base.dir", baseDir.getAbsolutePath()));
        globalScope.add(new Property("recipe.timestamp", BuildContext.PULSE_BUILD_TIMESTAMP_FORMAT.format(new Date(recipeStartTime))));
        globalScope.add(new Property("recipe.timestamp.millis", Long.toString(recipeStartTime)));
        if(buildContext != null)
        {
            for(Map.Entry<String, String> property: buildContext.getProperties().entrySet())
            {
                globalScope.add(new Property(property.getKey(), property.getValue()));
            }

            globalScope.add(new Property("build.number", Long.toString(buildContext.getBuildNumber())));
        }

        Map<String, String> env = System.getenv();
        for(Map.Entry<String, String> var: env.entrySet())
        {
            globalScope.addEnvironmentProperty(var.getKey(), var.getValue());
        }

        if(request.getProperties() != null)
        {
            globalScope.add(request.getProperties());
        }

        // import the required resources into the pulse files scope.
        importResources(resourceRepository, request.getResourceRequirements(), globalScope);

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

    private void importResources(ResourceRepository resourceRepository, List<ResourceRequirement> resourceRequirements, Scope scope)
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

                scope.add(resource.getProperties().values());

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

                    scope.add(version.getProperties().values());
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
