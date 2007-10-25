package com.zutubi.pulse.prototype.config.project.changeviewer;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.util.StringUtils;

/**
 * A ChangeViewer for linking to a Fisheye instance.
 */
@Form(fieldOrder = {"baseURL", "projectPath"})
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

    public String getFileViewURL(String path, String revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "browse", getProjectPath(), StringUtils.urlEncodePath(path) + "?r=" + revision);
    }

    public String getFileDownloadURL(String path, String revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "browse", "~raw,r=" + revision, getProjectPath(), StringUtils.urlEncodePath(path));
    }

    public String getFileDiffURL(String path, String revision)
    {
        ScmConfiguration scm = lookupScmConfiguration();
        String previousRevision = scm.getPreviousRevision(revision);
        if(previousRevision == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "browse", getProjectPath(), StringUtils.urlEncodePath(path) + "?r1=" + previousRevision + "&r2=" + revision);
    }

    private String getChangesetString(Revision revision)
    {
        ScmConfiguration scm = lookupScmConfiguration();
        if (scm.getType().equals("cvs"))
        {
            return String.format("%s:%s:%s", revision.getBranch(), revision.getAuthor(), CustomChangeViewerConfiguration.FISHEYE_DATE_FORMAT.format(revision.getDate()));
        }

        return revision.getRevisionString();
    }
}
