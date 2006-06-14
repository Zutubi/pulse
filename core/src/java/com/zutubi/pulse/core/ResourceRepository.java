package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Resource;

import java.util.List;

/**
 */
public interface ResourceRepository
{
    boolean hasResource(String name, String version);

    boolean hasResource(String name);

    Resource getResource(String name);

    List<String> getResourceNames();

}
