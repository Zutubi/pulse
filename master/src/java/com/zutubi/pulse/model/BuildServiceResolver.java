/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;

/**
 * <class-comment/>
 */
public interface BuildServiceResolver
{
    BuildService resolve();

    String getHostName();
}
