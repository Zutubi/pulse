/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

/**
 * 
 *
 */
public interface ProjectListener
{
    public void buildTriggered(PulseFile project);
    public void buildComplete(PulseFile project);
}
 
