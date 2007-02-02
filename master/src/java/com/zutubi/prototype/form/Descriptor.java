package com.zutubi.prototype.form;

import java.util.Map;

/**
 *
 *
 */
public interface Descriptor
{
    void addParameter(String key, Object value);
    
    Map<String, Object> getParameters();

    Object instantiate(Object obj);
}
