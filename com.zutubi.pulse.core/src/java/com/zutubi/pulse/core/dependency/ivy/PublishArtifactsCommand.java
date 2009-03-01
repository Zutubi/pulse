package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.Artifact;
import com.zutubi.pulse.core.Command;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.model.CommandResult;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A command that handles the publishing of build publications to the artifact repository.
 * This command should run at the end of the recipe.
 */
public class PublishArtifactsCommand implements Command
{
    private IvySupport ivy;
    private RecipeRequest request;

    public PublishArtifactsCommand(IvySupport ivy, RecipeRequest request)
    {
        this.ivy = ivy;
        this.request = request;
    }

    public void execute(ExecutionContext context, CommandResult result)
    {
        try
        {
            String stageName = request.getStageName();
            Map<String, String> extraAttributes = new HashMap<String, String>();
            extraAttributes.put("e:stage", stageName);
            ModuleRevisionId mrid = ModuleRevisionId.newInstance(request.getProjectOrg(), request.getProject(), null, extraAttributes);
            String artifactPattern = context.getString(NAMESPACE_INTERNAL, PROPERTY_PUBLICATION_PATTERN);

            File baseDir = context.getValue(NAMESPACE_INTERNAL, PROPERTY_RECIPE_PATHS, RecipePaths.class).getBaseDir();

            ivy.publish(mrid, request.getBuildNumber(), stageName, baseDir.getAbsolutePath() + "/" + artifactPattern);
        }
        catch (Exception e)
        {
            throw new BuildException("Error publishing artifacts: " + e.getMessage(), e);
        }
    }

    public List<Artifact> getArtifacts()
    {
        // return a list of artifacts actually published.
        return Collections.emptyList();
    }

    public String getName()
    {
        return "publish";
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

