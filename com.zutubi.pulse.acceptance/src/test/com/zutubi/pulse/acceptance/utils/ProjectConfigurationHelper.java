package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.*;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The project configuration helper is the base class for the various types
 * of projects used by the acceptance tests. It provides a suite of utility
 * methods that simplify the common project configuration tasks.
 */
public abstract class ProjectConfigurationHelper
{
    private ProjectConfiguration config;

    public ProjectConfigurationHelper(ProjectConfiguration config)
    {
        this.config = config;
    }

    public String getName()
    {
        return config.getName();
    }

    /**
     * Add a set of artifacts to this projects default recipe.
     *
     * @param paths an array of file paths relative to the project builds base directory.
     *
     * @return  the list of artifact configuration instances that can be further customised.
     */
    public List<FileArtifactConfiguration> addArtifacts(String... paths)
    {
        return getRecipe(DEFAULT_RECIPE).addArtifacts(paths);
    }

    public FileArtifactConfiguration addArtifact(String name, String path)
    {
        return getRecipe(DEFAULT_RECIPE).addArtifact(name, path);
    }

    public DirectoryArtifactConfiguration addDirArtifact(String name, String path)
    {
        return getRecipe(DEFAULT_RECIPE).addDirArtifact(name, path);
    }

    /**
     * Get the raw project configuration instance that this configuration helper is working with.
     *
     * @return the project configuration instance being updated by this configuration helper.
     */
    public ProjectConfiguration getConfig()
    {
        return config;
    }

    public ResourcePropertyConfiguration addStageProperty(BuildStageConfiguration stage, String key, String value)
    {
        ResourcePropertyConfiguration property = new ResourcePropertyConfiguration();
        property.setName(key);
        property.setValue(value);
        stage.getProperties().put(property.getName(), property);
        
        return property;
    }

    /**
     * Add a new stage with the specified name to the project.
     *
     * @param stageName the name of the new stage.
     *
     * @return  the new stage configuration instance that can be further customised.
     */
    public BuildStageConfiguration addStage(String stageName)
    {
        BuildStageConfiguration stage = new BuildStageConfiguration(stageName);
        getConfig().getStages().put(stage.getName(), stage);
        return stage;
    }

    public BuildStageConfiguration getDefaultStage()
    {
        return getStage(DEFAULT_STAGE);
    }

    public RecipeConfiguration getDefaultRecipe()
    {
        return getRecipe(DEFAULT_RECIPE).getConfig();
    }

    public CommandConfiguration getDefaultCommand()
    {
        return getDefaultRecipe().getCommands().get(DEFAULT_COMMAND);
    }

    public BuildStageConfiguration getStage(String stageName)
    {
        return getConfig().getStage(stageName);
    }

    public DependencyConfiguration addDependency(ProjectConfigurationHelper project)
    {
        return addDependency(project.getConfig());
    }

    public DependencyConfiguration addDependency(ProjectConfigurationHelper target, String stageName)
    {
        DependencyConfiguration dependency = addDependency(target);
        dependency.setStageType(DependencyConfiguration.StageType.SELECTED_STAGES);
        dependency.setStages(Arrays.asList(target.getStage(stageName)));
        return dependency;
    }
    
    public DependencyConfiguration addDependency(ProjectConfiguration project)
    {
        DependencyConfiguration dependency = new DependencyConfiguration();
        dependency.setProject(project);
        getConfig().getDependencies().getDependencies().add(dependency);
        return dependency;
    }

    public RecipeConfigurationHelper addRecipe(String recipeName)
    {
        MultiRecipeTypeConfiguration type = (MultiRecipeTypeConfiguration) config.getType();

        RecipeConfiguration recipe = new RecipeConfiguration(recipeName);
        type.addRecipe(recipe);

        CommandConfiguration command = createDefaultCommand();
        command.setName(DEFAULT_COMMAND);
        recipe.addCommand(command);

        return new RecipeConfigurationHelper(recipe);
    }

    public RecipeConfigurationHelper getRecipe(String recipeName)
    {
        MultiRecipeTypeConfiguration type = (MultiRecipeTypeConfiguration) config.getType();
        Map<String, RecipeConfiguration> recipies = type.getRecipes();
        RecipeConfiguration recipe = recipies.get(recipeName);

        return new RecipeConfigurationHelper(recipe);
    }

    public void addTrigger(TriggerConfiguration trigger)
    {
        getTriggers().put(trigger.getName(), trigger);
    }

    public void clearTriggers()
    {
        getTriggers().clear();
    }

    public <V> V getTrigger(String name)
    {
        return (V) getTriggers().get(name);
    }

    private Map<String, Object> getTriggers()
    {
        ProjectConfiguration project = getConfig();
        if (!project.getExtensions().containsKey(MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS))
        {
            project.getExtensions().put(MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS, new HashMap<String, Object>());
        }
        return (HashMap<String, Object>) project.getExtensions().get(MasterConfigurationRegistry.EXTENSION_PROJECT_TRIGGERS);
    }

    public void setOrganisation(String org)
    {
        getConfig().setOrganisation(org);
    }

    public abstract CommandConfiguration createDefaultCommand();
    public abstract List<String> getPostProcessorNames();
    public abstract List<Class> getPostProcessorTypes();
}
