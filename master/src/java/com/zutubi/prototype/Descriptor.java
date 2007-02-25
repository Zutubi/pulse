package com.zutubi.prototype;

import java.util.Map;

/**
 *
 *
 */
public interface Descriptor
{
    void addParameter(String key, Object value);

    void addAll(Map<String, Object> parameters);

    Map<String, Object> getParameters();

    Object instantiate(Object obj);
}
