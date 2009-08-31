package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The recipe configuration helper is part of the XYZConfigurationHelper set of
 * support classes designed to make it easier to configure a recipe for testing.
 */
public class RecipeConfigurationHelper
{
    public static final Pattern artifactFilePattern = Pattern.compile("(.+)\\.(.+)");

    public static final String COMMAND_BUILD = "build"; // refers to the build command created elsewhere..
    
    private RecipeConfiguration config;

    public RecipeConfigurationHelper(RecipeConfiguration recipe)
    {
        this.config = recipe;
    }

    public RecipeConfiguration getConfig()
    {
        return config;
    }

    public List<FileArtifactConfiguration> addArtifacts(String... filenames)
    {
        List<FileArtifactConfiguration> artifacts = new LinkedList<FileArtifactConfiguration>();

        for (String filename : filenames)
        {
            Matcher m = artifactFilePattern.matcher(filename);
            if (!m.matches())
            {
                throw new RuntimeException();
            }

            String name = m.group(1);
            String extension = m.group(2);

            FileArtifactConfiguration artifact = new FileArtifactConfiguration();
            artifact.setName(name);
            artifact.setFile("build/" + name + "." + extension); // refers to where the artifacts are created - see DepAntProject.addFilesToCreate usages.
            artifact.setPublish(true);
            
            Map<String, CommandConfiguration> commands = config.getCommands();
            CommandConfigurationSupport buildCommand = (CommandConfigurationSupport) commands.get(COMMAND_BUILD);
            buildCommand.addArtifact(artifact);

            artifacts.add(artifact);
        }
        return artifacts;
    }

    /**
     * Add a new command to this recipe.
     *
     * @param command   the new command.
     */
    public void addCommand(CommandConfiguration command)
    {
        this.config.addCommand(command);
    }
}
