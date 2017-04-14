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
