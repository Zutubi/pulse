package com.zutubi.tove.config.health;

import com.zutubi.util.StringUtils;

/**
 * Abstract base to support implementation of health problems.
 */
public abstract class HealthProblemSupport implements HealthProblem
{
    private String path;
    private String message;

    protected HealthProblemSupport(String path, String message)
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

    public boolean isSolvable()
    {
        return true;
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

        HealthProblemSupport that = (HealthProblemSupport) o;

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
        return (StringUtils.stringSet(path) ? path : "<root>") + ": " + message;
    }
}
