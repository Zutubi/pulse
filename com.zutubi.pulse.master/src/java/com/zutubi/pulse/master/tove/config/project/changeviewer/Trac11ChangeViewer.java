package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A change viwer implementation for linking to a Trac instance, version 0.11
 * onwards (the diff URL format changed in this version).
 */
@Form(fieldOrder = {"baseURL", "projectPath"})
@SymbolicName("zutubi.trac11ChangeViewerConfig")
public class Trac11ChangeViewer extends AbstractTracChangeViewer
{
    public Trac11ChangeViewer()
    {
        super(null, null);
    }

    public Trac11ChangeViewer(String baseURL, String projectPath)
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

        return StringUtils.join("/", true, true, getBaseURL(), "changeset?new_path=" + getDiffPath(fileChange.getPath()) + "&new=" + fileChange.getRevision() + "&old_path=" + getDiffPath(fileChange.getPath()) + "&old=" + previous);
    }

    private String getDiffPath(String path)
    {
        if(path.startsWith("/"))
        {
            path = path.substring(1);
        }

        try
        {
            return URLEncoder.encode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // Programmer error!
            return path;
        }
    }
}