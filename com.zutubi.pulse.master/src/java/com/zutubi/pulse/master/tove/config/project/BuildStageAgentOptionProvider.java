package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.DefaultReferenceOptionProvider;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.tove.type.TypeProperty;

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
