package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.util.logging.Logger;

/**
 * Action for viewing a configuration path.
 */
public class DisplayAction extends ToveActionSupport
{
    private static final Logger LOG = Logger.getLogger(DisplayAction.class);

    public String execute() throws Exception
    {
        try
        {
            return doRender();
        }
        catch (Exception e)
        {
            LOG.severe(e);
            addActionError(e.getMessage());
            return ERROR;
        }
    }
}
