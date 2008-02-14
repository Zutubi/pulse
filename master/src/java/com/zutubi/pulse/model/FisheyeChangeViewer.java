package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.util.StringUtils;

/**
 * A ChangeViewer for linking to a Fisheye instance.
 */
public class FisheyeChangeViewer extends BasePathChangeViewer
{
    private FisheyeChangeViewer()
    {
        super(null, null);
    }

    public FisheyeChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public String getDetails()
    {
        return "Fisheye [" + getBaseURL() + "]";
    }

    public String getChangesetURL(Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "changelog", getProjectPath(), "?cs=" + getChangesetString(revision));
    }

    public String getFileViewURL(String path, FileRevision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "browse", getProjectPath(), StringUtils.urlEncodePath(stripDepot(path)) + "?r=" + revision.getRevisionString());
    }

    public String getFileDownloadURL(String path, FileRevision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "browse", "~raw,r=" + revision.getRevisionString(), getProjectPath(), StringUtils.urlEncodePath(stripDepot(path)));
    }

    public String getFileDiffURL(String path, FileRevision revision)
    {
        FileRevision previousRevision = revision.getPrevious();
        if(previousRevision == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "browse", getProjectPath(), StringUtils.urlEncodePath(stripDepot(path)) + "?r1=" + previousRevision.getRevisionString() + "&r2=" + revision.getRevisionString());
    }

    public ChangeViewer copy()
    {
        return new FisheyeChangeViewer(getBaseURL(), getProjectPath());
    }

    private String getChangesetString(Revision revision)
    {
        if(revision instanceof CvsRevision)
        {
            CvsRevision cvs = (CvsRevision) revision;
            return String.format("%s:%s:%s", cvs.getBranch(), cvs.getAuthor(), CustomChangeViewer.FISHEYE_DATE_FORMAT.format(revision.getDate()));
        }

        return revision.getRevisionString();
    }

    private String stripDepot(String path)
    {
        // This is a workaround for CIB-1260: we detect Perforce depot paths
        // and string the depot name.  It is the best I can do without a
        // schema change.
        String stripPrefix = System.getProperty("pulse.fisheye.strip.prefix");
        if(stripPrefix != null && path.startsWith(stripPrefix))
        {
            return path.substring(stripPrefix.length());
        }
        else
        {
            if(path.startsWith("//"))
            {
                int index = path.indexOf("/", 2);
                if(index >= 0)
                {
                    return path.substring(index + 1);
                }
            }
        }

        return path;
    }
}
