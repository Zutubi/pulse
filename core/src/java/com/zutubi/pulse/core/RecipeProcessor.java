/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.CommandCommencedEvent;
import com.zutubi.pulse.events.build.CommandCompletedEvent;
import com.zutubi.pulse.events.build.RecipeCommencedEvent;
import com.zutubi.pulse.events.build.RecipeCompletedEvent;
import com.zutubi.pulse.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class RecipeProcessor
{
    private static final Logger LOG = Logger.getLogger(RecipeProcessor.class);

    private EventManager eventManager;
    private ResourceRepository resourceRepository;
    private Lock runningLock = new ReentrantLock();
    private long runningRecipe = 0;
    private Command runningCommand = null;
    private boolean terminating = false;
    private FileLoader fileLoader;

    public RecipeProcessor()
    {
    }

    public void init()
    {
        if (fileLoader == null)
        {
            fileLoader = new PulseFileLoader(new ObjectFactory(), resourceRepository);
        }
    }

    public static String getCommandDirName(int i, CommandResult result)
    {
        // Use the command name because:
        // a) we do not have an id for the command model
        // b) for local builds, this is a lot friendlier for the developer
        return String.format("%08d-%s", i, result.getCommandName());
    }

    public void build(long recipeId, RecipePaths paths, Bootstrapper bootstrapper, String pulseFileSource, String recipeName)
    {
        // This result holds only the recipe details (stamps, state etc), not
        // the command results.  A full recipe result with command results is
        // assembled elsewhere.
        RecipeResult result = new RecipeResult(recipeName);
        result.setId(recipeId);
        result.commence(paths.getOutputDir());

        runningRecipe = recipeId;
        eventManager.publish(new RecipeCommencedEvent(this, recipeId, recipeName, result.getStamps().getStartTime()));

        try
        {
            // Wrap bootstrapper in a command and run it.
            BootstrapCommand bootstrapCommand = new BootstrapCommand(bootstrapper);
            CommandResult bootstrapResult = new CommandResult(bootstrapCommand.getName());
            File commandOutput = new File(paths.getOutputDir(), getCommandDirName(0, bootstrapResult));

            executeCommand(recipeId, bootstrapResult, paths, commandOutput, bootstrapCommand);

            if (bootstrapResult.succeeded())
            {
                // Now we can load the recipe from the pulse file
                PulseFile pulseFile = loadPulseFile(paths.getBaseDir(), pulseFileSource, recipeName);
                Recipe recipe;

                if (recipeName == null)
                {
                    recipeName = pulseFile.getDefaultRecipe();
                    if (recipeName == null)
                    {
                        throw new BuildException("Please specify a default recipe for your project.");
                    }
                }

                recipe = pulseFile.getRecipe(recipeName);
                if (recipe == null)
                {
                    throw new BuildException("Undefined recipe '" + recipeName + "'");
                }

                build(recipeId, recipe, paths);
            }
        }
        catch (BuildException e)
        {
            result.error(e);
        }
        catch (Exception e)
        {
            LOG.severe(e);
            result.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            result.complete();
            eventManager.publish(new RecipeCompletedEvent(this, result));

            runningLock.lock();
            runningRecipe = 0;
            if (terminating)
            {
                terminating = false;
            }
            runningLock.unlock();
        }
    }

    public void build(long recipeId, Recipe recipe, RecipePaths paths) throws BuildException
    {
        // TODO: support continuing build when errors occur. Take care: exceptions.
        int i = 1;
        for (Command command : recipe.getCommands())
        {
            CommandResult result = new CommandResult(command.getName());

            File commandOutput = new File(paths.getOutputDir(), getCommandDirName(i, result));

            runningLock.lock();
            if (terminating)
            {
                runningLock.unlock();
                return;
            }

            runningCommand = command;
            runningLock.unlock();

            executeCommand(recipeId, result, paths, commandOutput, command);

            switch (result.getState())
            {
                case FAILURE:
                case ERROR:
                    return;
            }
            i++;
        }
    }

    private void executeCommand(long recipeId, CommandResult result, RecipePaths paths, File commandOutput, Command command)
    {
        result.commence(commandOutput);
        eventManager.publish(new CommandCommencedEvent(this, recipeId, result.getCommandName(), result.getStamps().getStartTime()));

        try
        {
            if (!commandOutput.mkdirs())
            {
                throw new BuildException("Could not create command output directory '" + commandOutput.getAbsolutePath() + "'");
            }

            command.execute(recipeId, paths, commandOutput, result);
        }
        catch (BuildException e)
        {
            result.error(e);
        }
        catch (Exception e)
        {
            result.error(new BuildException("Unexpected error: " + e.getMessage(), e));
        }
        finally
        {
            result.complete();
            eventManager.publish(new CommandCompletedEvent(this, recipeId, result));
        }
    }

    private PulseFile loadPulseFile(File baseDir, String pulseFileSource, String recipeName) throws BuildException
    {
        List<Reference> properties = new LinkedList<Reference>();
        Property property = new Property("base.dir", baseDir.getAbsolutePath());
        properties.add(property);

        InputStream stream = null;

        try
        {
            // CIB-286: special case empty file for better reporting
            if(pulseFileSource.trim().length() == 0)
            {
                throw new ParseException("File is empty");
            }

            stream = new ByteArrayInputStream(pulseFileSource.getBytes());
            PulseFile result = new PulseFile();
            fileLoader.load(stream, result, properties, new RecipeLoadPredicate(result, recipeName));
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

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }

    /**
     * The event manager is a required reference.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public FileLoader getFileLoader()
    {
        return fileLoader;
    }

    public void setFileLoader(FileLoader fileLoader)
    {
        this.fileLoader = fileLoader;
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
                if (runningCommand != null)
                {
                    runningCommand.terminate();
                }
            }
        }
        finally
        {
            runningLock.unlock();
        }
    }
}
