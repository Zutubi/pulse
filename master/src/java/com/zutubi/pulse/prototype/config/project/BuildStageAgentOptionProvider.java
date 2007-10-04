package com.zutubi.pulse.prototype.config.project;

import com.zutubi.prototype.MapOption;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.pulse.prototype.config.DefaultReferenceOptionProvider;

import java.util.Map;

/**
 * An option provider for the agent field of a build stage.
 */
public class BuildStageAgentOptionProvider extends DefaultReferenceOptionProvider
{
    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
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
