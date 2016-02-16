package com.zutubi.pulse.master.rest.actions;

import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.ui.model.ActionModel;

import java.util.Map;

/**
 * Handler for the clone action (does not handle smart cloning).
 */
public class CloneHandler extends AbstractCloneHandler
{
    @Override
    public ActionModel getModel(String path, String variant)
    {
        if (!configurationRefactoringManager.canClone(path))
        {
            throw new IllegalArgumentException("Cannot clone path '" + path + "'");
        }

        return getModel(path, false);
    }

    @Override
    public ActionResult doAction(String path, String variant, Map<String, Object> input)
    {
        return doAction(path, input, false);
    }
}
