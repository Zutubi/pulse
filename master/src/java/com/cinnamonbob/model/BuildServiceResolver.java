package com.cinnamonbob.model;

import com.cinnamonbob.BuildService;

/**
 * <class-comment/>
 */
public interface BuildServiceResolver
{
    BuildService resolve();

    String getHostName();
}
