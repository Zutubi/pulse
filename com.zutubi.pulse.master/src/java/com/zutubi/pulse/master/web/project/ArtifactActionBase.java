package com.zutubi.pulse.master.web.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.master.web.LookupErrorException;
import com.zutubi.util.TextUtils;

/**
 * Helper base class for actions that may drill down to the artifact level.
 */
public class ArtifactActionBase extends CommandActionBase
{
    private String artifactName;
    private StoredArtifact artifact;

    public String getArtifactName()
    {
        return artifactName;
    }

    public void setArtifactName(String artifactName)
    {
        this.artifactName = artifactName;
    }

    public String getu_artifactName()
    {
        return uriComponentEncode(artifactName);
    }

    public String geth_artifactName()
    {
        return htmlEncode(artifactName);
    }

    public StoredArtifact getArtifact()
    {
        CommandResult commandResult = getCommandResult();
        if (artifact == null && commandResult != null)
        {
            if (TextUtils.stringSet(artifactName))
            {
                artifact = commandResult.getArtifact(artifactName);
                if(artifact == null)
                {
                    throw new LookupErrorException("Unknown artifact [" + artifactName + "] for command [" + commandResult.getCommandName() + "] of stage [" + getRecipeResultNode().getStageName() + "] of build [" + getBuildResult().getNumber() + "] of project [" + getProject().getName() + "]");
                }
            }
        }

        return artifact;
    }

    public StoredArtifact getRequiredArtifact()
    {
        StoredArtifact artifact = getArtifact();
        if(artifact == null)
        {
            throw new LookupErrorException("Artifact name is required");
        }

        return artifact;
    }
}
