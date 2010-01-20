package com.zutubi.pulse.master.util.monitor;

/**
 * The feedback aware interface can be implemented by those tasks
 * and that are able to provide extra feedback to the UI.
 *
 * This interface is only effective when the task is run by the job
 * runner.
 *
 * @see JobRunner
 */
public interface FeedbackAware
{
    void setFeedback(TaskFeedback feedback);
}
