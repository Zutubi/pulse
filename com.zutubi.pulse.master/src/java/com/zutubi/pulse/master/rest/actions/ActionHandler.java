package com.zutubi.pulse.master.rest.actions;

import com.zutubi.pulse.master.rest.model.ActionModel;
import com.zutubi.tove.config.api.ActionResult;

import java.util.Map;

/**
 * Describes helpers that can execute a config action.  These are used for known actions such as
 * clone/pull up.  More general actions are handled by *ConfigurationAction classes (which give
 * pluggability but are less powerful).
 */
public interface ActionHandler
{
    ActionModel getModel(String path);
    ActionResult doAction(String path, Map<String, Object> input);
}
