package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.ant.AntPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

public class AntProjectHelper extends ProjectConfigurationHelper
{
    protected static final String ANT_PROCESSOR_NAME = "ant output processor";

    protected AntProjectHelper(ProjectConfiguration config)
    {
        super(config);
    }

    public CommandConfiguration createDefaultCommand()
    {
        // might be better to put this in a 'addCommand' type method?
        AntCommandConfiguration command = new AntCommandConfiguration();
        command.setBuildFile("build.xml");
        command.setName(ProjectConfigurationWizard.DEFAULT_COMMAND);
        command.addPostProcessor(getConfig().getPostProcessors().get(ANT_PROCESSOR_NAME));
        return command;
    }

    public List<String> getPostProcessorNames()
    {
        return Arrays.asList(ANT_PROCESSOR_NAME);
    }

    public List<Class> getPostProcessorTypes()
    {
        List<Class> types = new LinkedList<Class>();
        types.add(AntPostProcessorConfiguration.class);
        return types;
    }
}
