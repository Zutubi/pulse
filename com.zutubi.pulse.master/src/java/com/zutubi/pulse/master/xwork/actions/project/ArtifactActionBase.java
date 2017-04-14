/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.util.StringUtils;

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
            if (StringUtils.stringSet(artifactName))
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
