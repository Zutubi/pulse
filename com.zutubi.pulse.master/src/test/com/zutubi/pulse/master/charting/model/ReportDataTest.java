package com.zutubi.pulse.master.charting.model;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.Pair;

public class ReportDataTest extends PulseTestCase
{
    public void testIsEmptyNoSeries()
    {
        assertTrue(new ReportData().isEmpty());
    }

    public void testIsEmptyOnlyEmptySeries()
    {
        ReportData reportData = new ReportData();
        reportData.addSeries(new SeriesData("s1"));
        reportData.addSeries(new SeriesData("s2"));
        assertTrue(reportData.isEmpty());
    }

    public void testIsEmptyNotEmpty()
    {
        ReportData reportData = new ReportData();
        reportData.addSeries(new SeriesData("s1"));
        SeriesData s2 = new SeriesData("s2");
        s2.addPoint(new DataPoint(1, 1));
        reportData.addSeries(s2);
        assertFalse(reportData.isEmpty());
    }

    public void testGetRangeLimitsEmpty()
    {
        try
        {
            new ReportData().getRangeLimits();
            fail("Shouldn't be able to get limits from empty report.");
        }
        catch (IllegalStateException e)
        {
            assertEquals("Can't get limits for empty report", e.getMessage());
        }
    }

    public void testGetRangeLimitsSinglePoint()
    {
        ReportData reportData = new ReportData();
        SeriesData s1 = new SeriesData("s1");
        s1.addPoint(new DataPoint(2, 5));
        reportData.addSeries(s1);
        assertEquals(new Pair<Number, Number>(5, 5), reportData.getRangeLimits());
    }

    public void testGetRangeLimitsMultiplePoints()
    {
        ReportData reportData = new ReportData();

        SeriesData s1 = new SeriesData("s1");
        s1.addPoint(new DataPoint(2, 5));
        s1.addPoint(new DataPoint(3, 15));
        s1.addPoint(new DataPoint(4, 5));
        s1.addPoint(new DataPoint(5, 55));
        reportData.addSeries(s1);

        SeriesData s2 = new SeriesData("s2");
        s1.addPoint(new DataPoint(2, 10));
        s1.addPoint(new DataPoint(3, 12));
        s1.addPoint(new DataPoint(4, -1));
        s1.addPoint(new DataPoint(5, 5));
        reportData.addSeries(s2);
        
        assertEquals(new Pair<Number, Number>(-1, 55), reportData.getRangeLimits());
    }
}
