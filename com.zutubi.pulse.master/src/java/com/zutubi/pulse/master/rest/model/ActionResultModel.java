package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.type.TypeException;

/**
 * Holds the result of executing a config action.
 */
public class ActionResultModel
{
    private boolean success;
    private String message;
    private String newPath;
    private CompositeModel model;

    public ActionResultModel(ActionResult result, String newPath, CompositeModel model) throws TypeException
    {
        success = result.getStatus() == ActionResult.Status.SUCCESS;
        message = result.getMessage();
        this.newPath = newPath;
        this.model = model;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getMessage()
    {
        return message;
    }

    public String getNewPath()
    {
        return newPath;
    }

    public CompositeModel getModel()
    {
        return model;
    }
}
