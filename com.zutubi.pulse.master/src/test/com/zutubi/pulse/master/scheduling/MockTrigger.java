package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.master.model.Project;

/**
 */
public class MockTrigger extends Trigger
{
    public Trigger copy(Project oldProject, Project newProject)
    {
        throw new RuntimeException("not implemented");
    }

    public String getType()
    {
        return "mock";
    }


}
