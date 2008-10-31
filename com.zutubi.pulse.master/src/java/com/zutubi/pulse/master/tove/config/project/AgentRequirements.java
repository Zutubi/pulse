package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.Transient;
import com.zutubi.pulse.master.AgentService;
import com.zutubi.pulse.master.RecipeAssignmentRequest;

/**
 * An interface for determining if an agent satisfies the requirements to
 * have a recipe dispatched to it.  Requirements are configurable at the
 * stage level.
 */
public interface AgentRequirements
{
    @Transient
    public String getSummary();
    public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service);
}
