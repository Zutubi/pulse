package com.zutubi.tove.ui.model;

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
    private ConfigModel model;

    private ActionResultModel(boolean success, String message, String newPath, ConfigModel model)
    {
        this.success = success;
        this.message = message;
        this.newPath = newPath;
        this.model = model;
    }

    public ActionResultModel(ActionResult result, String newPath, ConfigModel model) throws TypeException
    {
        this(result.getStatus() == ActionResult.Status.SUCCESS, result.getMessage(), newPath, model);
    }

    public ActionResultModel(boolean success, String message, ConfigModel model)
    {
        this(success, message, null, model);
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

    public ConfigModel getModel()
    {
        return model;
    }
}
