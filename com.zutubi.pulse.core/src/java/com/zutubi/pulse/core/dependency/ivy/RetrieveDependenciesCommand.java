package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

/**
 * A command that handles retrieving the dependencies for a build.  This
 * should run after the scm bootstrapping, but before the build.
 */
public class RetrieveDependenciesCommand implements Command
{
    private IvySupport ivy;

    public RetrieveDependenciesCommand(IvySupport ivy)
    {
        this.ivy = ivy;
    }

    public void execute(CommandContext commandContext)
    {
        ExecutionContext context = commandContext.getExecutionContext();
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

    public void terminate()
    {
        // noop.
    }
}
