package com.cinnamonbob.core;

import com.cinnamonbob.core.model.Resource;

import java.util.List;

/**
 */
public interface ResourceRepository
{
    boolean hasResource(String name);

    Resource getResource(String name);

    List<String> getResourceNames();

    void addResource(Resource resource);
}
