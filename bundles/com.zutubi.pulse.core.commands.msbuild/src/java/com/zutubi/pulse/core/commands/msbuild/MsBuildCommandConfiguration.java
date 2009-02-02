package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.annotations.Required;

import java.util.*;

/**
 * A command which runs the MsBuild build tool.
 */
@SymbolicName("zutubi.msbuildCommandConfig")
public class MsBuildCommandConfiguration extends NamedArgumentCommandConfiguration
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
    @Addable("build-property")
    private Map<String, BuildPropertyConfiguration> buildProperties = new LinkedHashMap<String, BuildPropertyConfiguration>();

    public MsBuildCommandConfiguration()
    {
        super(NamedArgumentCommand.class, EXECUTABLE_PROPERTY, DEFAULT_EXECUTABLE);
        getPostProcessors().add(new MsBuildPostProcessorConfiguration("msbuild.pp"));
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();

        // Add the build file first so it is easy to distinguish in the full
        // command line.
        if (TextUtils.stringSet(buildFile))
        {
            result.add(new NamedArgument("build file", buildFile));
        }

        if (TextUtils.stringSet(targets))
        {
            List<String> flaggedTargets = new LinkedList<String>();
            for (String target: targets.split("\\s+"))
            {
                flaggedTargets.add(FLAG_TARGET + target);
            }

            result.add(new NamedArgument("targets", targets, flaggedTargets));
        }

        // We support the configuration property explicitly as it is so common.
        if (TextUtils.stringSet(configuration))
        {
            result.add(new NamedArgument("configuration", configuration, Arrays.asList(FLAG_PROPERTY + PROPERTY_CONFIGURATION + PROPERTY_SEPARATOR + configuration)));
        }

        return result;
    }

    @Override
    public List<String> getCombinedArguments()
    {
        List<String> result = new LinkedList<String>(super.getCombinedArguments());

        for (BuildPropertyConfiguration property: buildProperties.values())
        {
            result.add(FLAG_PROPERTY + property.getName() + PROPERTY_SEPARATOR + property.getValue());
        }

        return result;
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

    public Map<String, BuildPropertyConfiguration> getBuildProperties()
    {
        return buildProperties;
    }

    public void setBuildProperties(Map<String, BuildPropertyConfiguration> buildProperties)
    {
        this.buildProperties = buildProperties;
    }

    @SymbolicName("zutubi.msbuildCommandConfig.buildPropertyConfig")
    public static class BuildPropertyConfiguration extends AbstractNamedConfiguration
    {
        @Required
        private String value;

        public BuildPropertyConfiguration()
        {
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
