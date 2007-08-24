package com.zutubi.pulse.webwork.mapping;

import java.util.Map;

/**
 */
public interface ActionResolver
{
    String getAction();
    Map<String, String> getParameters();
    ActionResolver getChild(String name);
}
