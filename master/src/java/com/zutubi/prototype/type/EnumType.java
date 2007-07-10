package com.zutubi.prototype.type;

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

    public Object instantiate(Object data, Instantiator instantiator) throws TypeException
    {
        try
        {
            return Enum.valueOf(getClazz(), (String) data);
        }
        catch (IllegalArgumentException e)
        {
            throw new TypeException("Illegal enumeration value '" + data.toString() + "'");
        }
    }

    public Object unstantiate(Object instance) throws TypeException
    {
        return instance.toString();
    }
}
