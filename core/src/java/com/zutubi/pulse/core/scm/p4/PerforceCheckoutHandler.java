package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.FileRevision;
import com.zutubi.pulse.core.model.NumericalFileRevision;
import com.zutubi.pulse.core.scm.ScmCancelledException;
import com.zutubi.pulse.core.scm.ScmEventHandler;
import com.zutubi.pulse.core.scm.p4.PerforceErrorDetectingHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class PerforceCheckoutHandler extends PerforceErrorDetectingHandler
{
    private static final Pattern PATTERN = Pattern.compile("(.+)#([0-9]+) - (refreshing|updating|added as|deleted as) (.+)");

    private ScmEventHandler handler;

    public PerforceCheckoutHandler(boolean throwOnStderr, ScmEventHandler handler)
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
                handler.fileChanged(new Change(m.group(1), fileRevision, PerforceClient.decodeAction(m.group(3))));
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
