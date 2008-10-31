package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

import java.util.LinkedList;
import java.util.List;


/**
 * A test implementation of {@link com.zutubi.pulse.core.scm.api.ScmFeedbackHandler}
 * which just records the feedback.
 */
public class RecordingScmFeedbackHandler implements ScmFeedbackHandler
{
    private List<String> statusMessages = new LinkedList<String>();

    public void reset()
    {
        statusMessages.clear();
    }

    public void status(String message)
    {
        statusMessages.add(message);
    }

    public List<String> getStatusMessages()
    {
        return statusMessages;
    }

    public void checkCancelled() throws ScmCancelledException
    {

    }
}
