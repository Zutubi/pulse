package com.zutubi.tove.config.health;

/**
 * Represents a single problem found in a tove config store.
 *
 * @see ConfigurationHealthReport
 */
public class ConfigurationHealthProblem
{
    private String path;
    private String message;

    public ConfigurationHealthProblem(String path, String message)
    {
        this.path = path;
        this.message = message;
    }

    public String getPath()
    {
        return path;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return path + ": " + message;
    }
}
