package com.zutubi.pulse.master.tove.handler;

import com.zutubi.tove.config.api.Configuration;

/**
 * Context information available while building a form.  Note that sometimes no existing
 * instance is available, only a closest existing path.
 */
public class FormContext
{
    private Configuration existingInstance;
    private String closestExistingPath;

    public FormContext(Configuration existingInstance)
    {
        this.existingInstance = existingInstance;
        closestExistingPath = existingInstance.getConfigurationPath();
    }

    public FormContext(String closestExistingPath)
    {
        this.closestExistingPath = closestExistingPath;
    }

    public Configuration getExistingInstance()
    {
        return existingInstance;
    }

    public String getClosestExistingPath()
    {
        return closestExistingPath;
    }
}
