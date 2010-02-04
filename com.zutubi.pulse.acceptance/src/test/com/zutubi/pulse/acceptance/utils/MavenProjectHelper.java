package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2CommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2PostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MavenProjectHelper extends ProjectConfigurationHelper
{
    protected static final String MAVEN2_PROCESSOR_NAME = "maven 2 output processor";

    public MavenProjectHelper(ProjectConfiguration config)
    {
        super(config);
    }

    @Override
    public CommandConfiguration createDefaultCommand()
    {
        Maven2CommandConfiguration command = new Maven2CommandConfiguration();
        command.setPomFile("pom.xml");
        command.setGoals("clean deploy");
        command.addPostProcessor(getConfig().getPostProcessors().get(MAVEN2_PROCESSOR_NAME));
        return command;
    }

    public List<String> getPostProcessorNames()
    {
        return Arrays.asList(MAVEN2_PROCESSOR_NAME);
    }

    public List<Class> getPostProcessorTypes()
    {
        List<Class> types = new LinkedList<Class>();
        types.add(Maven2PostProcessorConfiguration.class);
        return types;
    }
}
