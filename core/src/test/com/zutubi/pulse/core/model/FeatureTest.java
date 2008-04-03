package com.zutubi.pulse.core.model;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.StringUtils;

/**
 */
public class FeatureTest extends PulseTestCase
{
    public void testSummaryBangOn()
    {
        String s = StringUtils.times("x", 4095);
        Feature f = new Feature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
    }

    public void testSummaryTooLong()
    {
        Feature f = new Feature(Feature.Level.WARNING, StringUtils.times("x", 4096));
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendUnderLimit()
    {
        Feature f = new Feature(Feature.Level.ERROR, "yay");
        f.appendToSummary("bird");
        assertEquals("yaybird", f.getSummary());
    }

    public void testAppendHitsLimit()
    {
        Feature f = new Feature(Feature.Level.ERROR, StringUtils.times("x", 4094));
        f.appendToSummary("x");
        assertEquals(StringUtils.times("x", 4095), f.getSummary());
    }
    
    public void testAppendMakesSummaryTooLong()
    {
        String s = StringUtils.times("x", 4090);
        Feature f = new Feature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
        f.appendToSummary("this is long enough");
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendToExactMakesSummaryTooLong()
    {
        String s = StringUtils.times("x", 4095);
        Feature f = new Feature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
        f.appendToSummary("w00t");
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendToAlreadyTrimmedSummary()
    {
        Feature f = new Feature(Feature.Level.WARNING, StringUtils.times("x", 4096));
        String trimmed = getTrimmedSummary();
        assertEquals(trimmed, f.getSummary());
        f.appendToSummary("again, again!");
        assertEquals(trimmed, f.getSummary());
    }
            
    private String getTrimmedSummary()
    {
        return StringUtils.times("x", 4082) + "... [trimmed]";
    }
}
