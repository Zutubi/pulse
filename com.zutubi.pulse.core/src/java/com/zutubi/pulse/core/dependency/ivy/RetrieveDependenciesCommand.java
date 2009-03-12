package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.util.url.CredentialsStore;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * A command that handles retrieving the dependencies for a build.  This
 * should run after the scm bootstrapping, but before the build.
 */
public class RetrieveDependenciesCommand implements Command
{
    private IvySupport ivy;

    public RetrieveDependenciesCommand(RetrieveDependenciesCommandConfiguration config)
    {
        this.ivy = config.getIvy();
    }

    public void execute(CommandContext commandContext)
    {
        ExecutionContext context = commandContext.getExecutionContext();

        updateIvyCredentials(context);
        
        try
        {
            ModuleDescriptor descriptor = context.getValue(PROPERTY_DEPENDENCY_DESCRIPTOR, ModuleDescriptor.class);
            if (descriptor != null)
            {
                ivy.resolve(descriptor);

                String retrievalPattern = context.getString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN);
                ivy.retrieve(descriptor.getModuleRevisionId(), context.getWorkingDir().getAbsolutePath() + "/" + retrievalPattern);
            }
        }
        catch (Exception e)
        {
            throw new BuildException("Error running dependency retrieval: " + e.getMessage(), e);
        }
    }

    private void updateIvyCredentials(ExecutionContext context)
    {
        try
        {
            URL masterUrl = new URL(context.getString(NAMESPACE_INTERNAL, PROPERTY_MASTER_URL));
            String host = masterUrl.getHost();

            CredentialsStore.INSTANCE.addCredentials("Pulse", host, "pulse", context.getString(NAMESPACE_INTERNAL, PROPERTY_HASH));
        }
        catch (MalformedURLException e)
        {
            // noop.
        }
    }

    public void terminate()
    {
        // noop.
    }
}
