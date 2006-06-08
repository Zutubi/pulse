package com.zutubi.pulse.logging;

import com.zutubi.pulse.util.logging.Logger;
import com.opensymphony.webwork.dispatcher.VelocityResult;
import com.opensymphony.webwork.dispatcher.DispatcherUtils;

/**
 */
public class MasterLogConfigurationManager extends LogConfigurationManager
{
    public void init()
    {
        super.init();
        
        Logger l = Logger.getLogger(VelocityResult.class.getName());
        l.setFilter(new BlockingFilter());
        l = Logger.getLogger(DispatcherUtils.class.getName());
        l.setFilter(new BlockingFilter());
    }
}
