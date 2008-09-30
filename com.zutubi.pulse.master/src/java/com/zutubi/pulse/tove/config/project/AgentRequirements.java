package com.zutubi.pulse.tove.config.project;

import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.RecipeAssignmentRequest;

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
