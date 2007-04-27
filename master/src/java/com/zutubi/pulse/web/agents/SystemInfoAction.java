package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.util.logging.Logger;

/**
 *
 *
 */
public class SystemInfoAction extends AgentActionSupport
{
    private static final Logger LOG = Logger.getLogger(SystemInfoAction.class);

    private SystemInfo info;

    /**
     */
    public String execute()
    {
        Agent agent = getAgent();
        if(agent.isOnline())
        {
            try
            {
                info = agent.getService().getSystemInfo();
            }
            catch(RuntimeException e)
            {
                addActionError("Unable to contact agent: " + e.getMessage());
                LOG.warning(e);
            }
        }
        else
        {
            addActionError("Agent is not online.");
        }
        
        // The UI will handle agent errors
        return SUCCESS;
    }

    // extract this into a formatter that can be used by the UI to define the format of the information.
    public long getPercentage(long a, long b)
    {
        return (long) ((((float)a)/((float)b)) * 100);
    }

    public SystemInfo getInfo()
    {
        return info;
    }
}
