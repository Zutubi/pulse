package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Helper methods for building configuration examples.
 */
public class ExamplesBuilder
{
    public static ConfigurationExample buildProject(CommandConfiguration command)
    {
        return buildProjectForCaptureProcessor(command, null);
    }

    public static ConfigurationExample buildProjectForCaptureProcessor(CommandConfiguration command, PostProcessorConfiguration postProcessor)
    {
        return new ConfigurationExample("project", recipes(command, postProcessor));
    }

    public static ConfigurationExample buildProjectForCommandOutputProcessor(PostProcessorConfiguration postProcessor)
    {
        ExecutableCommandConfiguration exe = new ExecutableCommandConfiguration();
        exe.setName("build");
        exe.setExe("bash");
        exe.setArgs("build-it.sh");
        exe.addPostProcessor(postProcessor);

        return new ConfigurationExample("project", recipes(exe, postProcessor));
    }

    private static ProjectRecipesConfiguration recipes(CommandConfiguration command, PostProcessorConfiguration postProcessor)
    {
        RecipeConfiguration recipe = new RecipeConfiguration("default");
        recipe.addCommand(command);

        ProjectRecipesConfiguration recipes = new ProjectRecipesConfiguration();
        if (postProcessor != null)
        {
            recipes.addPostProcessor(postProcessor);
        }
        recipes.addRecipe(recipe);
        return recipes;
    }
}
