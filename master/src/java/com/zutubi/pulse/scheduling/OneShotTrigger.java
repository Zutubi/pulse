/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.model.Project;

/**
 * <class-comment/>
 */
public class OneShotTrigger extends SimpleTrigger
{
    public OneShotTrigger()
    {
    }

    public OneShotTrigger(String name, String group)
    {
        super(name, group, null, 0, 0);
    }

    public OneShotTrigger copy(Project oldProject, Project newProject)
    {
        OneShotTrigger copy = new OneShotTrigger();
        copyCommon(copy, oldProject, newProject);
        return copy;
    }
}
