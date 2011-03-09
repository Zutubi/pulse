package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.core.JUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommandConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configuration for instances of {@link Maven3Command}.
 */
@SymbolicName("zutubi.maven3CommandConfig")
@Form(fieldOrder = {"name", "workingDir", "pomFile", "settingsFile", "goals", "args", "extraArguments", "postProcessors", "exe", "inputFile", "outputFile", "force"})
public class Maven3CommandConfiguration extends NamedArgumentCommandConfiguration
{
    private String pomFile;
    private String settingsFile;
    private String goals;

    public Maven3CommandConfiguration()
    {
        super(Maven3Command.class, "maven3.bin", SystemUtils.IS_WINDOWS ? "mvn.bat" : "mvn");
    }

    @Override
    public void initialiseSingleCommandProject(Map<String, PostProcessorConfiguration> availableProcessors)
    {
        PostProcessorConfiguration processor = availableProcessors.get(getDefaultPostProcessorName(JUnitReportPostProcessorConfiguration.class));
        if (processor != null)
        {
            DirectoryArtifactConfiguration output = new DirectoryArtifactConfiguration();
            output.setName("test reports");
            output.setBase("target/surefire-reports");
            output.getInclusions().add("TEST-*.xml");
            output.getPostProcessors().add(processor);
            getArtifacts().put(output.getName(), output);
        }
    }

    protected List<NamedArgument> getNamedArguments()
    {
        List<NamedArgument> result = new LinkedList<NamedArgument>();
        if (StringUtils.stringSet(pomFile))
        {
            result.add(new NamedArgument("POM file", pomFile, "-f"));
        }

        if (StringUtils.stringSet(settingsFile))
        {
            result.add(new NamedArgument("settings file", settingsFile, "-s"));
        }

        if (StringUtils.stringSet(goals))
        {
            result.add(new NamedArgument("goals", goals, Arrays.asList(goals.trim().split("\\s+"))));
        }

        return result;
    }

    public String getPomFile()
    {
        return pomFile;
    }

    public void setPomFile(String pomFile)
    {
        this.pomFile = pomFile;
    }

    public String getSettingsFile()
    {
        return settingsFile;
    }

    public void setSettingsFile(String settingsFile)
    {
        this.settingsFile = settingsFile;
    }

    public String getGoals()
    {
        return goals;
    }

    public void setGoals(String goals)
    {
        this.goals = goals;
    }
}
