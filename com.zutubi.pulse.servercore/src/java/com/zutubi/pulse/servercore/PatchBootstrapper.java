package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.servercore.repository.FileRepository;

import java.io.File;
import java.io.PrintWriter;

/**
 * A bootstrapper that applies a patch to a working directory bootstrapped
 * by some other bootstrapper.
 */
public class PatchBootstrapper implements Bootstrapper, ScmFeedbackHandler
{
    private static final String DEFAULT_PATCH_BOOSTRAP_PREFIX = "personal.patch.prefix.default";

    private static final String PATCH_BOOSTRAP_PREFIX = "personal.patch.prefix";

    private Bootstrapper delegate;
    private long userId;
    private long number;
    private EOLStyle localEOL;
    private volatile boolean terminated;
    private transient PrintWriter outputWriter;

    public PatchBootstrapper(Bootstrapper delegate, long userId, long number, EOLStyle localEOL)
    {
        this.delegate = delegate;
        this.userId = userId;
        this.number = number;
        this.localEOL = localEOL;
    }

    public void bootstrap(CommandContext commandContext) throws BuildException
    {
        delegate.bootstrap(commandContext);

        ExecutionContext context = commandContext.getExecutionContext();
        FileRepository fileRepository = context.getValue(NAMESPACE_INTERNAL, PROPERTY_FILE_REPOSITORY, FileRepository.class);
        File patchFile;
        try
        {
            patchFile = fileRepository.getPatchFile(userId, number);
        }
        catch (PulseException e)
        {
            throw new BuildException("Unable to retrieve patch file '" + e.getMessage(), e);
        }

        ScmConfiguration scmConfig = context.getValue(NAMESPACE_INTERNAL, BuildProperties.PROPERTY_SCM_CONFIGURATION, ScmConfiguration.class);
        ScmClientFactory scmClientFactory = context.getValue(NAMESPACE_INTERNAL, BuildProperties.PROPERTY_SCM_CLIENT_FACTORY, ScmClientFactory.class);
        ScmClient scmClient = null;
        try
        {
            scmClient = scmClientFactory.createClient(scmConfig);
            outputWriter = new PrintWriter(commandContext.getExecutionContext().getOutputStream());
            for (Feature feature: scmClient.applyPatch(context, patchFile, getBaseBuildDir(context), localEOL, this))
            {
                commandContext.addFeature(feature);
            }
        }
        catch(PulseException e)
        {
            throw new BuildException("Unable to apply patch: " + e.getMessage(), e);
        }
        finally
        {
            outputWriter.flush();
            if (scmClient != null)
            {
                scmClient.close();
            }
        }
    }

    private File getBaseBuildDir(ExecutionContext context)
    {
        // check if we need to apply a patch prefix for this bootstrap.

        String defaultPrefix = System.getProperty(DEFAULT_PATCH_BOOSTRAP_PREFIX);

        String projectPrefix = null;
        String projectName = context.getString(NAMESPACE_INTERNAL, PROPERTY_PROJECT);
        if (projectName != null)
        {
            projectPrefix = System.getProperty(PATCH_BOOSTRAP_PREFIX + "." + projectName);
        }

        String prefix = null;
        if (projectPrefix != null && !projectPrefix.equals(""))
        {
            prefix = projectPrefix;
        }
        else if (defaultPrefix != null && !defaultPrefix.equals(""))
        {
            prefix = defaultPrefix;
        }

        if (prefix != null)
        {
            return new File(context.getWorkingDir(), prefix);
        }
        return context.getWorkingDir();
    }

    public void terminate()
    {
        delegate.terminate();
        terminated = true;
    }

    public void status(String message)
    {
        outputWriter.println(message);
    }

    public void checkCancelled() throws ScmCancelledException
    {
        if (terminated)
        {
            throw new ScmCancelledException("Operation cancelled");
        }
    }
}
