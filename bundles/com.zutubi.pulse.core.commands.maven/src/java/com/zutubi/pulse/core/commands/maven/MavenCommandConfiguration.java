package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configuration for instances of {@link MavenCommand}.
 */
@SymbolicName("zutubi.mavenCommandConfig")
@Form(fieldOrder = {"name", "workingDir", "projectFile", "targets", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class MavenCommandConfiguration extends NamedArgumentCommandConfiguration
{
    @BrowseScmFileAction(baseDirField = "workingDir")
    private String projectFile;
    private String targets;

    public MavenCommandConfiguration()
    {
        super(MavenCommand.class, "maven.bin", SystemUtils.IS_WINDOWS ? "maven.bat" : "maven");
    }

    @Override
    public void initialiseSingleCommandProject(Map<String, PostProcessorConfiguration> postProcessors)
    {
        PostProcessorConfiguration processor = postProcessors.get(getDefaultPostProcessorName(JUnitReportPostProcessorConfiguration.class));
        if (processor != null)
        {
            DirectoryArtifactConfiguration output = new DirectoryArtifactConfiguration();
            output.setName("test reports");
            output.setBase("target/test-reports");
            output.getInclusions().add("TEST-*.xml");
            output.getPostProcessors().add(processor);
            getArtifacts().put(output.getName(), output);
        }
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();
        if (StringUtils.stringSet(projectFile))
        {
            result.add(new NamedArgument("project file", projectFile, "-p"));
        }
        if (StringUtils.stringSet(targets))
        {
            result.add(new NamedArgument("targets", targets, Arrays.asList(targets.split("\\s+"))));
        }
        return result;
    }

    public String getProjectFile()
    {
        return projectFile;
    }

    public void setProjectFile(String projectFile)
    {
        this.projectFile = projectFile;
    }

    public String getTargets()
    {
        return targets;
    }

    public void setTargets(String targets)
    {
        this.targets = targets;
    }
}
