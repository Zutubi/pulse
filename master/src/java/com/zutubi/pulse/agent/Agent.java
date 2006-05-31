package com.zutubi.pulse.agent;

import com.zutubi.pulse.model.DatabaseResourceRepository;

/**
 * An abstraction over an agent, be it the local (master) agent or a slave.
 */
public interface Agent
{
    DatabaseResourceRepository getResourceRepository();
}
