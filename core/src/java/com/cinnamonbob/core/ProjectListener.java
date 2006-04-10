package com.zutubi.pulse.core;

/**
 * 
 *
 */
public interface ProjectListener
{
    public void buildTriggered(BobFile project);
    public void buildComplete(BobFile project);
}
 
