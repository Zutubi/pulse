package com.zutubi.pulse.core.scm.cvs.client.commands;

import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;

import java.io.File;

/**
 * <class comment/>
 */
public class UpdateListener extends BootstrapListener
{

    public UpdateListener(ScmFeedbackHandler handler, File workingDirectory)
    {
        super(handler, workingDirectory);
    }

    public void fileRemoved(FileRemovedEvent evt)
    {
        reportStatus("D", new File(evt.getFilePath()));
    }

    public void fileInfoGenerated(FileInfoEvent evt)
    {
        FileInfoContainer c = evt.getInfoContainer();
        if (!(c instanceof DefaultFileInfoContainer))
        {
            return;
        }

        DefaultFileInfoContainer infoContainer = (DefaultFileInfoContainer) evt.getInfoContainer();
        if ("U".equals(infoContainer.getType()))
        {
            reportStatus("U", infoContainer.getFile());
        }
    }
}
