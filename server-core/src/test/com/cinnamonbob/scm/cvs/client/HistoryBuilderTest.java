package com.cinnamonbob.scm.cvs.client;

import junit.framework.TestCase;

/**
 * 
 *
 */
public class HistoryBuilderTest extends TestCase
{

    public void testParseCheckout()
    {
        String checkout = "O 2005-05-07 04:02 -0700 leanne project      =project= /e/tmp/cvstest/*";
        HistoryBuilder builder = new HistoryBuilder();
        builder.parseLine(checkout, false);

        assertEquals(1, builder.getHistoryInfo().size());
        HistoryInfo information = builder.getHistoryInfo().get(0);

        assertNotNull(information);
        assertTrue(information.isCheckout());
        assertEquals("leanne", information.getUser());
        assertEquals("project", information.getPathInRepository());
    }

    public void testParseCommit()
    {
        String commit = "M 2005-05-07 04:09 -0700 daniel 1.2 foo     project/test == /e/tmp/cvstest/project/test";
        HistoryBuilder builder = new HistoryBuilder();
        builder.parseLine(commit, false);

        assertEquals(1, builder.getHistoryInfo().size());
        HistoryInfo information = builder.getHistoryInfo().get(0);

        assertNotNull(information);
        assertTrue(information.isCommit());
        assertEquals("daniel", information.getUser());
        assertEquals("foo", information.getFile());
        assertEquals("project/test", information.getPathInRepository());
    }


}
