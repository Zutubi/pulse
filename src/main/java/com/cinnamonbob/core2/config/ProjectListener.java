package com.cinnamonbob.core2.config;

/**
 * 
 *
 */
public interface ProjectListener
{
    public void buildTriggered(BobFile project);
    public void buildComplete(BobFile project);
}
 
