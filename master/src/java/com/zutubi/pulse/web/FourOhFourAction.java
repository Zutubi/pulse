/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web;

import com.zutubi.pulse.bootstrap.StartupManager;

/**
 * The 404 action handles the presentation of the 404 page when a resource is requested but not found.
 *
 * In particular, this action will check if the system has started. If not, it will present a 'system starting'
 * page so that if a user hits pulse before it has had a chance to initialise, we do not generate a 404 since
 * the resouce is likely to be available shortly.
 */
public class FourOhFourAction extends ActionSupport
{
    private StartupManager startupManager;

    /**
     * Required resource.
     *
     * @param startupManager
     */
    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public String execute() throws Exception
    {
        // Q: What was the requested URL? This is important.

        if (startupManager.isSystemStarted())
        {
            return "404";
        }
        else
        {
            return "starting";
        }
    }
}
