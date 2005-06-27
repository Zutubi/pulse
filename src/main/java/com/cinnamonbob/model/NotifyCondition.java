package com.cinnamonbob.model;

import com.cinnamonbob.core2.BuildResult;

/**
 * Describes an interface for making notifications conditional based on
 * properties of the build result (e.g. only notify on build failed).
 * 
 * @author jsankey
 */
public interface NotifyCondition
{
    public boolean satisfied(BuildResult result);
}
