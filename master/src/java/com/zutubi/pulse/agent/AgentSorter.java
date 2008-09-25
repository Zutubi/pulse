package com.zutubi.pulse.agent;

import com.zutubi.pulse.RecipeAssignmentRequest;

import java.util.Collection;

/**
 * Interface to sort a collection of agents.
 */
public interface AgentSorter
{
    Iterable<Agent> sort(Collection<Agent> agents, RecipeAssignmentRequest request);
}
