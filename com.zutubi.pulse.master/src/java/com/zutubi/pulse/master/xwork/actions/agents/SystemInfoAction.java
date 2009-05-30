package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupManager;
import com.zutubi.util.Sort;
import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An action to display information about the machine and JVM an agent is
 * running on.
 */
public class SystemInfoAction extends AgentActionBase
{
    private static final Logger LOG = Logger.getLogger(SystemInfoAction.class);

    private SystemInfo info;

    private ConfigurationManager configurationManager;
    private StartupManager startupManager;

    /**
     */
    public String execute()
    {
        Agent agent = getAgent();
        if(agent == null)
        {
            info = SystemInfo.getSystemInfo(configurationManager, startupManager);
        }
        else
        {
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

    public List<String> getSortedKeys()
    {
        List<String> keys = new LinkedList<String>();
        for (Object o: info.getSystemProperties().keySet())
        {
            keys.add((String) o);
        }
        
        Collections.sort(keys, new Sort.StringComparator());
        return keys;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }
}
