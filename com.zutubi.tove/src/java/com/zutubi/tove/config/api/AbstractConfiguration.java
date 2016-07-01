package com.zutubi.tove.config.api;

import java.util.*;

/**
 * Convenient base class for configuration types.
 */
public abstract class AbstractConfiguration implements Configuration
{
    private Map<String, String> meta = new HashMap<String, String>();
    // As the handle is used so frequently we cache here to avoid multiple lookups (and parses) from the meta map.
    private long handle = UNDEFINED;
    private String configurationPath;
    private boolean concrete;
    // This takes advantage of the pseudo-immutability of Configuration instances.  Instances should be constructed and
    // initialised then never changed afterwards.  Thus they need only be validated once post-initialisation.  At that
    // point this flag is flipped so we can avoid unnecessary future validation.
    private boolean validated = false;
    private List<String> instanceErrors = new LinkedList<String>();
    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();

    public String getMeta(String key)
    {
        return meta.get(key);
    }

    public void putMeta(String key, String value)
    {
        meta.put(key, value);
        if (key.equals(HANDLE_KEY))
        {
            handle = Long.parseLong(value);
        }
    }

    public Set<String> metaKeySet()
    {
        return meta.keySet();
    }

    public long getHandle()
    {
        return handle;
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
        // will return false if getMeta(PERMANENT_KEY) returns null.
        return Boolean.parseBoolean(getMeta(PERMANENT_KEY));
    }

    public void setPermanent(boolean permanent)
    {
        putMeta(PERMANENT_KEY, Boolean.toString(permanent));
    }

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

    public boolean needsValidation()
    {
        return !validated;
    }

    public void validated()
    {
        validated = true;
    }

    public List<String> getInstanceErrors()
    {
        return Collections.unmodifiableList(instanceErrors);
    }

    public void addInstanceError(String message)
    {
        instanceErrors.add(message);
    }

    public void clearInstanceErrors()
    {
        instanceErrors.clear();
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return Collections.unmodifiableMap(fieldErrors);
    }

    public void clearFieldErrors()
    {
        fieldErrors.clear();
    }

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
        return (int)(handle ^ (handle >>> 32));
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (obj == this)
        {
            return true;
        }

        if (!(obj instanceof AbstractConfiguration))
        {
            return false;
        }

        AbstractConfiguration otherConfig = (AbstractConfiguration) obj;
        return otherConfig.handle != UNDEFINED && otherConfig.handle == handle;
    }
}
