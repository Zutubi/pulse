package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;

import java.util.Collection;

/**
 * Interface to sort a collection of agents.
 */
public interface AgentSorter
{
    Iterable<Agent> sort(Collection<Agent> agents, RecipeAssignmentRequest request);
}
