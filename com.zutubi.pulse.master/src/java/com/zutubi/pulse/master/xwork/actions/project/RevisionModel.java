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

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Defines JSON structure for a revision.
 */
public class RevisionModel
{
    private String revisionString;
    private String link;

    public RevisionModel(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public RevisionModel(Revision revision, ProjectConfiguration projectConfiguration)
    {
        this.revisionString = revision == null ? null : revision.getRevisionString();
        if (revision != null && projectConfiguration != null && projectConfiguration.getChangeViewer() != null)
        {
            this.link = projectConfiguration.getChangeViewer().getRevisionURL(projectConfiguration, revision);
        }
    }

    public String getRevisionString()
    {
        return revisionString;
    }

    public void setRevisionString(String revisionString)
    {
        this.revisionString = revisionString;
    }

    public String getLink()
    {
        return link;
    }
}
