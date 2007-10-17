package com.zutubi.prototype.type.record;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

/**
 * Convenient abstract base for record implementations.
 */
public abstract class AbstractRecord implements Record
{
    protected static final String HANDLE_KEY        = "handle";
    protected static final String PERMANENT_KEY     = "permanent";
    protected static final String SYMBOLIC_NAME_KEY = "symbolicName";

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

    public boolean isPermanent()
    {
        return Boolean.valueOf(getMeta(PERMANENT_KEY));
    }

    public boolean isCollection()
    {
        return getSymbolicName() == null;
    }

    public boolean shallowEquals(Record other)
    {
        if(other == null)
        {
            return false;
        }

        Set<String> metaKeys = metaKeySet();
        Set<String> otherMetaKeys = other.metaKeySet();
        if(!metaKeys.equals(otherMetaKeys))
        {
            return false;
        }

        for(String key: metaKeys)
        {
            if(!getMeta(key).equals(other.getMeta(key)))
            {
                return false;
            }
        }

        Set<String> simpleKeys = simpleKeySet();
        Set<String> otherSimpleKeys = other.simpleKeySet();
        if(!simpleKeys.equals(otherSimpleKeys))
        {
            return false;
        }

        for(String key: simpleKeys)
        {
            if (!valuesEqual(get(key), other.get(key)))
            {
                return false;
            }
        }

        return true;
    }

    public boolean valuesEqual(Object value, Object otherValue)
    {
        if(value == null)
        {
            return otherValue == null;
        }
        else if(otherValue == null)
        {
            return false;
        }

        if(value instanceof Object[])
        {
            if(!(otherValue instanceof Object[]) || !Arrays.equals((Object[])value, (Object[])otherValue))
            {
                return false;
            }
        }
        else if(otherValue instanceof Object[] || !value.equals(otherValue))
        {
            return false;
        }

        return true;
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
            if(!valuesEqual(get(key), other.get(key)))
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
