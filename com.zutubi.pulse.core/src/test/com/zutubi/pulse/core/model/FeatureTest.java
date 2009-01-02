package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.StringUtils;

/**
 */
public class FeatureTest extends PulseTestCase
{
    public void testSummaryBangOn()
    {
        String s = StringUtils.times("x", 4095);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
    }

    public void testSummaryTooLong()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, StringUtils.times("x", 4096));
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendUnderLimit()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.ERROR, "yay");
        f.appendToSummary("bird");
        assertEquals("yaybird", f.getSummary());
    }

    public void testAppendHitsLimit()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.ERROR, StringUtils.times("x", 4094));
        f.appendToSummary("x");
        assertEquals(StringUtils.times("x", 4095), f.getSummary());
    }
    
    public void testAppendMakesSummaryTooLong()
    {
        String s = StringUtils.times("x", 4090);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
        f.appendToSummary("this is long enough");
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendToExactMakesSummaryTooLong()
    {
        String s = StringUtils.times("x", 4095);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
        f.appendToSummary("w00t");
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendToAlreadyTrimmedSummary()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, StringUtils.times("x", 4096));
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
