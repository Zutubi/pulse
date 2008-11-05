package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
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
        if(capability.equals(Capability.VIEW_CHANGELIST) && scm.getType().equals(CVS_TYPE))
        {
            return false;
        }
        
        return super.hasCapability(capability);
    }

    public String getChangelistURL(Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath() + "?rev=" + revision.getRevisionString() + "&view=rev");
    }

    public String getFileViewURL(String path, Revision changelistRevision, String fileRevision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath(), StringUtils.urlEncodePath(path) + "?rev=" + fileRevision + "&view=markup");
    }

    public String getFileDownloadURL(String path, Revision changelistRevision, String fileRevision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "*checkout*", getProjectPath(), StringUtils.urlEncodePath(path) + "?rev=" + fileRevision);
    }

    public String getFileDiffURL(String path, Revision changelistRevision, String fileRevision)
    {
        ScmConfiguration config = lookupScmConfiguration();
        String previous = config.getPreviousRevision(fileRevision);
        if(previous == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), getProjectPath(), StringUtils.urlEncodePath(path) + "?r1=" + previous + "&r2=" + fileRevision);
    }
}
