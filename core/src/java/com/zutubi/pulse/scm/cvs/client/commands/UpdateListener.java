package com.zutubi.pulse.scm.cvs.client.commands;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.scm.ScmCheckoutEventHandler;
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
    private final ScmCheckoutEventHandler handler;

    public UpdateListener(ScmCheckoutEventHandler handler)
    {
        if (handler == null)
        {
            throw new IllegalArgumentException("handler is a required argument.");
        }
        this.handler = handler;
    }

    public void fileRemoved(FileRemovedEvent e)
    {
        handler.fileCheckedOut(new Change(e.getFilePath(), null, Change.Action.DELETE));
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
            handler.fileCheckedOut(new Change(infoContainer.getFile().getPath(), null, Change.Action.EDIT));
        }
    }
}
