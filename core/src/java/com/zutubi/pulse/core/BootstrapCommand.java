package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;

import java.io.File;
import java.util.List;
import java.util.Arrays;

/**
 * An adaptation between the command and Bootstrap interfaces that allows
 * any bootstrapper to be run like a command.  This further allows the result
 * of bootstrapping to be stored as part of the recipe result.
 *
 */
public class BootstrapCommand implements Command
{
    private static final String OUTPUT_NAME = "bootstrap output";

    private Bootstrapper bootstrapper;

    public BootstrapCommand(Bootstrapper bootstrapper)
    {
        this.bootstrapper = bootstrapper;
    }

    public void execute(long recipeId, RecipePaths paths, File outputDir, CommandResult result)
    {
        bootstrapper.bootstrap(paths);
    }

    public List<String> getArtifactNames()
    {
        return Arrays.asList(new String[]{OUTPUT_NAME});
    }

    public String getName()
    {
        return "bootstrap";
    }

    public void setName(String name)
    {
        // Ignored
    }

    public void terminate()
    {
        // Ignored (at present we have no way to cancel scm operations).
    }
}
