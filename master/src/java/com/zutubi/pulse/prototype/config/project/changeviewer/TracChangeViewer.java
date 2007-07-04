package com.zutubi.pulse.prototype.config.project.changeviewer;

import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.util.StringUtils;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A change viwer implementation for linking to a Trac instance.
 */
@Form(fieldOrder = {"name", "baseURL", "projectPath"})
@SymbolicName("zutubi.tracChangeViewerConfig")
public class TracChangeViewer extends BasePathChangeViewer
{
    public TracChangeViewer()
    {
        super(null, null);
    }

    public TracChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public String getDetails()
    {
        return "Trac [" + getBaseURL() + "]";
    }

    public String getChangesetURL(Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "changeset", revision.getRevisionString());
    }

    public String getFileViewURL(String path, FileRevision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "browser", StringUtils.urlEncodePath(path) + "?rev=" + revision.getRevisionString());
    }

    public String getFileDownloadURL(String path, FileRevision revision)
    {
        return getFileViewURL(path, revision) + "&format=raw";
    }

    public String getFileDiffURL(String path, FileRevision revision)
    {
        FileRevision previous = revision.getPrevious();
        if(previous == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "changeset?new=" + getDiffPath(path, revision) + "&old=" + getDiffPath(path, previous));
    }

    private String getDiffPath(String path, FileRevision revision)
    {
        String result = StringUtils.join("/", path + "@" + revision.getRevisionString());
        if(result.startsWith("/"))
        {
            result = result.substring(1);
        }
        
        try
        {
            return URLEncoder.encode(result, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // Programmer error!
            return result;
        }
    }
}
