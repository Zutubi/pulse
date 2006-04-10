package com.cinnamonbob;

import com.cinnamonbob.test.BobTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class VersionTest extends BobTestCase
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

    public void testDefaultVersion()
    {
        Version v = Version.getVersion();
        assertEquals("@BUILD_DATE@", v.getBuildDate());
        assertEquals("@BUILD_NUMBER@", v.getBuildNumber());
        assertEquals("@VERSION@", v.getVersionNumber());
    }

    public void testReadWriteVersion() throws IOException
    {
        Version v = new Version("a", "b", "c");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        v.write(out);

        Version v2 = Version.load(new ByteArrayInputStream(out.toByteArray()));

        assertEquals(v.getVersionNumber(), v2.getVersionNumber());
        assertEquals(v.getBuildNumber(), v2.getBuildNumber());
        assertEquals(v.getBuildDate(), v2.getBuildDate());
    }
}