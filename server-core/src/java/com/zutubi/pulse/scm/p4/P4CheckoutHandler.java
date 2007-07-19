package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.NumericalFileRevision;
import com.zutubi.pulse.scm.SCMCancelledException;
import com.zutubi.pulse.scm.SCMCheckoutEventHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class P4CheckoutHandler extends P4ErrorDetectingHandler
{
    private static final Pattern PATTERN = Pattern.compile("(.+)#([0-9]+) - (refreshing|updating|added as|deleted as) (.+)");

    private SCMCheckoutEventHandler handler;

    public P4CheckoutHandler(boolean throwOnStderr, SCMCheckoutEventHandler handler)
    {
        super(throwOnStderr);
        this.handler = handler;
    }

    public void handleStdout(String line)
    {
        if (handler != null)
        {
            Matcher m = PATTERN.matcher(line);
            if (m.matches())
            {
                FileRevision fileRevision = new NumericalFileRevision(Long.parseLong(m.group(2)));
                handler.fileCheckedOut(new Change(m.group(1), fileRevision, P4Server.decodeAction(m.group(3))));
            }
        }
    }

    public void checkCancelled() throws SCMCancelledException
    {
        if(handler != null)
        {
            handler.checkCancelled();
        }
    }
}
