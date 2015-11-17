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
