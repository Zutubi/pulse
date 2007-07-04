package com.zutubi.pulse.prototype.config.project.changeviewer;

import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.util.StringUtils;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;

/**
 * A ChangeViewer for linking to a Fisheye instance.
 */
@Form(fieldOrder = {"name", "baseUrl", "projectPath"})
@SymbolicName("zutubi.fisheyeChangeViewerConfig")
public class FisheyeConfiguration extends BasePathChangeViewer
{
    public FisheyeConfiguration()
    {
        super(null, null);
    }

    public FisheyeConfiguration(String baseURL, String projectPath)
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
        return StringUtils.join("/", true, true, getBaseURL(), "browse", getProjectPath(), StringUtils.urlEncodePath(path) + "?r=" + revision.getRevisionString());
    }

    public String getFileDownloadURL(String path, FileRevision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "browse", "~raw,r=" + revision.getRevisionString(), getProjectPath(), StringUtils.urlEncodePath(path));
    }

    public String getFileDiffURL(String path, FileRevision revision)
    {
        FileRevision previousRevision = revision.getPrevious();
        if(previousRevision == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "browse", getProjectPath(), StringUtils.urlEncodePath(path) + "?r1=" + previousRevision.getRevisionString() + "&r2=" + revision.getRevisionString());
    }

    private String getChangesetString(Revision revision)
    {
        if(revision instanceof CvsRevision)
        {
            CvsRevision cvs = (CvsRevision) revision;
            return String.format("%s:%s:%s", cvs.getBranch(), cvs.getAuthor(), CustomChangeViewerConfiguration.FISHEYE_DATE_FORMAT.format(revision.getDate()));
        }

        return revision.getRevisionString();
    }
}
