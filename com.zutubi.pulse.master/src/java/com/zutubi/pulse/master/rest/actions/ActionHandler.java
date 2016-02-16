package com.zutubi.pulse.master.rest.actions;

import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.tove.ui.model.ActionModel;

import java.util.Map;

/**
 * Describes helpers that can execute a config action.  These are used for known actions such as
 * clone/pull up.  More general actions are handled by *ConfigurationAction classes (which give
 * pluggability but are less powerful).
 */
public interface ActionHandler
{
    ActionModel getModel(String path, String variant);
    ActionResult doAction(String path, String variant, Map<String, Object> input);
}
