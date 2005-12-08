package com.cinnamonbob.scheduling;

import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public class ManualSchedulerStrategyTest extends TestCase
{
    private ManualSchedulerStrategy scheduler;

    public ManualSchedulerStrategyTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        scheduler = new ManualSchedulerStrategy();
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        scheduler = null;

        super.tearDown();
    }

    public void test()
    {
        
    }

}