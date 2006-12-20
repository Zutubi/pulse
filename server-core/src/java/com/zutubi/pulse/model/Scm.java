package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.util.logging.Logger;

import java.util.*;

/**
 * 
 *
 */
public abstract class Scm extends Entity implements Cloneable
{
    private static final Logger LOG = Logger.getLogger(Scm.class);

    private String path;
    private Properties properties;

    /**
     * The active status of this SCM.
     */
    private boolean monitor;

    private Integer pollingInterval;
    private Long lastPollTime;

    public abstract SCMServer createServer() throws SCMException;
    public abstract String getType();
    public abstract Map<String, String> getRepositoryProperties();

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

    public Integer getPollingInterval()
    {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval)
    {
        this.pollingInterval = pollingInterval;
    }

    public Long getLastPollTime()
    {
        return lastPollTime;
    }

    public void setLastPollTime(Long lastPollTime)
    {
        this.lastPollTime = lastPollTime;
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
     * @param b sets the active status to true or false.
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
            // Should ever happen, but if it does, we need some trace of it.
            LOG.error(e);
        }

        return null;
    }
}
