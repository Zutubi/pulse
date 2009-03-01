package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.Artifact;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_DEPENDENCY_DESCRIPTOR;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_RETRIEVAL_PATTERN;

import java.util.List;
import java.util.Collections;

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

    public void execute(ExecutionContext context, CommandResult result)
    {
        try
        {
            ModuleDescriptor descriptor = context.getValue(PROPERTY_DEPENDENCY_DESCRIPTOR, ModuleDescriptor.class);
            ivy.resolve(descriptor);

            String retrievalPattern = context.getString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN);
            ivy.retrieve(descriptor.getModuleRevisionId(), context.getWorkingDir().getAbsolutePath() + "/" + retrievalPattern);
        }
        catch (Exception e)
        {
            throw new BuildException("Error running dependency retrieval: " + e.getMessage(), e);
        }
    }

    public List<Artifact> getArtifacts()
    {
        // return the list of artifacts actually retrieved.
        return Collections.emptyList();
    }

    public String getName()
    {
        return "retrieve";
    }

    public void setName(String name)
    {
        // noop.
    }

    public boolean isForce()
    {
        return false;
    }

    public void terminate()
    {
        // noop.
    }
}
