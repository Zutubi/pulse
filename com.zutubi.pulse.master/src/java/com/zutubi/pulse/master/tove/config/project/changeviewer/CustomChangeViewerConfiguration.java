package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.engine.api.Property;
import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.StringUtils;
import com.zutubi.util.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * A change viewer that can be configured to suit a custom system y providing
 * URLs with embedded variables.
 */
@Form(fieldOrder = {"changesetURL", "fileViewURL", "fielDownloadURL", "fileDiffURL"})
@SymbolicName("zutubi.customChangeViewerConfig")
@Wire
public class CustomChangeViewerConfiguration extends ChangeViewerConfiguration
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

    public static final SimpleDateFormat PULSE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    public static final SimpleDateFormat FISHEYE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private String changesetURL;
    private String fileViewURL;
    private String fileDownloadURL;
    private String fileDiffURL;
    
    private ConfigurationProvider configurationProvider;

    static
    {
        // fisheye presents its change set ids using GMT times.  By setting the date format timezone to
        // GMT, we ensure that local server times are converted into GMT times.
        FISHEYE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public CustomChangeViewerConfiguration()
    {
    }

    public CustomChangeViewerConfiguration(String changesetUrl, String fileViewURL, String fileDownloadURL, String fileDiffURL)
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

    public boolean hasCapability(Capability capability)
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

    public String getFileViewURL(String path, String revision)
    {
        return resolveFileURL(fileViewURL, path, revision);
    }

    public String getFileDownloadURL(String path, String revision)
    {
        return resolveFileURL(fileDownloadURL, path, revision);
    }

    public String getFileDiffURL(String path, String revision)
    {
        ScmConfiguration config = lookupScmConfiguration();
        String previous = config.getPreviousRevision(revision);
        if (previous == null)
        {
            return null;
        }
        
        return resolveFileURL(fileDiffURL, path, revision);
    }

    private String resolveURL(String url, Revision revision)
    {
        if(TextUtils.stringSet(url))
        {
            PulseScope scope = new PulseScope();
            scope.add(new Property(PROPERTY_REVISION, revision.getRevisionString()));

            Map<String, Object> properties = ChangeViewerUtils.getRevisionProperties(revision);
            if (properties.containsKey(ChangeViewerUtils.PROPERTY_AUTHOR))
            {
                scope.add(new Property(PROPERTY_AUTHOR, (String) properties.get(ChangeViewerUtils.PROPERTY_AUTHOR)));
            }

            if (properties.containsKey(ChangeViewerUtils.PROPERTY_BRANCH))
            {
                scope.add(new Property(PROPERTY_BRANCH, (String) properties.get(ChangeViewerUtils.PROPERTY_BRANCH)));
            }

            if (properties.containsKey(ChangeViewerUtils.PROPERTY_DATE))
            {
                Date date = (Date) properties.get(ChangeViewerUtils.PROPERTY_DATE);
                scope.add(new Property(PROPERTY_TIMESTAMP_PULSE, PULSE_DATE_FORMAT.format(date)));
                scope.add(new Property(PROPERTY_TIMESTAMP_FISHEYE, FISHEYE_DATE_FORMAT.format(date)));
            }

            try
            {
                return VariableHelper.replaceVariables(url, scope, VariableHelper.ResolutionStrategy.RESOLVE_NON_STRICT);
            }
            catch (FileLoadException e)
            {
                // Never happens with allowUnresolved set to true
                e.printStackTrace();
            }
        }

        return null;
    }

    private String resolveFileURL(String url, String path, String revision)
    {
        if(TextUtils.stringSet(url))
        {
            PulseScope scope = new PulseScope();
            scope.add(new Property(PROPERTY_PATH, StringUtils.urlEncodePath(path)));
            scope.add(new Property(PROPERTY_PATH_RAW, path));
            scope.add(new Property(PROPERTY_PATH_FORM, StringUtils.formUrlEncode(path)));
            scope.add(new Property(PROPERTY_REVISION, revision));

            ScmConfiguration config = lookupScmConfiguration();
            String previous = config.getPreviousRevision(revision);
            if(previous != null)
            {
                scope.add(new Property(PROPERTY_PREVIOUS_REVISION, previous));
            }
            
            try
            {
                return VariableHelper.replaceVariables(url, scope, VariableHelper.ResolutionStrategy.RESOLVE_NON_STRICT);
            }
            catch (FileLoadException e)
            {
                // Never happens with non-strict resolution
            }
        }

        return null;
    }

    public static void validateChangesetURL(String url)
    {
        PulseScope scope = new PulseScope();
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
        PulseScope scope = new PulseScope();
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

    protected ScmConfiguration lookupScmConfiguration()
    {
        ProjectConfiguration project = configurationProvider.getAncestorOfType(this, ProjectConfiguration.class);
        return project.getScm();
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

}
