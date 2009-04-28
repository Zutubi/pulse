package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.PulseExecutionContext;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

/**
 * A command that handles retrieving the dependencies for a build.  This
 * should run after the scm bootstrapping, but before the build.
 */
public class RetrieveDependenciesCommand extends BaseIvyCommand
{
    private IvyClient ivy;

    public RetrieveDependenciesCommand(RetrieveDependenciesCommandConfiguration config)
    {
        this.ivy = config.getIvy();
    }

    public void execute(CommandContext commandContext)
    {
        try
        {
            ExecutionContext context = commandContext.getExecutionContext();

            updateIvyCredentials((PulseExecutionContext)context);

            ModuleDescriptor descriptor = context.getValue(PROPERTY_DEPENDENCY_DESCRIPTOR, ModuleDescriptor.class);
            ivy.resolve(descriptor);

            String retrievalPattern = context.getString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN);
            retrievalPattern = context.resolveReferences(retrievalPattern);
            
            ivy.retrieve(descriptor.getModuleRevisionId(), context.getWorkingDir().getAbsolutePath() + "/" + retrievalPattern);
        }
        catch (Exception e)
        {
            throw new BuildException("Error running dependency retrieval: " + e.getMessage(), e);
        }
    }
}
