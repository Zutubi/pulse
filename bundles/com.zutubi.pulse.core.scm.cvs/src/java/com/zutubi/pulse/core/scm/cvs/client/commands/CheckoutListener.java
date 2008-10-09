package com.zutubi.pulse.core.scm.cvs.client.commands;

import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;

import java.io.File;

/**
 * An adapter between the CVS library and our SCM API listener interfaces.
 */
public class CheckoutListener extends BootstrapListener
{
    public CheckoutListener(ScmFeedbackHandler handler, File workingDirectory)
    {
        super(handler, workingDirectory);
    }

    public void fileAdded(FileAddedEvent e)
    {
        reportStatus("U", new File(e.getFilePath()));
    }
}

