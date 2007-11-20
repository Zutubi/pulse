package com.zutubi.prototype.type;

import com.zutubi.util.TextUtils;

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
        String s = (String) data;
        if (TextUtils.stringSet(s))
        {
            try
            {
                return Enum.valueOf(getClazz(), s);
            }
            catch (IllegalArgumentException e)
            {
                throw new TypeException("Illegal enumeration value '" + data.toString() + "'");
            }
        }
        else
        {
            return null;
        }
    }

    public Object unstantiate(Object instance) throws TypeException
    {
        if (instance == null)
        {
            return "";
        }
        else
        {
            return instance.toString();
        }
    }

    public Object toXmlRpc(Object data) throws TypeException
    {
        // Leave it as is (a string).
        return data;
    }

    public String fromXmlRpc(Object data) throws TypeException
    {
        typeCheck(data, String.class);
        return (String) data;
    }
}
