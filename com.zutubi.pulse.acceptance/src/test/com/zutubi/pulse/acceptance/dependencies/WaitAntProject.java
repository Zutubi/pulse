package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.Constants;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.scm.config.api.CheckoutScheme;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * A project configuration setup for working with the wait ant projects.
 */
public class WaitAntProject extends ProjectConfigurationHelper
{
    private File waitFile;

    public WaitAntProject(ProjectConfiguration config, File tmpDir)
    {
        super(config);
        waitFile =  new File(tmpDir, getConfig().getName());
        if (waitFile.exists() && !waitFile.delete())
        {
            throw new RuntimeException("Unable to clean up wait file '" + waitFile.getAbsolutePath() + "'");
        }
    }

    @Override
    public CommandConfiguration createDefaultCommand()
    {
        AntCommandConfiguration command = (AntCommandConfiguration) super.createDefaultCommand();
        command.setArgs(getFileArgument(waitFile));
        return command;
    }

    public ScmConfiguration createDefaultScm()
    {
        SubversionConfiguration svn = new SubversionConfiguration();
        svn.setCheckoutScheme(CheckoutScheme.CLEAN_CHECKOUT);
        svn.setMonitor(false);
        svn.setUrl(Constants.WAIT_ANT_REPOSITORY);
        return svn;
    }

    public void releaseBuild() throws IOException
    {
        FileSystemUtils.createFile(waitFile, "test");
    }

    private String getFileArgument(File waitFile)
    {
        return "-Dfile=" + waitFile.getAbsolutePath().replace("\\", "/");
    }
}
