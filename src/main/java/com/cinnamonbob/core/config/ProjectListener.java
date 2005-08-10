package com.cinnamonbob.core.config;

/**
 * 
 *
 */
public interface ProjectListener
{
    public void buildTriggered(BobFile project);
    public void buildComplete(BobFile project);
}
 
