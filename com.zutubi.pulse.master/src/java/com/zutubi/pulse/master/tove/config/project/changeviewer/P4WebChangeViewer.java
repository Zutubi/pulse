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
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;

/**
 * A ChangeViewer for linking to a P4Web instance.
 */
@Form(fieldOrder = {"baseURL", "projectPath"})
@SymbolicName("zutubi.p4WebChangeViewerConfig")
public class P4WebChangeViewer extends BasePathChangeViewer
{
    public P4WebChangeViewer()
    {
        super(null, null);
    }

    public P4WebChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public String getRevisionURL(ProjectConfiguration projectConfiguration, Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "@md=d@", revision.getRevisionString() + "?ac=10");
    }

    public String getFileViewURL(ChangeContext context, FileChange fileChange)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "@md=d@" + pathPart(fileChange) + "?ac=64&rev1=" + fileChange.getRevision());
    }

    public String getFileDownloadURL(ChangeContext context, FileChange fileChange)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "@md=d&rev1=" + fileChange.getRevision() + "@" + pathPart(fileChange));
    }

    public String getFileDiffURL(ChangeContext context, FileChange fileChange) throws ScmException
    {
        Revision previous = context.getPreviousFileRevision(fileChange);
        if(previous == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "@md=d@" + pathPart(fileChange) + "?ac=19&rev1=" + previous + "&rev2=" + fileChange.getRevision());
    }

    private String pathPart(FileChange fileChange)
    {
        return WebUtils.uriPathEncode(fileChange.getPath());
    }
}
