package com.zutubi.pulse.master.util.monitor;

import com.zutubi.util.Constants;

public class LongRunningNoopTask extends AbstractTask implements FeedbackAware
{
    private long duration;

    private TaskFeedback feedback;

    public LongRunningNoopTask(long duration)
    {
        super("Long running task.");

        this.duration = duration;
    }

    public void execute() throws TaskException
    {
        long startTime = System.currentTimeMillis();

        long projectedEndTime = startTime + duration;

        long currentTime = startTime;
        while (currentTime < projectedEndTime )
        {
            feedback.setPercetageComplete( (int)((currentTime - startTime) * 100 / duration) );
            try
            {
                Thread.sleep(Constants.SECOND);
            }
            catch (InterruptedException e)
            {
                // noop - ignore interruptions.
            }
            currentTime = System.currentTimeMillis();
        }
        feedback.setPercetageComplete(100);
    }

    public void setFeedback(TaskFeedback feedback)
    {
        this.feedback = feedback;
    }
}
