package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;

/**
 * A ChangeViewer for linking to a P4Web instance.
 */
@Form(fieldOrder = {"baseURL", "projectPath"})
@SymbolicName("zutubi.p4WebChangeViewerConfig")
public class P4WebChangeViewer extends BasePathChangeViewer
{
    public P4WebChangeViewer()
    {
        super(null, null);
    }

    public P4WebChangeViewer(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public String getChangelistURL(Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "@md=d@", revision.getRevisionString() + "?ac=10");
    }

    public String getFileViewURL(String path, Revision changelistRevision, String fileRevision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "@md=d@" + StringUtils.urlEncodePath(path) + "?ac=64&rev1=" + fileRevision);
    }

    public String getFileDownloadURL(String path, Revision changelistRevision, String fileRevision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "@md=d&rev1=" + fileRevision + "@" + StringUtils.urlEncodePath(path));
    }

    public String getFileDiffURL(String path, Revision changelistRevision, String fileRevision)
    {
        ScmConfiguration config = lookupScmConfiguration();
        String previous = config.getPreviousRevision(fileRevision);
        if(previous == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "@md=d@" + StringUtils.urlEncodePath(path) + "?ac=19&rev1=" + previous + "&rev2=" + fileRevision);
    }
}
