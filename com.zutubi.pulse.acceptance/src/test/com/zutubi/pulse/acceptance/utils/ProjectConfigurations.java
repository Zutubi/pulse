package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.scm.git.config.GitConfiguration;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;

import java.io.File;

import static com.zutubi.pulse.master.rest.wizards.ProjectConfigurationWizard.*;

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
    public AntProjectHelper createFailAntProject(String projectName) throws Exception
    {
        return createAntProject(projectName, Constants.FAIL_ANT_REPOSITORY);
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
        DepAntProject project = new DepAntProject(new ProjectConfiguration(projectName), configurationHelper);
        configureBaseProject(project, addDefaultStage);
        configureSvnScm(project, Constants.DEP_ANT_REPOSITORY);
        return project;
    }


    public AntProjectHelper createIvyAntProject(String projectName) throws Exception
    {
        return createAntProject(projectName, Constants.IVY_ANT_REPOSITORY);
    }

    public MavenProjectHelper createDepMavenProject(String projectName) throws Exception
    {
        MavenProjectHelper project = new MavenProjectHelper(new ProjectConfiguration(projectName), configurationHelper);
        configureBaseProject(project, true);
        configureSvnScm(project, Constants.DEP_MAVEN_REPOSITORY);
        return project;
    }

    /**
     * Create a project configuration that allows for the execution of the build to be synchronised
     * with the acceptance tests.  When executing, the build will wait until explicitly 'released'
     *
     * @param projectName   the name of the project.
     * @param dir           a temporary directory that will be used to communicate with the build
     *                      process
     * @param cleanup       if true, the build will clean up the signal that caused it to release
     *                      before it completes, allowing subsequent builds to wait again
     * @return the project configuration helper instance to allow further configuration
     * of this project.
     * 
     * @throws Exception thrown on error.
     */
    public WaitProject createWaitAntProject(String projectName, File dir, boolean cleanup) throws Exception
    {
        WaitProject project = new WaitProject(new ProjectConfiguration(projectName), configurationHelper, dir, cleanup);
        configureBaseProject(project, true);
        configureSvnScm(project, Constants.WAIT_ANT_REPOSITORY);
        return project;
    }

    /**
     * Create a project configuration that is configured with the test ant project, a project that
     * generates test results.
     *
     * @param projectName   the name of the project
     * @return the project configuration helper instance to allow further configuration
     * of this project.
     *
     * @throws Exception thrown on error.
     */
    public AntProjectHelper createTestAntProject(String projectName) throws Exception
    {
        AntProjectHelper project = createAntProject(projectName, Constants.TEST_ANT_REPOSITORY);
        FileArtifactConfiguration artifact = project.addArtifact("junit report", "build/reports/xml/TESTS-TestSuites.xml");
        artifact.addPostProcessor(configurationHelper.getPostProcessor("junit xml report processor", JUnitReportPostProcessorConfiguration.class));

        return project;
    }

    /**
     * Creates a new ant project using the trivial repository (a simple build.xml file).
     * 
     * @param projectName name of the project to create
     * @return the new project
     * @throws Exception on error
     */
    public TriviAntProject createTrivialAntProject(String projectName) throws Exception
    {
        TriviAntProject project = new TriviAntProject(new ProjectConfiguration(projectName), configurationHelper);
        configureBaseProject(project, true);
        configureSvnScm(project, Constants.TRIVIAL_ANT_REPOSITORY);
        return project;
    }

    public AntProjectHelper createAntProject(String projectName, String svnUrl) throws Exception
    {
        AntProjectHelper project = new AntProjectHelper(new ProjectConfiguration(projectName), configurationHelper);
        configureBaseProject(project, true);
        configureSvnScm(project, svnUrl);
        return project;
    }

    /**
     * Create a project configuration that is configured to use an ant project in a git repository.
     *
     * @param projectName   the name of the project
     * @return the project configuration helper instance to allow further configuration
     * of this project.
     * @throws Exception thrown on error.
     */
    public AntProjectHelper createGitAntProject(String projectName) throws Exception
    {
        AntProjectHelper project = new AntProjectHelper(new ProjectConfiguration(projectName), configurationHelper);
        configureBaseProject(project, true);
        configureGitScm(project, Constants.getGitUrl());
        return project;
    }

    private void configureBaseProject(ProjectConfigurationHelper helper, boolean addDefaultStage) throws Exception
    {
        AgentConfiguration master = configurationHelper.getMasterAgentReference();

        // setup the defaults:
        if (addDefaultStage)
        {
            BuildStageConfiguration stage = helper.addStage(DEFAULT_STAGE);
            stage.setAgent(master);
        }

        MultiRecipeTypeConfiguration type = new MultiRecipeTypeConfiguration();
        type.setDefaultRecipe(DEFAULT_RECIPE);
        helper.getConfig().setType(type);

        helper.addRecipe(DEFAULT_RECIPE);

        DependentBuildTriggerConfiguration trigger = new DependentBuildTriggerConfiguration();
        trigger.setName(DEPENDENCY_TRIGGER);
        helper.addTrigger(trigger);
    }

    private void configureGitScm(ProjectConfigurationHelper helper, String repository)
    {
        GitConfiguration git = new GitConfiguration();
        git.setMonitor(false);
        git.setRepository(repository);

        helper.getConfig().setScm(git);
    }

    private void configureSvnScm(ProjectConfigurationHelper helper, String url)
    {
        SubversionConfiguration svn = new SubversionConfiguration();
        svn.setMonitor(false);
        svn.setUrl(url);

        helper.getConfig().setScm(svn);
    }
}
