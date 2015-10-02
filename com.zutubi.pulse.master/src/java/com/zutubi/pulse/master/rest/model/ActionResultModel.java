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
    private CompositeModel model;

    public ActionResultModel(ActionResult result, CompositeModel model) throws TypeException
    {
        success = result.getStatus() == ActionResult.Status.SUCCESS;
        message = result.getMessage();
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

    public CompositeModel getModel()
    {
        return model;
    }
}
