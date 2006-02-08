package com.cinnamonbob;

import junit.framework.*;

/**
 * <class-comment/>
 */
public class VersionTest extends TestCase
{
    public VersionTest(String testName)
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

    public void test()
    {
        assertEquals("@BUILD_DATE@", Version.getBuildDate());
        assertEquals("@BUILD_NUMBER@", Version.getBuildNumber());
        assertEquals("@VERSION@", Version.getVersion());
    }
}