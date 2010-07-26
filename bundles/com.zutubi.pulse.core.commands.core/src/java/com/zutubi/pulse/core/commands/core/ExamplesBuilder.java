package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.api.FileSystemArtifactConfigurationSupport;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.ReferenceCollectingProjectRecipesConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Helper methods for building configuration examples.
 */
public class ExamplesBuilder
{
    /**
     * Builds an example with a single recipe that runs the given command.
     *
     * @param command example command to add to the recipe
     * @return a single-recipe, single-command example
     */
    public static ConfigurationExample buildProject(CommandConfiguration command)
    {
        return new ConfigurationExample("project", recipes(command, null));
    }

    /**
     * Builds an example that shows an output file/directory being captured and
     * processed by some processor.  Used to illustrate a capture processor by
     * showing the type of files that it would apply to and how it is applied.
     * <p/>
     * The example includes a single recipe with a single executable command,
     * which will capture the output.
     *
     * @param commandExe executable for the created command -- should be a
     *                   typical executable for the type of project that the
     *                   capture processor would be used in (e.g. make for a
     *                   C++ test report processor)
     * @param output     configured output capture, which should have the
     *                   post-processor being illustrated as its first (and
     *                   usually only) post-processor
     * @return an example illustrating a post-processor on capture files
     */
    public static ConfigurationExample buildProjectForCaptureProcessor(String commandExe, FileSystemArtifactConfigurationSupport output)
    {
        ExecutableCommandConfiguration command = new ExecutableCommandConfiguration();
        command.setName("build");
        command.setExe(commandExe);
        command.addArtifact(output);

        return new ConfigurationExample("project", recipes(command, output.getPostProcessors().get(0)));
    }

    /**
     * Builds an example to illustrate a post-processor typically used on
     * command output.  The example includes a single recipe with a single
     * generic command to which the processor is applied.
     *
     * @param postProcessor the post-processor being illustrated
     * @return an example showing the processor applied to command output
     */
    public static ConfigurationExample buildProjectForCommandOutputProcessor(PostProcessorConfiguration postProcessor)
    {
        ExecutableCommandConfiguration exe = new ExecutableCommandConfiguration();
        exe.setName("build");
        exe.setExe("bash");
        exe.setArgs("build-it.sh");
        exe.addPostProcessor(postProcessor);

        return new ConfigurationExample("project", recipes(exe, postProcessor));
    }

    private static ReferenceCollectingProjectRecipesConfiguration recipes(CommandConfiguration command, PostProcessorConfiguration postProcessor)
    {
        RecipeConfiguration recipe = new RecipeConfiguration("default");
        recipe.addCommand(command);

        ReferenceCollectingProjectRecipesConfiguration recipes = new ReferenceCollectingProjectRecipesConfiguration();
        if (postProcessor != null)
        {
            recipes.addPostProcessor(postProcessor);
        }
        recipes.addRecipe(recipe);
        return recipes;
    }
}
