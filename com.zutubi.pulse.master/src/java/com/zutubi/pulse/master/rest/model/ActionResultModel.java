package com.zutubi.pulse.master.rest.model;

import com.zutubi.pulse.master.rest.ConfigModelBuilder;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.type.TypeException;

/**
 * Holds the result of executing a config action.
 */
public class ActionResultModel
{
    private boolean success;
    private String message;
    private ConfigDeltaModel delta;

    public ActionResultModel(ActionResult result, ConfigModelBuilder builder) throws TypeException
    {
        success = result.getStatus() == ActionResult.Status.SUCCESS;
        message = result.getMessage();
        if (result.getInvalidatedPaths().size() > 0)
        {
            delta = new ConfigDeltaModel();
            for (String path: result.getInvalidatedPaths())
            {
                delta.addUpdatedPath(path, builder.buildModel(null, path, -1));
            }
        }
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getMessage()
    {
        return message;
    }

    public ConfigDeltaModel getDelta()
    {
        return delta;
    }
}
