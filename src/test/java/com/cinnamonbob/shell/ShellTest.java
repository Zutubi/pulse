/**
 * <class-comment/>
 */
package com.cinnamonbob.shell;

import junit.framework.*;

public class ShellTest extends TestCase
{

    public ShellTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
    }

    public void testEmptyEnvironment() throws Exception
    {
        Shell shell = new Shell();
        try
        {
            shell.getEnvironment().clear();
            shell.open();
            shell.execute("java");
            shell.waitFor();
            assertTrue(shell.getExitStatus() != 0);
        }
        finally
        {
            shell.close();
        }
    }

    public void testExecute() throws Exception
    {
        Shell shell = new Shell();
        try
        {
            shell.open();
            StreamReader reader = new StreamReader(shell.getInput(), System.out);
            reader.start();

            shell.execute("dir");
            shell.waitFor();
            assertEquals(0, shell.getExitStatus());

            shell.execute("dir");
            shell.waitFor();
            assertEquals(0, shell.getExitStatus());

            shell.execute("badCommand");
            shell.waitFor();
            assertTrue(shell.getExitStatus() != 0);
            assertTrue(shell.getExitStatus() != Shell.EXIT_STATUS_UNKNOWN);

            shell.close();
            reader.join();
        }
        finally
        {
        }
    }

    public void testExecuteMultipleCommands() throws Exception
    {
        Shell shell = new Shell();
        try
        {
            shell.open();
            StreamReader reader = new StreamReader(shell.getInput(), System.out);
            reader.start();

            assertFalse(shell.isExecuting());
            assertTrue(shell.isIdle());
            shell.execute("dir");
            assertTrue(shell.isExecuting());
            assertFalse(shell.isIdle());

            shell.execute("dir");
            shell.execute("dir");

            assertTrue(shell.isExecuting());
            assertFalse(shell.isIdle());
            shell.waitFor();
            assertFalse(shell.isExecuting());
            assertTrue(shell.isIdle());

            assertEquals(0, shell.getExitStatus());

            shell.close();
            reader.join();
        }
        finally
        {

        }
    }

    public void testInputStream() throws Exception
    {
        Shell shell = new Shell();
        try
        {
            shell.open();
            StreamReader reader = new StreamReader(shell.getInput(), System.out);
            reader.start();

            shell.execute("dir");
            shell.waitFor();
            assertEquals(0, shell.getExitStatus());

            shell.close();

            reader.join();
        }
        finally
        {
        }
    }
}