package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.StringUtils;

/**
 * Helper base class for actions that may drill down to the file artifact
 * level.
 */
public class FileArtifactActionBase extends ArtifactActionBase
{
    private String path;
    private StoredFileArtifact fileArtifact;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public StoredFileArtifact getFileArtifact()
    {
        StoredArtifact artifact = getArtifact();
        if (fileArtifact == null && artifact != null)
        {
            if (StringUtils.stringSet(path))
            {
                fileArtifact = artifact.findFileBase(path);
                if(fileArtifact == null)
                {
                    throw new LookupErrorException("Unknown file [" + path + "] for artifact [" + getArtifactName() + "] of command [" + getCommandName() + "] of stage [" + getRecipeResultNode().getStageName() + "] of build [" + getBuildResult().getNumber() + "] of project [" + getProjectName() + "]");
                }
            }
        }

        return fileArtifact;
    }

    public StoredFileArtifact getRequiredFileArtifact()
    {
        StoredFileArtifact artifact = getFileArtifact();
        if(artifact == null)
        {
            throw new LookupErrorException("Artifact path is required");
        }

        return artifact;
    }
}
