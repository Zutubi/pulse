package com.zutubi.tove.type;

import com.zutubi.tove.annotations.Transient;

import java.util.Map;

/**
 *
 *
 */
public interface Extendable
{
    @Transient
    Map<String, Object> getExtensions();
}
