package com.zutubi.prototype.type.record;

import java.util.Map;
import java.util.Set;
import java.util.Collection;

/**
 *
 *
 */
public interface Record extends Map<String, Object>, Cloneable
{
    void setSymbolicName(String name);

    String getSymbolicName();

    void putMeta(String key, String value);

    String getMeta(String key);

    Object clone() throws CloneNotSupportedException;
}
