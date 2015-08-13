package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.tove.config.DefaultReferenceOptionProvider;
import com.zutubi.tove.type.TypeProperty;

import java.util.Map;

/**
 * An option provider for the agent field of a build stage.
 */
public class BuildStageAgentOptionProvider extends DefaultReferenceOptionProvider
{
    public Option getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return null;
    }

    public Map<String, String> getMap(Object instance, String path, TypeProperty property)
    {
        Map<String, String> allAgents = super.getMap(instance, path, property);
        allAgents.put("0", "[any capable]");
        return allAgents;
    }
}
