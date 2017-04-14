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

import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.SecurityUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;

/**
 * Change viewer for Perforce (Helix) Swarm.
 */
@Form(fieldOrder = {"baseURL", "projectPath"})
@SymbolicName("zutubi.perforceSwarmChangeViewerConfig")
public class PerforceSwarmChangeViewer extends BasePathChangeViewer
{
    public PerforceSwarmChangeViewer()
    {
        super(null, null);
    }

    public PerforceSwarmChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    @Override
    public String getRevisionURL(ProjectConfiguration projectConfiguration, Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "changes", revision.getRevisionString());
    }

    @Override
    public String getFileViewURL(ChangeContext context, FileChange fileChange) throws ScmException
    {
        return StringUtils.join("/", true, true, getBaseURL(), "files", pathPart(fileChange) + "?v=" + fileChange.getRevision());
    }

    @Override
    public String getFileDownloadURL(ChangeContext context, FileChange fileChange) throws ScmException
    {
        return StringUtils.join("/", true, true, getBaseURL(), "downloads", pathPart(fileChange) + "?v=" + fileChange.getRevision());
    }

    @Override
    public String getFileDiffURL(ChangeContext context, FileChange fileChange) throws ScmException
    {
        return StringUtils.join("/", true, true, getBaseURL(), "changes", context.getRevision().getRevisionString() + "#" + SecurityUtils.md5Digest(fileChange.getPath()));
    }

    private String pathPart(FileChange fileChange)
    {
        return WebUtils.uriPathEncode(fileChange.getPath());
    }
}
