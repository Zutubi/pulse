package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.tove.type.record.PathUtils;

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

    public DirectoryArtifactConfiguration addDirArtifact(String name, String dir)
    {
        DirectoryArtifactConfiguration artifact = new DirectoryArtifactConfiguration();
        artifact.setName(name);
        artifact.setBase(dir);
        artifact.setPublish(true);

        Map<String, CommandConfiguration> commands = config.getCommands();
        CommandConfigurationSupport buildCommand = (CommandConfigurationSupport) commands.get(COMMAND_BUILD);
        buildCommand.addArtifact(artifact);

        return artifact;
    }

    public FileArtifactConfiguration addArtifact(String name, String path)
    {
        FileArtifactConfiguration artifact = new FileArtifactConfiguration();
        artifact.setName(name);
        artifact.setFile(path);
        artifact.setPublish(true);

        Map<String, CommandConfiguration> commands = config.getCommands();
        CommandConfigurationSupport buildCommand = (CommandConfigurationSupport) commands.get(COMMAND_BUILD);
        buildCommand.addArtifact(artifact);

        return artifact;
    }

    public List<FileArtifactConfiguration> addArtifacts(String... paths)
    {
        List<FileArtifactConfiguration> artifacts = new LinkedList<FileArtifactConfiguration>();

        for (String path : paths)
        {
            String filename = PathUtils.getBaseName(path);

            Matcher m = artifactFilePattern.matcher(filename);
            if (!m.matches())
            {
                throw new RuntimeException();
            }

            String name = m.group(1);
            artifacts.add(addArtifact(name, path));
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
