package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_PUBLICATION_PATTERN;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_DEPENDENCY_DESCRIPTOR;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A command that handles the publishing of build publications to the artifact repository.
 * This command should run at the end of the recipe.
 */
public class PublishArtifactsCommand extends BaseIvyCommand
{
    private IvyClient ivy;
    private RecipeRequest request;

    public PublishArtifactsCommand(PublishArtifactsCommandConfiguration config)
    {
        this.ivy = config.getIvy();
        this.request = config.getRequest();
    }

    public void execute(CommandContext commandContext)
    {
        try
        {
            ExecutionContext context = commandContext.getExecutionContext();

            updateIvyCredentials((PulseExecutionContext)context);

            ModuleDescriptor descriptor = context.getValue(PROPERTY_DEPENDENCY_DESCRIPTOR, ModuleDescriptor.class);
            ivy.resolve(descriptor);

            Map<String, String> extraAttributes = new HashMap<String, String>();
            extraAttributes.put("e:stage", IvyUtils.ivyEncodeStageName(request.getStageName()));
            ModuleRevisionId mrid = IvyModuleRevisionId.newInstance(request.getProjectOrg(), request.getProject(), null, extraAttributes);
            String artifactPattern = context.getString(NAMESPACE_INTERNAL, PROPERTY_PUBLICATION_PATTERN);
            artifactPattern = context.resolveReferences(artifactPattern);

            File baseDir = context.getWorkingDir();

            String configName = IvyUtils.ivyEncodeStageName(request.getStageName());
            ivy.publish(mrid, request.getBuildNumber(), configName, baseDir.getAbsolutePath() + "/" + artifactPattern);
        }
        catch (Exception e)
        {
            throw new BuildException("Error publishing artifacts: " + e.getMessage(), e);
        }
    }
}

