package com.zutubi.pulse.core.config;

import com.zutubi.config.annotations.Transient;

import java.util.*;

/**
 * Convenient base class for configuration types.
 */
public abstract class AbstractConfiguration implements Configuration
{
    private Map<String, String> meta = new HashMap<String, String>();
    private String configurationPath;
    private boolean concrete;
    private List<String> instanceErrors = new LinkedList<String>();
    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();

    public String getMeta(String key)
    {
        return meta.get(key);
    }

    public void putMeta(String key, String value)
    {
        meta.put(key, value);
    }

    public Set<String> metaKeySet()
    {
        return meta.keySet();
    }

    public long getHandle()
    {
        String m = getMeta(HANDLE_KEY);
        if(m == null)
        {
            return 0;
        }
        
        return Long.parseLong(m);
    }

    public void setHandle(long handle)
    {
        putMeta(HANDLE_KEY, Long.toString(handle));
    }

    public String getConfigurationPath()
    {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath)
    {
        this.configurationPath = configurationPath;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public void setConcrete(boolean concrete)
    {
        this.concrete = concrete;
    }

    public boolean isPermanent()
    {
        return Boolean.parseBoolean(getMeta(PERMANENT_KEY));
    }

    public void setPermanent(boolean permanent)
    {
        putMeta(PERMANENT_KEY, Boolean.toString(permanent));
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
        return new Long(getHandle()).hashCode();
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
        return otherConfig.getHandle() != 0 && otherConfig.getHandle() == getHandle();
    }
}
