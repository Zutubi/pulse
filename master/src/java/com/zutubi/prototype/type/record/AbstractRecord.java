package com.zutubi.prototype.type.record;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.HashSet;
import java.util.Set;

/**
 * Convenient abstract base for record implementations.
 */
public abstract class AbstractRecord implements Record
{
    protected static final String HANDLE_KEY = "handle";
    protected static final long UNDEFINED = 0;

    public long getHandle()
    {
        String idString = getMeta(HANDLE_KEY);
        if (idString != null)
        {
            try
            {
                return Long.parseLong(idString);
            }
            catch (NumberFormatException e)
            {
                // Illegal
            }
        }

        return UNDEFINED;
    }

    public boolean isCollection()
    {
        return getSymbolicName() == null;
    }

    public Set<String> simpleKeySet()
    {
        return CollectionUtils.filter(keySet(), new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return !(get(s) instanceof Record);
            }
        }, new HashSet<String>(size()));
    }

    public Set<String> nestedKeySet()
    {
        return CollectionUtils.filter(keySet(), new Predicate<String>()
        {
            public boolean satisfied(String s)
            {
                return get(s) instanceof Record;
            }
        }, new HashSet<String>(size()));
    }

    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof AbstractRecord))
        {
            return false;
        }

        AbstractRecord other = (AbstractRecord) obj;
        if(getSymbolicName() == null && !(other.getSymbolicName() == null))
        {
            return false;
        }

        if(getSymbolicName() != null && !getSymbolicName().equals(other.getSymbolicName()))
        {
            return false;
        }

        if(keySet().size() != other.keySet().size())
        {
            return false;
        }

        for(String key: keySet())
        {
            if(!get(key).equals(other.get(key)))
            {
                return false;
            }
        }

        if(metaKeySet().size() != other.metaKeySet().size())
        {
            return false;
        }

        for(String key: metaKeySet())
        {
            if(!getMeta(key).equals(other.getMeta(key)))
            {
                return false;
            }
        }

        return true;
    }

    public int hashCode()
    {
        int code = keySet().hashCode();
        code = 31 * code + metaKeySet().hashCode();
        code = 31 * code + (getSymbolicName() == null ? 0 : getSymbolicName().hashCode());
        return code;
    }
}
