package com.zutubi.pulse.restore.feedback;

import junit.framework.TestCase;

/**
 *
 *
 */
public class FeedbackTest extends TestCase
{
    private Feedback feedback;

    protected void setUp() throws Exception
    {
        super.setUp();

        feedback = new Feedback();
    }

    protected void tearDown() throws Exception
    {
        feedback = null;

        super.tearDown();
    }

    public void testStartProcess()
    {
        assertFalse(feedback.isStarted());
        feedback.start();
        assertTrue(feedback.isStarted());
    }

    public void testEndProcess()
    {
        assertFalse(feedback.isFinished());
        feedback.completed();
        assertTrue(feedback.isFinished());
    }
}
