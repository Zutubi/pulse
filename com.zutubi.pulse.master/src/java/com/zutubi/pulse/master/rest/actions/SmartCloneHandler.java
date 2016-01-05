package com.zutubi.pulse.master.rest.actions;

import com.zutubi.pulse.master.rest.model.ActionModel;
import com.zutubi.tove.config.api.ActionResult;

import java.util.Map;

/**
 * Handler for the smart clone (clone by introducing a new parent template) action.
 */
public class SmartCloneHandler extends AbstractCloneHandler
{
    @Override
    public ActionModel getModel(String path, String variant)
    {
        if (!configurationRefactoringManager.canSmartClone(path))
        {
            throw new IllegalArgumentException("Cannot smart clone path '" + path + "'");
        }

        return getModel(path, true);
    }

    @Override
    public ActionResult doAction(String path, String variant, Map<String, Object> input)
    {
        return doAction(path, input, true);
    }

}
