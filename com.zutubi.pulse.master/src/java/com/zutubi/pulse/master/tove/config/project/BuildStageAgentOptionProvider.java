package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.DefaultReferenceOptionProvider;
import com.zutubi.tove.ui.handler.FormContext;

import java.util.Map;

/**
 * An option provider for the agent field of a build stage.
 */
public class BuildStageAgentOptionProvider extends DefaultReferenceOptionProvider
{
    public Map<String, String> getMap(TypeProperty property, FormContext context)
    {
        Map<String, String> allAgents = super.getMap(property, context);
        allAgents.put("", "[any capable]");
        return allAgents;
    }
}
