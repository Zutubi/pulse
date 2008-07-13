package com.zutubi.pulse.tove.config.project;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.RecipeDispatchRequest;

/**
 * An interface for determining if an agent satisfies the requirements to
 * have a recipe dispatched to it.  Requirements are configurable at the
 * stage level.
 */
public interface AgentRequirements
{
    public boolean fulfilledBy(RecipeDispatchRequest request, AgentService service);
}
