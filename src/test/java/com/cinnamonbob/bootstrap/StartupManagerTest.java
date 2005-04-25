package com.cinnamonbob.bootstrap;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;

/**
 * 
 *
 */
public class StartupManagerTest extends TestCase
{

    public StartupManagerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {

    }

    public void tearDown() throws Exception
    {

    }

    public void testStartupSystem()
    {
        StartupManager manager = StartupManager.getInstance();
        assertFalse(manager.isSystemStarted());

        StartupManager.startupSystem();

        assertTrue(manager.isSystemStarted());

        // test that spring is configured with the test configuration.
        ApplicationContext context = StartupManager.getInstance().getApplicationContext();        
        assertNotNull(context.getBean("testBean"));
    }
}
