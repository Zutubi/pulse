package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2CommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2PostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

public class MavenProjectHelper extends ProjectConfigurationHelper
{
    protected static final String MAVEN2_PROCESSOR_NAME = "maven 2 output processor";

    public MavenProjectHelper(ProjectConfiguration config, ConfigurationHelper helper)
    {
        super(config, helper);
    }

    @Override
    public CommandConfiguration createDefaultCommand()
    {
        try
        {
            Maven2CommandConfiguration command = new Maven2CommandConfiguration();
            command.setPomFile("pom.xml");
            command.setGoals("clean deploy");
            command.addPostProcessor(helper.getPostProcessor(MAVEN2_PROCESSOR_NAME, Maven2PostProcessorConfiguration.class));
            return command;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
