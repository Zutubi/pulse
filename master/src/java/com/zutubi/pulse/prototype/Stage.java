package com.zutubi.pulse.prototype;

import com.zutubi.pulse.core.model.PersistentName;
import com.zutubi.pulse.model.PostBuildAction;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.prototype.config.project.AgentRequirements;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a build stage: part of a build.
 */
public class Stage
{
    private PersistentName pname;
    private AgentRequirements hostRequirements;
    private String recipe;
    private List<ResourceRequirement> resourceRequirements = new LinkedList<ResourceRequirement>();
    private Map<String, PostBuildAction> postActions = new LinkedHashMap<String, PostBuildAction>();

}
