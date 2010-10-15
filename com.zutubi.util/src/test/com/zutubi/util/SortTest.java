package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Comparator;

public class SortTest extends ZutubiTestCase
{
    public void testInverseComparator()
    {
        TestComparator baseComparator = new TestComparator();
        Comparator<Integer> comparator = new Sort.InverseComparator(baseComparator);

        assertEquals(0, baseComparator.compare(1, 1));
        assertEquals(0, comparator.compare(1, 1));

        assertEquals(-1, baseComparator.compare(1, 2));
        assertEquals(1, comparator.compare(1, 2));
        
        assertEquals(1, baseComparator.compare(2, 1));
        assertEquals(-1, comparator.compare(2, 1));
    }

    public void testChainComparator()
    {
        Sort.ChainComparator comparator = new Sort.ChainComparator(
                new FixedComparator(1),
                new FixedComparator(2)
        );
        assertEquals(1, comparator.compare(0, 0));

        comparator = new Sort.ChainComparator(
                new FixedComparator(0),
                new FixedComparator(2)
        );
        assertEquals(2, comparator.compare(0, 0));

        comparator = new Sort.ChainComparator(
                new FixedComparator(0),
                new FixedComparator(0)
        );
        assertEquals(0, comparator.compare(0, 0));
    }

    private class TestComparator implements Comparator<Integer>
    {
        public int compare(Integer o1, Integer o2)
        {
            return o1 - o2;
        }
    }

    private class FixedComparator implements Comparator<Integer>
    {
        private int result;

        private FixedComparator(int result)
        {
            this.result = result;
        }

        public int compare(Integer o1, Integer o2)
        {
            return result;
        }
    }
}
