package com.zutubi.pulse.core.config;

import com.zutubi.config.annotations.Transient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Convenient base class for configuration types.
 */
public abstract class AbstractConfiguration implements Configuration
{
    private long handle;
    private String configurationPath;
    private boolean permanent;
    private List<String> instanceErrors = new LinkedList<String>();
    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();

    public long getHandle()
    {
        return handle;
    }

    public void setHandle(long handle)
    {
        this.handle = handle;
    }

    public String getConfigurationPath()
    {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath)
    {
        this.configurationPath = configurationPath;
    }

    public boolean isPermanent()
    {
        return permanent;
    }

    public void setPermanent(boolean permanent)
    {
        this.permanent = permanent;
    }

    @Transient
    public boolean isValid()
    {
        if(instanceErrors.size() > 0)
        {
            return false;
        }

        for(List<String> fieldMessages: fieldErrors.values())
        {
            if(fieldMessages.size() > 0)
            {
                return false;
            }
        }

        return true;
    }

    @Transient
    public List<String> getInstanceErrors()
    {
        return instanceErrors;
    }

    public void addInstanceError(String message)
    {
        instanceErrors.add(message);
    }

    public void clearInstanceErrors()
    {
        instanceErrors.clear();
    }

    @Transient
    public Map<String, List<String>> getFieldErrors()
    {
        return fieldErrors;
    }

    public void clearFieldErrors()
    {
        fieldErrors.clear();
    }

    @Transient
    public List<String> getFieldErrors(String field)
    {
        List<String> errors = fieldErrors.get(field);
        if(errors == null)
        {
            errors = new LinkedList<String>();
            fieldErrors.put(field, errors);
        }

        return errors;
    }

    public void addFieldError(String field, String message)
    {
        getFieldErrors(field).add(message);
    }

    public void clearFieldErrors(String field)
    {
        getFieldErrors(field).clear();
    }

    public int hashCode()
    {
        return new Long(handle).hashCode();
    }

    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        if(!(obj instanceof AbstractConfiguration))
        {
            return false;
        }

        AbstractConfiguration otherConfig = (AbstractConfiguration) obj;
        return otherConfig.handle != 0 && otherConfig.handle == handle;
    }
}
