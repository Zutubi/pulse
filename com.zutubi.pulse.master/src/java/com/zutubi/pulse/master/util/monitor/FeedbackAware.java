package com.zutubi.pulse.master.util.monitor;

/**
 * The feedback aware interface can be implemented by those tasks
 * and that are able to provide extra feedback to the UI.
 */
public interface FeedbackAware
{
    void setFeedback(TaskFeedback feedback);
}
