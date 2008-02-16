package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.restore.ArchiveException;
import com.zutubi.util.logging.Logger;

/**
 *
 *
 */
public class ExecuteArchiveAction extends RestoreActionSupport
{
    private static final Logger LOG = Logger.getLogger(ExecuteArchiveAction.class);

    public String execute() throws Exception
    {
        try
        {
            archiveManager.createArchive();

            return SUCCESS;
        }
        catch (ArchiveException e)
        {
            LOG.error(e);
            return ERROR;
        }
    }
}
