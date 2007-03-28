package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * A change viewer that can be configured to suit a custom system y providing
 * URLs with embedded variables.
 */
public class CustomChangeViewer extends ChangeViewer
{
    private static final String PROPERTY_REVISION = "revision";
    private static final String PROPERTY_PREVIOUS_REVISION = "previous.revision";
    private static final String PROPERTY_AUTHOR = "author";
    private static final String PROPERTY_BRANCH = "branch";
    private static final String PROPERTY_PATH = "path";
    private static final String PROPERTY_PATH_RAW = "path.raw";
    private static final String PROPERTY_PATH_FORM = "path.form";
    private static final String PROPERTY_TIMESTAMP_PULSE = "time.pulse";
    private static final String PROPERTY_TIMESTAMP_FISHEYE = "time.fisheye";

    private static final SimpleDateFormat PULSE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    public static final SimpleDateFormat FISHEYE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private String changesetURL;
    private String fileViewURL;
    private String fileDownloadURL;
    private String fileDiffURL;

    static
    {
        // fisheye presents its change set ids using GMT times.  By setting the date format timezone to
        // GMT, we ensure that local server times are converted into GMT times.
        FISHEYE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public CustomChangeViewer()
    {
    }

    public CustomChangeViewer(String changesetUrl, String fileViewURL, String fileDownloadURL, String fileDiffURL)
    {
        this.changesetURL = changesetUrl;
        this.fileViewURL = fileViewURL;
        this.fileDownloadURL = fileDownloadURL;
        this.fileDiffURL = fileDiffURL;
    }

    public String getChangesetURL()
    {
        return changesetURL;
    }

    public void setChangesetURL(String changesetURL)
    {
        this.changesetURL = changesetURL;
    }

    public String getFileViewURL()
    {
        return fileViewURL;
    }

    public void setFileViewURL(String fileViewURL)
    {
        this.fileViewURL = fileViewURL;
    }

    public String getFileDownloadURL()
    {
        return fileDownloadURL;
    }

    public void setFileDownloadURL(String fileDownloadURL)
    {
        this.fileDownloadURL = fileDownloadURL;
    }

    public String getFileDiffURL()
    {
        return fileDiffURL;
    }

    public void setFileDiffURL(String fileDiffURL)
    {
        this.fileDiffURL = fileDiffURL;
    }

    public boolean hasCapability(Scm scm, Capability capability)
    {
        switch(capability)
        {
            case DOWNLOAD_FILE:
                return TextUtils.stringSet(fileDownloadURL);
            case VIEW_FILE:
                return TextUtils.stringSet(fileViewURL);
            case VIEW_FILE_DIFF:
                return TextUtils.stringSet(fileDiffURL);
            case VIEW_CHANGESET:
                return TextUtils.stringSet(changesetURL);
            default:
                return false;
        }
    }

    public String getDetails()
    {
        return "custom";
    }

    public String getChangesetURL(Revision revision)
    {
        return resolveURL(changesetURL, revision);
    }

    public String getFileViewURL(String path, FileRevision revision)
    {
        return resolveFileURL(fileViewURL, path, revision);
    }

    public String getFileDownloadURL(String path, FileRevision revision)
    {
        return resolveFileURL(fileDownloadURL, path, revision);
    }

    public String getFileDiffURL(String path, FileRevision revision)
    {
        if(revision.getPrevious() == null)
        {
            return null;
        }

        return resolveFileURL(fileDiffURL, path, revision);
    }

    public ChangeViewer copy()
    {
        return new CustomChangeViewer(changesetURL, fileViewURL, fileDownloadURL, fileDiffURL);
    }

    private String resolveURL(String url, Revision revision)
    {
        if(TextUtils.stringSet(url))
        {
            Scope scope = new Scope();
            scope.add(new Property(PROPERTY_REVISION, revision.getRevisionString()));
            scope.add(new Property(PROPERTY_AUTHOR, revision.getAuthor()));
            scope.add(new Property(PROPERTY_BRANCH, revision.getBranch()));

            if(revision.getDate() != null)
            {
                scope.add(new Property(PROPERTY_TIMESTAMP_PULSE, PULSE_DATE_FORMAT.format(revision.getDate())));
                scope.add(new Property(PROPERTY_TIMESTAMP_FISHEYE, FISHEYE_DATE_FORMAT.format(revision.getDate())));
            }

            try
            {
                return VariableHelper.replaceVariables(url, scope, true);
            }
            catch (FileLoadException e)
            {
                // Never happens with allowUnresolved set to true
            }
        }

        return null;
    }

    private String resolveFileURL(String url, String path, FileRevision revision)
    {
        if(TextUtils.stringSet(url))
        {
            Scope scope = new Scope();
            scope.add(new Property(PROPERTY_PATH, StringUtils.urlEncodePath(path)));
            scope.add(new Property(PROPERTY_PATH_RAW, path));
            scope.add(new Property(PROPERTY_PATH_FORM, StringUtils.formUrlEncode(path)));
            scope.add(new Property(PROPERTY_REVISION, revision.getRevisionString()));
            FileRevision previous = revision.getPrevious();
            if(previous != null)
            {
                scope.add(new Property(PROPERTY_PREVIOUS_REVISION, previous.getRevisionString()));
            }
            
            try
            {
                return VariableHelper.replaceVariables(url, scope, true);
            }
            catch (FileLoadException e)
            {
                // Never happens with allowUnresolved set to true
            }
        }

        return null;
    }

    public static void validateChangesetURL(String url)
    {
        Scope scope = new Scope();
        scope.add(new Property(PROPERTY_REVISION, ""));
        scope.add(new Property(PROPERTY_AUTHOR, ""));
        scope.add(new Property(PROPERTY_BRANCH, ""));
        scope.add(new Property(PROPERTY_TIMESTAMP_FISHEYE, ""));
        scope.add(new Property(PROPERTY_TIMESTAMP_PULSE, ""));

        try
        {
            VariableHelper.replaceVariables(url, scope);
        }
        catch (FileLoadException e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static void validateFileURL(String url)
    {
        Scope scope = new Scope();
        scope.add(new Property(PROPERTY_PATH, ""));
        scope.add(new Property(PROPERTY_REVISION, ""));
        scope.add(new Property(PROPERTY_PREVIOUS_REVISION, ""));

        try
        {
            VariableHelper.replaceVariables(url, scope);
        }
        catch (FileLoadException e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

}
