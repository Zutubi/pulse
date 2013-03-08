package com.zutubi.pulse.core.model;

import com.google.common.base.Strings;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;

/**
 */
public class FeatureTest extends PulseTestCase
{
    public void testSummaryBangOn()
    {
        String s = Strings.repeat("x", 4095);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
    }

    public void testSummaryTooLong()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, Strings.repeat("x", 4096));
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
        PersistentFeature f = new PersistentFeature(Feature.Level.ERROR, Strings.repeat("x", 4094));
        f.appendToSummary("x");
        assertEquals(Strings.repeat("x", 4095), f.getSummary());
    }
    
    public void testAppendMakesSummaryTooLong()
    {
        String s = Strings.repeat("x", 4090);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
        f.appendToSummary("this is long enough");
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendToExactMakesSummaryTooLong()
    {
        String s = Strings.repeat("x", 4095);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
        f.appendToSummary("w00t");
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendToAlreadyTrimmedSummary()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, Strings.repeat("x", 4096));
        String trimmed = getTrimmedSummary();
        assertEquals(trimmed, f.getSummary());
        f.appendToSummary("again, again!");
        assertEquals(trimmed, f.getSummary());
    }
            
    private String getTrimmedSummary()
    {
        return Strings.repeat("x", 4082) + "... [trimmed]";
    }
}
