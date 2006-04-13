/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm.svn;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class SVNServerTest extends TestCase
{
    private SVNServer server;

    protected void setUp() throws Exception
    {
        super.setUp();
//        server = new SVNServer("http://svn.apache.org/repos/asf/jakarta/oro/trunk/", "anonymous", "");
    }

    protected void tearDown() throws Exception
    {
        server = null;
        super.tearDown();
    }

    public void testPlaceholder() {}

//    public void testList() throws SCMException
//    {
//        List<RemoteFile> files = server.getListing("docs/images");
//        assertEquals(2, files.size());
//        assertEquals("logo.gif", files.get(0).getName());
//        assertEquals("logoSmall.gif", files.get(1).getName());
//    }
//
//    public void testListNonExistent() throws SCMException
//    {
//        try
//        {
//            server.getListing("nosuchfile");
//            fail();
//        }
//        catch (SCMException e)
//        {
//            assertTrue(e.getMessage().contains("not found"));
//        }
//    }
}
