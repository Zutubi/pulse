package com.zutubi.pulse.master.xwork.actions.ajax;

/**
 * Object representing the JSON result for the GetLatestRevisionAction.
 *
 * @see com.zutubi.pulse.master.webwork.dispatcher.FlexJsonResult
 */
public class GetLatestRevisionActionResult
{
    private boolean successful = false;
    private String latestRevision;
    private String error;

    public void setSuccessful(boolean successful)
    {
        this.successful = successful;
    }

    public void setLatestRevision(String latestRevision)
    {
        this.latestRevision = latestRevision;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public String getLatestRevision()
    {
        return latestRevision;
    }

    public String getError()
    {
        return error;
    }
}
