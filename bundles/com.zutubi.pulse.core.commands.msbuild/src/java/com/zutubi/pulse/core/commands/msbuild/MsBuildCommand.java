package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.ProcessArtifact;
import com.zutubi.pulse.core.commands.core.ExecutableCommand;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * A command which runs the MsBuild build tool.
 */
public class MsBuildCommand extends ExecutableCommand
{
    private static final String EXECUTABLE_PROPERTY = "msbuild.bin";
    private static final String DEFAULT_EXECUTABLE = "msbuild";

    private static final String FLAG_TARGET   = "/target:";
    private static final String FLAG_PROPERTY = "/property:";

    private static final String PROPERTY_SEPARATOR     = "=";
    private static final String PROPERTY_CONFIGURATION = "Configuration";

    private String buildFile;
    private String targets;
    private String configuration;
    private List<BuildProperty> buildProperties = new LinkedList<BuildProperty>();
    private boolean postProcess = true;

    public MsBuildCommand()
    {
        super(EXECUTABLE_PROPERTY, DEFAULT_EXECUTABLE);
    }

    public void execute(ExecutionContext context, CommandResult commandResult)
    {
        // Add the build file first so it is easy to distinguish in the full
        // command line.
        if (TextUtils.stringSet(buildFile))
        {
            addArguments(buildFile);
            commandResult.getProperties().put("build file", buildFile);
        }

        if (TextUtils.stringSet(targets))
        {
            for (String target: targets.split(" +"))
            {
                addArguments(FLAG_TARGET + target);
            }

            commandResult.getProperties().put("targets", targets);
        }

        // We support the configuration property explicitly as it is so common.
        if (TextUtils.stringSet(configuration))
        {
            addArgumentForBuildProperty(PROPERTY_CONFIGURATION, configuration);
            commandResult.getProperties().put("configuration", configuration);
        }

        for (BuildProperty property: buildProperties)
        {
            addArgumentForBuildProperty(property.getName(), property.getValue());
        }

        if (postProcess)
        {
            ProcessArtifact pa = createProcess();
            pa.setProcessor(new MsBuildPostProcessor("msbuild.pp"));
        }

        super.execute(context, commandResult);
    }

    private void addArgumentForBuildProperty(String name, String value)
    {
        addArguments(FLAG_PROPERTY + name + PROPERTY_SEPARATOR + value);
    }

    public String getBuildFile()
    {
        return buildFile;
    }

    public void setBuildFile(String buildFile)
    {
        this.buildFile = buildFile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    public String getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(String configuration)
    {
        this.configuration = configuration;
    }

    public List<BuildProperty> getBuildProperties()
    {
        return buildProperties;
    }

    public BuildProperty createBuildProperty()
    {
        BuildProperty buildProperty = new BuildProperty();
        buildProperties.add(buildProperty);
        return buildProperty;
    }

    public boolean isPostProcess()
    {
        return postProcess;
    }

    public void setPostProcess(boolean postProcess)
    {
        this.postProcess = postProcess;
    }

    public static class BuildProperty
    {
        @Required
        private String value;
        @Required
        private String name;

        public BuildProperty()
        {
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }
}
