package com.zutubi.pulse.acceptance.components.pulse.agent;

import com.zutubi.util.StringUtils;

import java.util.Map;

/**
 * Summarised agent info from the agents page.
 */
public class AgentInfo
{
    public String name;
    public String location;
    public String status;
    public String executingOwner;
    public String executingNumber;
    public String executingStage;

    public AgentInfo(Map<String, String> row)
    {
        name = row.get("name");
        location = row.get("location");
        status = row.get("status");
        
        String stage = row.get("executingStage");
        if (stage.contains("::"))
        {
            String[] pieces = stage.split("::");
            executingOwner = pieces[0].trim();
            executingNumber = StringUtils.stripPrefix(pieces[1].trim(), "build ");
            executingStage = pieces[2].trim();
        }
    }
}
