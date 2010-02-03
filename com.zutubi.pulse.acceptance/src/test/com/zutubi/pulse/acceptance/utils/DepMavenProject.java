package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2CommandConfiguration;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;

public class DepMavenProject extends ProjectConfigurationHelper
{
    public DepMavenProject(ProjectConfiguration config)
    {
        super(config);
    }

    @Override
    public CommandConfiguration createDefaultCommand()
    {
        // might be better to put this in a 'addCommand' type method?
        Maven2CommandConfiguration command = new Maven2CommandConfiguration();
        command.setPomFile("pom.xml");
        command.setGoals("clean deploy");
        command.setName(ProjectConfigurationWizard.DEFAULT_COMMAND);
        command.addPostProcessor(getConfig().getPostProcessors().get(MAVEN2_PROCESSOR_NAME));

        return command;
    }

    public ScmConfiguration createDefaultScm()
    {
        SubversionConfiguration svn = new SubversionConfiguration();
        svn.setCheckoutScheme(CheckoutScheme.CLEAN_CHECKOUT);
        svn.setMonitor(false);
        svn.setUrl(Constants.DEP_MAVEN_REPOSITORY);
        return svn;
    }
}
