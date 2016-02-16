package com.zutubi.tove.ui;

import com.zutubi.tove.config.ToveRuntimeException;
import com.zutubi.tove.config.api.Configuration;

public class ValidationException extends ToveRuntimeException
{
    private Configuration instance;
    private String key;

    public ValidationException(Configuration instance, String key)
    {
        super("Validation failed");
        this.instance = instance;
        this.key = key;
    }

    public Configuration getInstance()
    {
        return instance;
    }

    public String getKey()
    {
        return key;
    }
}
