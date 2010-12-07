package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.ant.AntPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;

public class AntProjectHelper extends ProjectConfigurationHelper
{
    protected static final String ANT_PROCESSOR_NAME = "ant output processor";

    protected AntProjectHelper(ProjectConfiguration config, ConfigurationHelper helper)
    {
        super(config, helper);
    }

    public CommandConfiguration createDefaultCommand()
    {
        try
        {
            // might be better to put this in a 'addCommand' type method?
            AntCommandConfiguration command = new AntCommandConfiguration();
            command.setBuildFile("build.xml");
            command.setName(ProjectConfigurationWizard.DEFAULT_COMMAND);
            command.addPostProcessor(helper.getPostProcessor(ANT_PROCESSOR_NAME, AntPostProcessorConfiguration.class));
            return command;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the default command target.
     *
     * @param target    the ant build target.
     */
    public void setTarget(String target)
    {
        AntCommandConfiguration command = (AntCommandConfiguration) getDefaultCommand();
        command.setTargets(target);
    }
}
