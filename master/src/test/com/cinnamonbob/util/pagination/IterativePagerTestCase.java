package com.cinnamonbob.util.pagination;

import junit.framework.*;

import java.util.List;
import java.util.LinkedList;

public class IterativePagerTestCase extends TestCase
{
    private List<Integer> data = null;
    private IterativePager pager = null;

    public IterativePagerTestCase(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        data = new LinkedList<Integer>();
        for (int i = 1; i <= 100; i++ )
        {
            data.add(i);
        }
        pager = new IterativePager(data);
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        pager = null;
        data = null;

        super.tearDown();
    }

    public void testPaging()
    {
        assertFalse(pager.hasPreviousPage());
        assertFalse(pager.isFirstPage());
        assertFalse(pager.isLastPage());

        try
        {
            pager.currentPage();
            assertFalse("Expected an exception.", true);
        }
        catch (IndexOutOfBoundsException e)
        {
            // all is well.
        }

        assertTrue(pager.hasNextPage());
        assertEquals(data.subList(0, 10), pager.nextPage());
        assertEquals(data.subList(0, 10), pager.currentPage());
        assertTrue(pager.isFirstPage());

        assertEquals(data.subList(10, 20), pager.nextPage());
        assertFalse(pager.isFirstPage());
        assertFalse(pager.isLastPage());

        assertEquals(data.subList(90, 100), pager.lastPage());
        assertFalse(pager.isFirstPage());
        assertTrue(pager.isLastPage());

        assertEquals(data.subList(80, 90), pager.previousPage());
        assertFalse(pager.isFirstPage());
        assertFalse(pager.isLastPage());
    }
}