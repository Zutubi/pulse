package com.zutubi.pulse.web.agents;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.agent.Agent;

/**
 *
 *
 */
public class SystemInfoAction extends AgentActionSupport
{
    private SystemInfo info;

    /**
     */
    public String execute()
    {
        lookupSlave();
        Agent agent = getAgent();

        if(agent.isOnline())
        {
            try
            {
                info = agent.getSystemInfo();
            }
            catch(RuntimeException e)
            {
                addActionError("Unable to contact agent: " + e.getMessage());
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
