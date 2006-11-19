package com.zutubi.pulse.web.server;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.FatController;

/**
 * <class comment/>
 */
public class CancelQueuedBuildAction extends ActionSupport
{
    /**
     * The id of the queued build request event to be cancelled.
     */
    private long id;

    private FatController fatController;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public String execute() throws Exception
    {
        // if the queued build request does not exist, then the build has started. It will need to be cancelled
        // separately.
        fatController.cancelQueuedBuild(id);

        return SUCCESS;
    }
}
