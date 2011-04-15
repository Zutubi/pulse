package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.build.queue.FatController;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;
import org.springframework.security.access.AccessDeniedException;

/**
 * Ajax action to cancel a build in the queue.
 */
public class CancelQueuedBuildAction extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(CancelQueuedBuildAction.class);
    
    /**
     * The id of the queued build request event to be cancelled.
     */
    private long id;
    private SimpleResult result;

    private FatController fatController;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        try
        {
            if (fatController.cancelQueuedBuild(id))
            {
                result = new SimpleResult(true, I18N.format("request.cancelled"));
            }
            else
            {
                result = new SimpleResult(false, I18N.format("request.not.found"));
            }
        }
        catch (AccessDeniedException e)
        {
            result = new SimpleResult(false, I18N.format("cancel.not.permitted"));
        }

        return SUCCESS;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }
}
