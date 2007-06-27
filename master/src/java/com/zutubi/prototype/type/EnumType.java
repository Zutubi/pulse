package com.zutubi.prototype.type;

import com.zutubi.prototype.config.InstanceCache;

/**
 * Type used for enum-valued properties.  They are similar to simple string
 * values, except are converted to the enums on instantiated objects and
 * allow more smarts (e.g. default presentation of options to select).
 */
public class EnumType extends SimpleType
{
    public <T extends Enum<T>> EnumType(Class<T> clazz)
    {
        super(clazz);
    }

    @SuppressWarnings({"unchecked"})
    public Class<? extends Enum> getClazz()
    {
        return (Class<? extends Enum>) super.getClazz();
    }

    public Object instantiate(String path, InstanceCache cache, Object record) throws TypeException
    {
        return Enum.valueOf(getClazz(), (String) record);
    }

    public Object unstantiate(Object instance) throws TypeException
    {
        return instance.toString();
    }
}
