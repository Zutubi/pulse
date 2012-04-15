package com.zutubi.util.adt;

import com.zutubi.util.junit.ZutubiTestCase;

/**
 */
public class CircularBufferTest extends ZutubiTestCase
{
    private CircularBuffer<String> buffer;

    protected void setUp() throws Exception
    {
        super.setUp();
        buffer = new CircularBuffer<String>(4);
    }

    public void testIterateEmpty()
    {
        for (String s : buffer)
        {
            fail();
        }
    }

    public void testAppend()
    {
        helper(1);
    }

    public void testIterate()
    {
        iterateHelper(1);
    }

    public void testFull()
    {
        helper(4);
    }

    public void testIterateFull()
    {
        iterateHelper(4);
    }

    public void testWrap()
    {
        helper(5);
    }

    public void testIterateWrap()
    {
        iterateHelper(5);
    }

    public void testWrapMultiple()
    {
        helper(7);
    }

    public void testIterateWrapMultiple()
    {
        iterateHelper(7);
    }

    private void fill(int n)
    {
        for (int i = 0; i < n; i++)
        {
            buffer.append(String.format("test%d", i));
        }
    }

    public void testFillTwice()
    {
        helper(8);
    }

    public void testIterateFillTwice()
    {
        iterateHelper(8);
    }

    public void testWrapTwice()
    {
        helper(9);
    }

    public void testIterateWrapTwice()
    {
        iterateHelper(9);
    }

    public void testAppendZero()
    {
        CircularBuffer<String> buffer = new CircularBuffer<String>(0);
        buffer.append("i regret nothing....");

        for (String s : buffer)
        {
            fail();
        }
    }

    public void helper(int n)
    {
        fill(n);

        int total = n > 4 ? 4 : n;
        for (int i = 0; i < total; i++)
        {
            int d = i;
            if (n > 4)
            {
                d = i + n - 4;
            }

            assertEquals(String.format("test%d", d), buffer.getElement(i));
        }
    }

    public void iterateHelper(int n)
    {
        fill(n);

        int count = 0;
        for (String s : buffer)
        {
            int d = count;
            if (n > 4)
            {
                d = count + n - 4;
            }
            assertEquals(String.format("test%d", d), s);
            count++;
        }

        assertEquals(n > 4 ? 4 : n, count);

    }

}
