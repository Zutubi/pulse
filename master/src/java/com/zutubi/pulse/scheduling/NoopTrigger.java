/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.model.Project;

/**
 * <class-comment/>
 */
public class NoopTrigger extends Trigger
{
    protected static final String TYPE = "noop";

    public NoopTrigger()
    {
    }

    public NoopTrigger(String name)
    {
        this(name, DEFAULT_GROUP);
    }

    public NoopTrigger(String name, String group)
    {
        super(name, group);
    }

    public NoopTrigger copy(Project oldProject, Project newProject)
    {
        NoopTrigger copy = new NoopTrigger();
        copyCommon(copy, oldProject, newProject);
        return copy;
    }

    public String getType()
    {
        return TYPE;
    }
}
