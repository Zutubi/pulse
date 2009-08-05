package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;

/**
 * A change viwer implementation for linking to a Trac instance.
 */
@Form(fieldOrder = {"baseURL", "projectPath"})
@SymbolicName("zutubi.tracChangeViewerConfig")
public class Trac10ChangeViewer extends AbstractTracChangeViewer
{
    public Trac10ChangeViewer()
    {
        super(null, null);
    }

    public Trac10ChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public String getFileDiffURL(ChangeContext context, FileChange fileChange) throws ScmException
    {
        Revision previous = context.getPreviousFileRevision(fileChange);
        if(previous == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "changeset?new=" + getDiffPath(fileChange.getPath(), fileChange.getRevision()) + "&old=" + getDiffPath(fileChange.getPath(), previous));
    }

    private String getDiffPath(String path, Revision revision)
    {
        String result = StringUtils.join("/", path + "@" + revision);
        if(result.startsWith("/"))
        {
            result = result.substring(1);
        }
        
        return WebUtils.formUrlEncode(result);
    }
}
