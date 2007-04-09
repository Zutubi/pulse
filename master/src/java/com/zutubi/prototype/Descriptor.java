package com.zutubi.prototype;

import com.zutubi.prototype.type.record.Record;

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

    Object instantiate(String path, Record record);
}
