package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A change viwer implementation for linking to a Trac instance.
 */
@Form(fieldOrder = {"baseURL", "projectPath"})
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

    public String getChangelistURL(Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "changeset", revision.getRevisionString());
    }

    public String getFileViewURL(String path, Revision changelistRevision, String fileRevision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "browser", StringUtils.urlEncodePath(path) + "?rev=" + fileRevision);
    }

    public String getFileDownloadURL(String path, Revision changelistRevision, String fileRevision)
    {
        return getFileViewURL(path, changelistRevision, fileRevision) + "&format=raw";
    }

    public String getFileDiffURL(String path, Revision changelistRevision, String fileRevision)
    {
        ScmConfiguration scm = lookupScmConfiguration();
        String previous = scm.getPreviousRevision(fileRevision);
        if(previous == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "changeset?new=" + getDiffPath(path, fileRevision) + "&old=" + getDiffPath(path, previous));
    }

    private String getDiffPath(String path, String revision)
    {
        String result = StringUtils.join("/", path + "@" + revision);
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
