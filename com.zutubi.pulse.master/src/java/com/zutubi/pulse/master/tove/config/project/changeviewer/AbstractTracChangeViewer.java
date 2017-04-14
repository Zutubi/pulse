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
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;

/**
 * Base class shared by implementations for different trac versions.
 */
@SymbolicName("zutubi.abstractTracChangeViewerConfig")
public abstract class AbstractTracChangeViewer extends BasePathChangeViewer
{
    public AbstractTracChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public String getRevisionURL(ProjectConfiguration projectConfiguration, Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "changeset", revision.getRevisionString());
    }

    public String getFileViewURL(ChangeContext context, FileChange fileChange)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "browser", WebUtils.uriPathEncode(fileChange.getPath()) + "?rev=" + fileChange.getRevision());
    }

    public String getFileDownloadURL(ChangeContext context, FileChange fileChange)
    {
        return getFileViewURL(context, fileChange) + "&format=raw";
    }
}
