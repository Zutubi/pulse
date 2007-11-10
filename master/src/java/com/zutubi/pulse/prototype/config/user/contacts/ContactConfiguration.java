package com.zutubi.pulse.prototype.config.user.contacts;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.model.BuildResult;

/**
 *
 *
 */
@SymbolicName("zutubi.userContactConfig")
@Table(columns = {"name", "uid"})
@Classification(collection = "contacts")
public abstract class ContactConfiguration extends AbstractNamedConfiguration
{
    @Internal
    private String lastError;

    public boolean hasError()
    {
        return lastError != null;
    }

    public String getLastError()
    {
        return lastError;
    }

    public void clearLastError()
    {
        lastError = null;
    }

    public void notify(BuildResult result, String subject, String rendered, String mimeType)
    {
        lastError = null;
        try
        {
            internalNotify(result, subject, rendered, mimeType);
        }
        catch(Exception e)
        {
            lastError = e.getClass().getName();
            if(e.getMessage() != null)
            {
                lastError += ": " + e.getMessage();
            }
        }
    }

    @Transient
    public abstract String getUid();

    protected abstract void internalNotify(BuildResult buildResult, String subject, String content, String mimeType) throws Exception;
}
