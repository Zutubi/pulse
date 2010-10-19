package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.tove.annotations.Transient;

/**
 * An interface for determining if an agent satisfies the requirements to
 * have a recipe dispatched to it.  Requirements are configurable at the
 * stage level.
 */
public interface AgentRequirements
{
    @Transient
    public String getSummary();
    public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service);

    /**
     * Get a human readable reason why the specified request could not be
     * fulfilled.  This assumed that {@link #isFulfilledBy(RecipeAssignmentRequest, AgentService)}
     * has returned false.
     *
     * The format of the reason should be a concise to the point statement.
     *
     * @param request   the recipe request providing the context for the
     *                  isFulfillable check.
     *
     * @return a human readable message for why the requirements are not
     * fulfilled in the context of the request.
     */
    @Transient
    String getUnfulFilledReason(RecipeAssignmentRequest request);
}
