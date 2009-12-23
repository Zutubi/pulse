package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.core.commands.ant.AntPostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;

import java.io.File;

/**
 * The project configurations instance provides the acceptance tests with a set of
 * 'pre-defined' project configurations that provide a convenient starting point for
 * configuring projects.
 *
 * All of the common project configurations will be represented here. 
 */
public class ProjectConfigurations
{
    private ConfigurationHelper configurationHelper;

    public ProjectConfigurations(ConfigurationHelper configurationHelper)
    {
        this.configurationHelper = configurationHelper;
    }

    /**
     * Create a project configuration that will fail when run.
     *
     * @param projectName   the name of the project.
     * @return the project configuration helper instance to allow further configuration
     * of this project.
     *
     * @throws Exception thrown on error.
     */
    public FailAntProject createFailAntProject(String projectName) throws Exception
    {
        FailAntProject project = new FailAntProject(new ProjectConfiguration(projectName));
        configureBaseProject(project, true);
        return project;
    }

    /**
     * Create a project configuration that allows for the assertion of the existence and
     * non-existence of files in the build directories during execution, as well as the
     * creation of files as part of the execution of the build.
     *
     * @param projectName     the name of the project.
     * @return the project configuration helper instance to allow further configuration
     * of this project.
     *
     * @throws Exception thrown on error.
     */
    public DepAntProject createDepAntProject(String projectName) throws Exception
    {
        return createDepAntProject(projectName, true);
    }

    /**
     * Create a project configuration that allows for the assertion of the existence and
     * non-existance of files in the build directories during execution, as well as the
     * creation of files as part of the execution of the build.
     *
     * @param projectName     the name of the project.
     * @param addDefaultStage if true, add a default build stage to the project
     * @return the project configuration helper instance to allow further configuration
     * of this project.
     *
     * @throws Exception thrown on error.
     */
    public DepAntProject createDepAntProject(String projectName, boolean addDefaultStage) throws Exception
    {
        DepAntProject project = new DepAntProject(new ProjectConfiguration(projectName));
        configureBaseProject(project, addDefaultStage);
        return project;
    }

    /**
     * Create a project configuration that allows for the execution of the build to be synchronised
     * with the acceptance tests.  When executing, the build will wait until explicitly 'released'
     *
     * @param dir           a temporary directory that will be used to communicate with the build process.
     * @param projectName   the name of the project.
     * @return the project configuration helper instance to allow further configuration
     * of this project.
     * 
     * @throws Exception thrown on error.
     */
    public WaitAntProject createWaitAntProject(File dir, String projectName) throws Exception
    {
        WaitAntProject project = new WaitAntProject(new ProjectConfiguration(projectName), dir);
        configureBaseProject(project, true);
        return project;
    }

    private void configureBaseProject(ProjectConfigurationHelper helper, boolean addDefaultStage) throws Exception
    {
        AgentConfiguration master = configurationHelper.getMasterAgentReference();

        // setup the Ant post processor so that the commands have something to reference.
        AntPostProcessorConfiguration postProcessorReference = configurationHelper.getConfigurationReference("projects/global project template/postProcessors/ant output processor", AntPostProcessorConfiguration.class);
        helper.getConfig().getPostProcessors().put(postProcessorReference.getName(), postProcessorReference);

        // setup the defaults:
        if (addDefaultStage)
        {
            BuildStageConfiguration stage = helper.addStage(ProjectConfigurationWizard.DEFAULT_STAGE);
            stage.setAgent(master);
        }

        MultiRecipeTypeConfiguration type = new MultiRecipeTypeConfiguration();
        type.setDefaultRecipe(ProjectConfigurationWizard.DEFAULT_RECIPE);
        helper.getConfig().setType(type);

        helper.addRecipe(ProjectConfigurationWizard.DEFAULT_RECIPE);

        DependentBuildTriggerConfiguration trigger = new DependentBuildTriggerConfiguration();
        trigger.setName(ProjectConfigurationWizard.DEPENDENCY_TRIGGER);
        helper.addTrigger(trigger);

        helper.getConfig().setScm(helper.createDefaultScm());
    }
}
