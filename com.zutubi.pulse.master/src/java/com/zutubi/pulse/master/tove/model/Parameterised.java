package com.zutubi.pulse.master.tove.model;

import java.util.Map;

/**
 */
public interface Parameterised
{
    void addParameter(String key, Object value);

    void addAll(Map<String, Object> parameters);

    Map<String, Object> getParameters();

    boolean hasParameter(String key);

    Object getParameter(String key);
}
