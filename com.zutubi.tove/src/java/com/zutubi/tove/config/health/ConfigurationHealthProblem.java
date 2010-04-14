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
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ConfigurationHealthProblem that = (ConfigurationHealthProblem) o;

        if (message != null ? !message.equals(that.message) : that.message != null)
        {
            return false;
        }
        if (path != null ? !path.equals(that.path) : that.path != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return path + ": " + message;
    }
}
