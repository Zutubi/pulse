package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import static com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerUtils.*;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A ChangeViewer for linking to a Fisheye instance.
 */
@Form(fieldOrder = {"baseURL", "projectPath", "pathStripPrefix"})
@SymbolicName("zutubi.fisheyeChangeViewerConfig")
public class FisheyeConfiguration extends BasePathChangeViewer
{
    static final String TYPE_CVS = "cvs";

    private static final DateFormat FISHEYE_DATE_FORMAT = new SimpleDateFormat(CustomChangeViewerConfiguration.FISHEYE_DATE_FORMAT_STRING);

    /**
     * Useful when configured against Perforce.  In this case the paths in
     * Pulse are full depot paths, but Fisheye expects a shorter form:
     * starting after a path configured in Fisheye itself.  This will be at
     * least //depot, but could be deeper, and needs to be stripped from the
     * front of all paths before using them to construct URLs.
     */
    private String pathStripPrefix;

    public FisheyeConfiguration()
    {
        super(null, null);
    }

    public FisheyeConfiguration(String baseURL, String projectPath)
    {
        super(baseURL, projectPath);
    }

    public String getPathStripPrefix()
    {
        return pathStripPrefix;
    }

    public void setPathStripPrefix(String pathStripPrefix)
    {
        this.pathStripPrefix = pathStripPrefix;
    }

    public String getRevisionURL(Revision revision)
    {
        return StringUtils.join("/", true, true, getBaseURL(), "changelog", getProjectPath(), "?cs=" + getChangesetString(revision));
    }

    public String getFileViewURL(ChangeContext context, FileChange fileChange)
    {
        Revision revision = chooseRevision(context, fileChange);
        return StringUtils.join("/", true, true, getBaseURL(), "browse", getProjectPath(), pathPart(fileChange) + "?r=" + revision);
    }

    public String getFileDownloadURL(ChangeContext context, FileChange fileChange)
    {
        Revision revision = chooseRevision(context, fileChange);
        return StringUtils.join("/", true, true, getBaseURL(), "browse", "~raw,r=" + revision, getProjectPath(), pathPart(fileChange));
    }

    public String getFileDiffURL(ChangeContext context, FileChange fileChange) throws ScmException
    {
        Revision revision = chooseRevision(context, fileChange);
        Revision previousRevision = useFileRevision(context) ? context.getPreviousFileRevision(fileChange) : context.getPreviousChangelistRevision();
        if (previousRevision == null)
        {
            return null;
        }

        return StringUtils.join("/", true, true, getBaseURL(), "browse", getProjectPath(), pathPart(fileChange) + "?r1=" + previousRevision + "&r2=" + revision);
    }

    private Revision chooseRevision(ChangeContext context, FileChange fileChange)
    {
        if (useFileRevision(context))
        {
            return fileChange.getRevision();
        }
        else
        {
            return context.getChangelist().getRevision();
        }
    }

    private boolean useFileRevision(ChangeContext context)
    {
        return isCVS(context.getScmConfiguration());
    }

    private String pathPart(FileChange fileChange)
    {
        return WebUtils.uriPathEncode(stripPathPrefix(fileChange.getPath()));
    }

    private String stripPathPrefix(String path)
    {
        if(StringUtils.stringSet(pathStripPrefix) && path.startsWith(pathStripPrefix))
        {
            path = path.substring(pathStripPrefix.length());
        }

        return path;
    }

    private String getChangesetString(Revision revision)
    {
        ScmConfiguration scm = lookupScmConfiguration();
        if (isCVS(scm))
        {
            Map<String, Object> properties = getRevisionProperties(revision);
            if (properties.containsKey(PROPERTY_AUTHOR) &&
                properties.containsKey(PROPERTY_BRANCH) &&
                properties.containsKey(PROPERTY_DATE))
            {

                String date;
                synchronized (FISHEYE_DATE_FORMAT)
                {
                    date = FISHEYE_DATE_FORMAT.format((Date) properties.get(PROPERTY_DATE));
                }
                return String.format("%s:%s:%s", properties.get(PROPERTY_BRANCH), properties.get(PROPERTY_AUTHOR), date);
            }
        }

        return revision.getRevisionString();
    }

    private boolean isCVS(ScmConfiguration scm)
    {
        return scm.getType().equals(TYPE_CVS);
    }
}
