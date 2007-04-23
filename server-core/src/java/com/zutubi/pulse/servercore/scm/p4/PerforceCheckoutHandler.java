package com.zutubi.pulse.servercore.scm.p4;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.NumericalFileRevision;
import com.zutubi.pulse.scm.ScmCancelledException;
import com.zutubi.pulse.scm.ScmCheckoutEventHandler;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.p4.PerforceErrorDetectingHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class PerforceCheckoutHandler extends PerforceErrorDetectingHandler
{
    private static final Pattern PATTERN = Pattern.compile("(.+)#([0-9]+) - (refreshing|updating|added as|deleted as) (.+)");

    private ScmCheckoutEventHandler handler;

    public PerforceCheckoutHandler(boolean throwOnStderr, ScmCheckoutEventHandler handler)
    {
        super(throwOnStderr);
        this.handler = handler;
    }

    public void handleStdout(String line) throws ScmException
    {
        if (handler != null)
        {
            Matcher m = PATTERN.matcher(line);
            if (m.matches())
            {
                FileRevision fileRevision = new NumericalFileRevision(Long.parseLong(m.group(2)));
                handler.fileCheckedOut(new Change(m.group(1), fileRevision, PerforceClient.decodeAction(m.group(3))));
            }
        }
    }

    public void checkCancelled() throws ScmCancelledException
    {
        if(handler != null)
        {
            handler.checkCancelled();
        }
    }
}
