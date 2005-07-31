package com.cinnamonbob.model;

import com.cinnamonbob.core2.BuildResult;

/**
 * 
 *
 */
public interface BuildManager 
{
    void save(BuildResult result);

    BuildResult getBuildResult(long id);
}
