package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.Map;

/**
 */
public interface ActionResolver
{
    String getAction();
    Map<String, String> getParameters();
    ActionResolver getChild(String name);
}
