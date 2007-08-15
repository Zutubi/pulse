package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.scm.ScmCancelledException;
import com.zutubi.pulse.core.scm.ScmEventHandler;

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
                handler.fileChanged(new Change(m.group(1), m.group(2), PerforceClient.decodeAction(m.group(3))));
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
