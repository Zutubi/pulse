package com.zutubi.tove.type;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.MutableRecord;

/**
 * Helper base class that takes care of some of the type implementation.
 */
public abstract class AbstractType implements Type
{
    protected TypeRegistry typeRegistry;

    private Class clazz;
    private String symbolicName;

    public AbstractType(Class clazz)
    {
        this(clazz, null);
    }

    public AbstractType(Class type, String symbolicName)
    {
        this.clazz = type;
        this.symbolicName = symbolicName;
    }

    public Class getClazz()
    {
        return clazz;
    }

    public Type getTargetType()
    {
        return this;
    }

    public Type getActualType(Object value)
    {
        return this;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    protected static void typeCheck(Object data, Class expectedClass) throws TypeException
    {
        if(!expectedClass.isInstance(data))
        {
            throw new TypeException("Expecting '" + expectedClass.getName() + "', found '" + data.getClass().getName() + "'");
        }
    }

    protected void copyMetaToRecord(Object instance, MutableRecord record)
    {
        if(instance instanceof Configuration)
        {
            Configuration configuration = (Configuration) instance;
            for(String key: configuration.metaKeySet())
            {
                record.putMeta(key, configuration.getMeta(key));
            }
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
