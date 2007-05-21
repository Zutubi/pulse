package com.zutubi.prototype.i18n;

import com.zutubi.i18n.context.Context;
import com.zutubi.prototype.type.Type;

import java.io.InputStream;

/**
 *
 *
 */
public class TypeContext implements Context
{
    private Type type;

    public TypeContext(Type type)
    {
        this.type = type;
    }

    public Type getType()
    {
        return type;
    }

    public InputStream getResourceAsStream(String name)
    {
        return type.getClazz().getResourceAsStream(name);
    }
}
