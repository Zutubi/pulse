package com.zutubi.util.math;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AggregationFunctionTest extends ZutubiTestCase
{
    private static final double DELTA = 0.0000001;

    public void testSumEmpty()
    {
        try
        {
            AggregationFunction.SUM.aggregate(Collections.<Number>emptyList());
            fail("Should not be able to aggregate empty collection");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Cannot aggregate an empty collection", e.getMessage());
        }
    }

    public void testSumMismatch()
    {
        try
        {
            AggregationFunction.SUM.aggregate(asList(1L, 2d));
            fail("Should not be able to aggregate mismatched collection");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Unsupported argument types or combination: java.lang.Long and java.lang.Double", e.getMessage());
        }
    }

    public void testSumFloats()
    {
        try
        {
            AggregationFunction.SUM.aggregate(asList(1.0f, 2.0f));
            fail("Should not be able to aggregate floats");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Unsupported argument types or combination: java.lang.Double and java.lang.Float", e.getMessage());
        }
    }

    public void testSumSingleInt()
    {
        assertEquals(3, AggregationFunction.SUM.aggregate(asList(3)));
    }

    public void testSumMultipleInts()
    {
        assertEquals(13, AggregationFunction.SUM.aggregate(asList(2, 9, 2)));
    }

    public void testSumSingleLong()
    {
        assertEquals(3L, AggregationFunction.SUM.aggregate(asList(3L)));
    }

    public void testSumMultipleLongs()
    {
        assertEquals(13L, AggregationFunction.SUM.aggregate(asList(3L, 6L, 4L)));
    }

    public void testSumSingleDouble()
    {
        assertEquals(0.34, AggregationFunction.SUM.aggregate(asList(0.34)).doubleValue(), DELTA);
    }

    public void testSumMultipleDoubles()
    {
        assertEquals(1.3, AggregationFunction.SUM.aggregate(asList(0.3, 0.6, 0.4)).doubleValue(), DELTA);
    }

    public void testMeanSingleInt()
    {
        assertEquals(23.0, AggregationFunction.MEAN.aggregate(asList(23)).doubleValue(), DELTA);
    }

    public void testMeanMultipleInts()
    {
        assertEquals(4.333333333, AggregationFunction.MEAN.aggregate(asList(2, 9, 2)).doubleValue(), DELTA);
    }

    public void testMeanSingleLong()
    {
        assertEquals(235.0, AggregationFunction.MEAN.aggregate(asList(235L)).doubleValue(), DELTA);
    }

    public void testMeanMultipleLongs()
    {
        assertEquals(6.0, AggregationFunction.MEAN.aggregate(asList(6L, 9L, 3L)).doubleValue(), DELTA);
    }

    public void testMeanSingleDouble()
    {
        assertEquals(0.34, AggregationFunction.MEAN.aggregate(asList(0.34)).doubleValue(), DELTA);
    }

    public void testMeanMultipleDoubles()
    {
        assertEquals(2.5, AggregationFunction.MEAN.aggregate(asList(2.0, 1.0, 4.0, 3.0)).doubleValue(), DELTA);
    }

    public void testMinSingleInt()
    {
        assertEquals(3, AggregationFunction.MIN.aggregate(asList(3)));
    }

    public void testMinMultipleInts()
    {
        assertEquals(2, AggregationFunction.MIN.aggregate(asList(2, 9, 32)));
    }

    public void testMinSingleLong()
    {
        assertEquals(3L, AggregationFunction.MIN.aggregate(asList(3L)));
    }

    public void testMinMultipleLongs()
    {
        assertEquals(3L, AggregationFunction.MIN.aggregate(asList(3L, 6L, 4L)));
    }

    public void testMinSingleDouble()
    {
        assertEquals(0.34, AggregationFunction.MIN.aggregate(asList(0.34)).doubleValue(), DELTA);
    }

    public void testMinMultipleDoubles()
    {
        assertEquals(0.3, AggregationFunction.MIN.aggregate(asList(0.3, 0.6, 0.4)).doubleValue(), DELTA);
    }

    public void testMaxSingleInt()
    {
        assertEquals(3, AggregationFunction.MAX.aggregate(asList(3)));
    }

    public void testMaxMultipleInts()
    {
        assertEquals(32, AggregationFunction.MAX.aggregate(asList(2, 9, 32)));
    }

    public void testMaxSingleLong()
    {
        assertEquals(3L, AggregationFunction.MAX.aggregate(asList(3L)));
    }

    public void testMaxMultipleLongs()
    {
        assertEquals(6L, AggregationFunction.MAX.aggregate(asList(3L, 6L, 4L)));
    }

    public void testMaxSingleDouble()
    {
        assertEquals(0.34, AggregationFunction.MAX.aggregate(asList(0.34)).doubleValue(), DELTA);
    }

    public void testMaxMultipleDoubles()
    {
        assertEquals(0.6, AggregationFunction.MAX.aggregate(asList(0.3, 0.6, 0.4)).doubleValue(), DELTA);
    }
    
    /**
     * Wrapper around Arrays.asList that forces types to Number.
     *
     * @param ns numbers to add to the list
     * @return a list of the given numbers
     */
    private List<Number> asList(Number... ns)
    {
        return Arrays.asList(ns);
    }
}
