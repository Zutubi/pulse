/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.bootstrap.StartupManager;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.web.ActionSupport;
import com.sun.java_cup.internal.version;
import com.caucho.hessian.client.HessianRuntimeException;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.io.File;

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

        try
        {
            info = agent.getSystemInfo();
        }
        catch(HessianRuntimeException e)
        {
            addActionError("Unable to contact agent: " + e.getMessage());
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
