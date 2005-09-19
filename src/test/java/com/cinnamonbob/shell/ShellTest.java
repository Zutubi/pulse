/**
 * <class-comment/>
 */
package com.cinnamonbob.shell;

import junit.framework.*;

import java.io.*;

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
        Shell shell = ShellFactory.createShell();
        try
        {
            shell.getEnvironment().clear();
            shell.open();
            assertTrue(shell.execute("java") != 0);
        }
        finally
        {
            shell.close();
        }
    }

    public void testExecute() throws Exception
    {
        Shell shell = ShellFactory.createShell();
        try
        {
            shell.open();

            assertEquals(0, shell.execute("dir"));
            assertEquals(0, shell.execute("dir"));
            assertFalse(shell.execute("badCommand") == 0);
        }
        finally
        {
            shell.close();
        }
    }

    public void testInputStream() throws Exception
    {
        Shell shell = ShellFactory.createShell();
        try
        {
            shell.open();
            StreamReader reader = new StreamReader(shell.getInput(), System.out);
            reader.start();

            assertEquals(0, shell.execute("dir"));

            shell.close();

            reader.join();
        }
        finally
        {
        }
    }
}