package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.MavenUtils;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.core.NamedArgumentCommand;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Support for running Maven 1 - adds automatic version capturing.
 */
public class MavenCommand extends NamedArgumentCommand
{
    private static final String DEFAULT_PROJECT_FILE = "project.xml";

    public MavenCommand(MavenCommandConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected List<Class<? extends PostProcessorConfiguration>> getDefaultPostProcessorTypes()
    {
        return Arrays.<Class<? extends PostProcessorConfiguration>>asList(MavenPostProcessorConfiguration.class);
    }

    @Override
    public void execute(CommandContext commandContext)
    {
        super.execute(commandContext);

        try
        {
            //TODO: use the context's variables to transfer this maven specific information around.
            PulseExecutionContext pec = (PulseExecutionContext) commandContext.getExecutionContext();
            String projectFile = getProjectFile();
            String version = MavenUtils.extractVersion(new File(getWorkingDir(pec.getWorkingDir()), projectFile), "currentVersion");
            if (version != null)
            {
                commandContext.addCustomField(FieldScope.BUILD, "maven.version", version);
            }
        }
        catch (PulseException e)
        {
            commandContext.addFeature(new Feature(Feature.Level.WARNING, e.getMessage()));
        }
    }

    private String getProjectFile()
    {
        MavenCommandConfiguration config = (MavenCommandConfiguration) getConfig();
        String projectFile = config.getProjectFile();
        if (!StringUtils.stringSet(projectFile))
        {
            projectFile = DEFAULT_PROJECT_FILE;
        }
        return projectFile;
    }
}
