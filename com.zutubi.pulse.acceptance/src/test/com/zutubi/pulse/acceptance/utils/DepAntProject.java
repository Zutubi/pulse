package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.StringUtils;

/**
 * A project configuration setup for working with dep ant projects.
 */
public class DepAntProject extends AntProjectHelper
{
    // specific to the dep ant project.
    public static final String PROPERTY_CREATE_LIST = "create.list";
    public static final String PROPERTY_EXPECTED_LIST = "expected.list";
    public static final String PROPERTY_NOT_EXPECTED_LIST = "not.expected.list";

    public DepAntProject(ProjectConfiguration config, ConfigurationHelper helper)
    {
        super(config, helper);
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
        command.setArgs("-Dcreate.list=\"$(" + PROPERTY_CREATE_LIST + ")\" -Dpresent.list=\"$(" + PROPERTY_EXPECTED_LIST + ")\" -Dnot.present.list=\"$(" + PROPERTY_NOT_EXPECTED_LIST + ")\"");
        return command;
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
     * Add a list of file paths that should be created by the execution of the specified stage.
     *
     * @param stage the stage for which the file should be created.
     * @param paths the paths to be created.
     */
    public void addFilesToCreateInStage(String stage, String... paths)
    {
        addStageProperty(getConfig().getStage(stage), PROPERTY_CREATE_LIST, StringUtils.join(",", paths));
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
     * Add a list of file paths that this build does not expect to be present at execution.
     *
     * @param paths the array of paths (relative to the builds base directory)
     */
    public void addNotExpectedFiles(String paths)
    {
        addStagePathsProperty(PROPERTY_NOT_EXPECTED_LIST, paths);
    }

    private void addStagePathsProperty(String propertyName, String... paths)
    {
        for (BuildStageConfiguration stage : getConfig().getStages().values())
        {
            addStageProperty(stage, propertyName, StringUtils.join(",", paths));
        }
    }
}
