package com.zutubi.pulse.scm.cvs.client.commands;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.scm.ScmEventHandler;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;

/**
 * <class comment/>
 */
public class UpdateListener extends CVSAdapter
{
    private final ScmEventHandler handler;

    public UpdateListener(ScmEventHandler handler)
    {
        if (handler == null)
        {
            throw new IllegalArgumentException("handler is a required argument.");
        }
        this.handler = handler;
    }

    public void fileRemoved(FileRemovedEvent e)
    {
        handler.fileChanged(new Change(e.getFilePath(), null, Change.Action.DELETE));
    }

    public void fileInfoGenerated(FileInfoEvent e)
    {
        FileInfoContainer c = e.getInfoContainer();
        if (!(c instanceof DefaultFileInfoContainer))
        {
            return;
        }
        
        DefaultFileInfoContainer infoContainer = (DefaultFileInfoContainer) e.getInfoContainer();
        if ("U".equals(infoContainer.getType()))
        {
            handler.fileChanged(new Change(infoContainer.getFile().getPath(), null, Change.Action.EDIT));
        }
    }
}
