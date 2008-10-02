package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.util.StringUtils;

/**
 * A change viewer for linking to ViewVC.
 */
@Form(fieldOrder = {"baseURL", "projectPath"})
@SymbolicName("zutubi.viewVCChangeViewerConfig")
public class ViewVCChangeViewer extends BasePathChangeViewer
{
    private static final String CVS_TYPE = "cvs";

    public ViewVCChangeViewer()
    {
        super(null, null);
    }

    public ViewVCChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public boolean hasCapability(Capability capability)
    {
        ScmConfiguration scm = lookupScmConfiguration();
        if(capability.equals(Capability.VIEW_CHANGESET) && scm.getType().equals(CVS_TYPE))
        {
            return false;
        }
        
        return super.hasCapability(capability);
    }

    public String getDetails()
    {
        return "ViewVC [" + getBaseURL() + "]";
    }

    public String getChangesetURL(Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath() + "?rev=" + revision.getRevisionString() + "&view=rev");
    }

    public String getFileViewURL(String path, String revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath(), StringUtils.urlEncodePath(path) + "?rev=" + revision + "&view=markup");
    }

    public String getFileDownloadURL(String path, String revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "*checkout*", getProjectPath(), StringUtils.urlEncodePath(path) + "?rev=" + revision);
    }

    public String getFileDiffURL(String path, String revision)
    {
        ScmConfiguration config = lookupScmConfiguration();
        String previous = config.getPreviousRevision(revision);
        if(previous == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath(), StringUtils.urlEncodePath(path) + "?r1=" + previous + "&r2=" + revision);
    }
}
