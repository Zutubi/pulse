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

    private ActionResultModel(boolean success, String message, String newPath, CompositeModel model)
    {
        this.success = success;
        this.message = message;
        this.newPath = newPath;
        this.model = model;
    }

    public ActionResultModel(ActionResult result, String newPath, CompositeModel model) throws TypeException
    {
        this(result.getStatus() == ActionResult.Status.SUCCESS, result.getMessage(), newPath, model);
    }

    public ActionResultModel(CompositeModel model)
    {
        this(true, null, null, model);
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
