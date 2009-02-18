package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.api.DirectoryOutputConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.SystemUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.File;

/**
 * Configuration for instances of {@link MavenCommand}.
 */
@SymbolicName("zutubi.mavenCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "targets", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class MavenCommandConfiguration extends NamedArgumentCommandConfiguration
{
    private String targets;

    public MavenCommandConfiguration()
    {
        super(MavenCommand.class, "maven.bin", SystemUtils.IS_WINDOWS ? "maven.bat" : "maven");
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();
        if (TextUtils.stringSet(targets))
        {
            result.add(new NamedArgument("targets", targets, Arrays.asList(targets.split("\\s+"))));
        }
        return result;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }

    public static void configure(MavenCommandConfiguration commandConfiguration, Map<String, PostProcessorConfiguration> postProcessors)
    {
        PostProcessorConfiguration processor = CollectionUtils.find(postProcessors.values(), new Predicate<PostProcessorConfiguration>()
        {
            public boolean satisfied(PostProcessorConfiguration postProcessorConfiguration)
            {
                return postProcessorConfiguration instanceof JUnitReportPostProcessorConfiguration;
            }
        });

        if (processor != null)
        {
            DirectoryOutputConfiguration output = new DirectoryOutputConfiguration();
            output.setName("test reports");
            output.setBase(new File("target/test-reports"));
            output.getInclusions().add("TEST-*.xml");
            output.getPostProcessors().add(processor);
            commandConfiguration.getOutputs().put(output.getName(), output);
        }
    }
}
