package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public class IdContext implements Context
{
    private String id;

    public IdContext(String id)
    {
        this.id = id;
    }

    public String getContext()
    {
        return this.id;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof IdContext))
        {
            return false;
        }

        final IdContext idContext = (IdContext) o;

        if (id != null ? !id.equals(idContext.id) : idContext.id != null)
        {
            return false;
        }

        return true;
    }

    public InputStream getResourceAsStream(String name)
    {
        return getClass().getResourceAsStream(name);
    }

    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }

    public String toString()
    {
        return "<id:" + id + ">";
    }
}
