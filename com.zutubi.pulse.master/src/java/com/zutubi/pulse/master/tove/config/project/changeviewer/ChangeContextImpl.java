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

package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * A default implementation of {@link com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeContext
 */
public class ChangeContextImpl implements ChangeContext
{
    private Revision revision;
    private ProjectConfiguration projectConfiguration;
    private ScmClient scmClient;
    private ScmContext scmContext;

    public ChangeContextImpl(Revision revision, ProjectConfiguration projectConfiguration, ScmClient scmClient, ScmContext scmContext)
    {
        this.revision = revision;
        this.projectConfiguration = projectConfiguration;
        this.scmClient = scmClient;
        this.scmContext = scmContext;
    }

    public Revision getRevision()
    {
        return revision;
    }

    public ProjectConfiguration getProjectConfiguration()
    {
        return projectConfiguration;
    }

    public ScmClient getScmClient()
    {
        return scmClient;
    }

    public ScmContext getScmContext()
    {
        return scmContext;
    }

    public Revision getPreviousChangelistRevision() throws ScmException
    {
        return scmClient.getPreviousRevision(scmContext, revision, false);
    }

    public Revision getPreviousFileRevision(FileChange fileChange) throws ScmException
    {
        return scmClient.getPreviousRevision(scmContext, fileChange.getRevision(), true);
    }
}
