package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.Scope;
import com.zutubi.pulse.core.VariableHelper;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.util.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 *
 */
public abstract class Scm extends Entity implements Cloneable
{
    private static final Logger LOG = Logger.getLogger(Scm.class);

    private static final SimpleDateFormat PULSE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    private static final SimpleDateFormat FISHEYE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    static
    {
        // fisheye presents its change set ids using GMT times.  By setting the date format timezone to
        // GMT, we ensure that local server times are converted into GMT times.
        FISHEYE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private String path;
    private Properties properties;

    /**
     * The active status of this SCM.
     */
    private boolean monitor;
    private static final String CHANGE_VIEWER_URL = "change.viewer.url";
    private static final String PROPERTY_REVISION = "revision";
    private static final String PROPERTY_AUTHOR = "author";
    private static final String PROPERTY_BRANCH = "branch";
    private static final String PROPERTY_TIMESTAMP_PULSE = "time.pulse";
    private static final String PROPERTY_TIMESTAMP_FISHEYE = "time.fisheye";

    public abstract SCMServer createServer() throws SCMException;

    public boolean supportsUpdate()
    {
        try
        {
            return createServer().supportsUpdate();
        }
        catch (SCMException e)
        {
            return false;
        }
    }

    protected Properties getProperties()
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        return properties;
    }

    private void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getChangeViewerUrl()
    {
        return (String) properties.get(CHANGE_VIEWER_URL);
    }

    public void setChangeViewerUrl(String url)
    {
        properties.put(CHANGE_VIEWER_URL, url);
    }

    /**
     * An active scm can be monitored by automated processes. Inactive scms should not.
     *
     * @return true if this scm can be monitored by automated processes.
     */
    public boolean isMonitor()
    {
        return monitor;
    }

    /**
     * Set the active status of this scm configuration.
     *
     * @param b
     */
    public void setMonitor(boolean b)
    {
        this.monitor = b;
    }

    /**
     * Returns true if scm filtering is enabled for this scm, false otherwise.
     *
     * @return true if scm filtering is enabled.
     */
    public boolean isFilterEnabled()
    {
        return getFilteredPaths().size() > 0;
    }

    public List<String> getFilteredPaths()
    {
        // reconstruct the filter paths, or an empty list if no paths are stored.
        Properties props = getProperties();
        if (!props.containsKey("filters.length"))
        {
            return new LinkedList<String>();
        }
        int length = Integer.valueOf(props.getProperty("filters.length"));
        List<String> paths = new LinkedList<String>();
        for (int i = 0; i < length; i++)
        {
            paths.add(i, props.getProperty("filters." + i));
        }
        return paths;
    }

    public void setFilteredPaths(List<String> paths)
    {
        // we need to update the contents of the properties object.
        Properties props = getProperties();

        // remove any existing content.
        Enumeration propertyNames = props.propertyNames();
        while (propertyNames.hasMoreElements())
        {
            String name = (String) propertyNames.nextElement();
            if (name.startsWith("filters"))
            {
                props.remove(name);
            }
        }

        // add the new content.
        props.setProperty("filters.length", Integer.toString(paths.size()));
        int index = 0;
        for (String path: paths)
        {
            props.put("filters." + index, path);
            index++;
        }
    }

    public boolean addExcludedPath(String str)
    {
        List<String> paths = getFilteredPaths();
        if (!paths.contains(str))
        {
            paths.add(str);
            setFilteredPaths(paths);
            return true;
        }
        return false;
    }

    public Scm copy()
    {
        try
        {
            Scm copy = (Scm) clone();
            // ensure that the copy is considered a new instance by hibernate.
            copy.setId(0);
            // Deep copy the properties
            copy.properties = new Properties();
            copy.properties.putAll(properties);
            return copy;
        }
        catch (CloneNotSupportedException e)
        {
            // Never happens
        }

        return null;
    }

    public static void validateChangeViewerURL(String url)
    {
        Scope scope = new Scope();
        scope.add(new Property(PROPERTY_REVISION, ""));
        scope.add(new Property(PROPERTY_AUTHOR, ""));
        scope.add(new Property(PROPERTY_BRANCH, ""));
        scope.add(new Property(PROPERTY_TIMESTAMP_FISHEYE, ""));
        scope.add(new Property(PROPERTY_TIMESTAMP_PULSE, ""));

        try
        {
            VariableHelper.replaceVariables(url, true, scope);
        }
        catch (FileLoadException e)
        {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public String getChangeUrl(Revision revision)
    {
        String url = getChangeViewerUrl();
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
                return VariableHelper.replaceVariables(url, true, scope);
            }
            catch (FileLoadException e)
            {

                LOG.warning("Unable to replace variables in change viewer url: " + e.getMessage(), e);
            }
        }

        return null;
    }
}
