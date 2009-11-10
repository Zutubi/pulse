package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.util.StringUtils;

/**
 * A project configuration setup for working with dep ant projects.
 */
public class DepAntProject extends ProjectConfigurationHelper
{
    // specific to the dep ant project.
    public static final String PROPERTY_CREATE_LIST = "create.list";
    public static final String PROPERTY_EXPECTED_LIST = "expected.list";
    public static final String PROPERTY_NOT_EXPECTED_LIST = "not.expected.list";

    public DepAntProject(ProjectConfiguration config)
    {
        super(config);
    }

    public BuildStageConfiguration addStage(String stageName)
    {
        BuildStageConfiguration stage = super.addStage(stageName);

        // specific to the dep ant project
        addStageProperty(stage, PROPERTY_CREATE_LIST, "");
        addStageProperty(stage, PROPERTY_EXPECTED_LIST, "");
        addStageProperty(stage, PROPERTY_NOT_EXPECTED_LIST, "");

        return stage;
    }

    @Override
    public CommandConfiguration createDefaultCommand()
    {
        AntCommandConfiguration command = (AntCommandConfiguration) super.createDefaultCommand();
        command.setTargets("present not.present create");
        command.setArgs("-Dcreate.list=\"${" + PROPERTY_CREATE_LIST + "}\" -Dpresent.list=\"${" + PROPERTY_EXPECTED_LIST + "}\" -Dnot.present.list=\"${" + PROPERTY_NOT_EXPECTED_LIST + "}\"");
        return command;
    }

    public ScmConfiguration createDefaultScm()
    {
        SubversionConfiguration svn = new SubversionConfiguration();
        svn.setCheckoutScheme(CheckoutScheme.CLEAN_CHECKOUT);
        svn.setMonitor(false);
        svn.setUrl(Constants.DEP_ANT_REPOSITORY);
        return svn;
    }


    /**
     * Add a list of file paths that should be created by the execution of this build.
     *
     * @param paths the array of paths (relative to the builds base directory)
     */
    public void addFilesToCreate(String... paths)
    {
        addStagePathsProperty(PROPERTY_CREATE_LIST, paths);
    }

    /**
     * Reset the list of file paths that should be created by the execution of this build to an empty string.
     */
    public void clearFilesToCreate()
    {
        clearStageProperty(PROPERTY_CREATE_LIST);
    }

    /**
     * Add a list of files paths that this build expects to be present at execution.
     *
     * @param paths the array of paths (relative to the builds base directory)
     */
    public void addExpectedFiles(String... paths)
    {
        addStagePathsProperty(PROPERTY_EXPECTED_LIST, paths);
    }

    /**
     * Reset the list of expected paths to an empty string.
     */
    public void clearExpectedFiles()
    {
        clearStageProperty(PROPERTY_EXPECTED_LIST);
    }

    /**
     * Add a list of file paths that this build does not expect to be present at execution.
     *
     * @param paths the array of paths (relative to the builds base directory)
     */
    public void addNotExpectedFiles(String paths)
    {
        addStagePathsProperty(PROPERTY_NOT_EXPECTED_LIST, paths);
    }

    /**
     * Reset the list of not expected paths to an empty string.
     */
    public void clearNotExpectedFiles()
    {
        clearStageProperty(PROPERTY_NOT_EXPECTED_LIST);
    }

    private void clearStageProperty(String propertyName)
    {
        for (BuildStageConfiguration stage : getConfig().getStages().values())
        {
            stage.getProperties().remove(propertyName);
            addStageProperty(stage, propertyName, "");
        }
    }

    private void addStagePathsProperty(String propertyName, String... paths)
    {
        for (BuildStageConfiguration stage : getConfig().getStages().values())
        {
            addStageProperty(stage, propertyName, StringUtils.join(",", paths));
        }
    }

}
