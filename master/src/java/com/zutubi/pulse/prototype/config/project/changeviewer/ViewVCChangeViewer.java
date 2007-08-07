package com.zutubi.pulse.prototype.config.project.changeviewer;

import com.zutubi.pulse.core.model.CvsRevision;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.servercore.scm.config.ScmConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;

/**
 * A change viewer for linking to ViewVC.
 */
@Form(fieldOrder = {"name", "baseURL", "projectPath"})
@SymbolicName("zutubi.viewVCChangeViewerConfig")
public class ViewVCChangeViewer extends BasePathChangeViewer
{
    public ViewVCChangeViewer()
    {
        super(null, null);
    }

    public ViewVCChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public boolean hasCapability(ScmConfiguration scm, Capability capability)
    {
        if(capability.equals(Capability.VIEW_CHANGESET) && scm.getType().equals("cvs"))
        {
            return false;
        }
        
        return super.hasCapability(scm, capability);
    }

    public String getDetails()
    {
        return "ViewVC [" + getBaseURL() + "]";
    }

    public String getChangesetURL(Revision revision)
    {
        if(revision instanceof CvsRevision)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath() + "?rev=" + revision.getRevisionString() + "&view=rev");
    }

    public String getFileViewURL(String path, FileRevision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath(), StringUtils.urlEncodePath(path) + "?rev=" + revision.getRevisionString() + "&view=markup");
    }

    public String getFileDownloadURL(String path, FileRevision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "*checkout*", getProjectPath(), StringUtils.urlEncodePath(path) + "?rev=" + revision.getRevisionString());
    }

    public String getFileDiffURL(String path, FileRevision revision)
    {
        FileRevision previous = revision.getPrevious();
        if(previous == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath(), StringUtils.urlEncodePath(path) + "?r1=" + previous.getRevisionString() + "&r2=" + revision.getRevisionString());
    }
}
