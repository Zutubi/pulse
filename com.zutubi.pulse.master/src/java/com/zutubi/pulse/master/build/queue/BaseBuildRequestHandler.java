package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.model.Sequence;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.bean.ObjectFactory;

/**
 * The base implementation of the build request handler interface.
 */
public abstract class BaseBuildRequestHandler implements BuildRequestHandler
{
    /**
     * The sequence that generates the meta build ids.
     */
    private Sequence sequence;

    /**
     * The meta build id for this handler.
     */
    private long metaBuildId;

    /**
     * The build queue to which this handler sends queue requests.
     */
    protected BuildQueue buildQueue;

    protected ProjectManager projectManager;
    protected ObjectFactory objectFactory;
    protected BuildRequestRegistry buildRequestRegistry;

    /**
     * Initialise the handler, assigning a meta build id.
     */
    public void init()
    {
        metaBuildId = sequence.getNext();
    }

    public long getMetaBuildId()
    {
        return metaBuildId;
    }

    public void setSequence(Sequence sequence)
    {
        this.sequence = sequence;
    }

    public void setBuildQueue(BuildQueue buildQueue)
    {
        this.buildQueue = buildQueue;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildRequestRegistry(BuildRequestRegistry buildRequestRegistry)
    {
        this.buildRequestRegistry = buildRequestRegistry;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
