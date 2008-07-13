package com.zutubi.tove.type.record;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.GraphFunction;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Convenient abstract base for record implementations.
 */
public abstract class AbstractRecord implements Record
{
    protected static final String SYMBOLIC_NAME_KEY = "symbolicName";
    protected static final String HANDLE_KEY = Configuration.HANDLE_KEY;
    protected static final String PERMANENT_KEY = Configuration.PERMANENT_KEY;
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
        return other != null && metaEquals(other) && simpleEquals(other);
    }

    public boolean metaEquals(Record other)
    {
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

        return true;
    }

    public boolean simpleEquals(Record other)
    {
        Set<String> simpleKeys = simpleKeySet();
        Set<String> otherSimpleKeys = other.simpleKeySet();
        if(!simpleKeys.equals(otherSimpleKeys))
        {
            return false;
        }

        for(String key: simpleKeys)
        {
            if (!RecordUtils.valuesEqual(get(key), other.get(key)))
            {
                return false;
            }
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
        if(!StringUtils.equals(getSymbolicName(), other.getSymbolicName()))
        {
            return false;
        }

        Set<String> keySet = keySet();
        if(keySet.size() != other.keySet().size())
        {
            return false;
        }

        for(String key: keySet)
        {
            if(!RecordUtils.valuesEqual(get(key), other.get(key)))
            {
                return false;
            }
        }

        Set<String> metaKeySet = metaKeySet();
        if(metaKeySet.size() != other.metaKeySet().size())
        {
            return false;
        }

        for(String key: metaKeySet)
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

    public void forEach(GraphFunction<Record> f)
    {
        f.process(this);
        for(String key: nestedKeySet())
        {
            f.push(key);
            ((Record) get(key)).forEach(f);
            f.pop();
        }
    }
}
