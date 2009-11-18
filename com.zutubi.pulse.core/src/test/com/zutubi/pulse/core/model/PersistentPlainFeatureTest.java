package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import static java.util.Arrays.asList;
import java.util.List;

public class PersistentPlainFeatureTest extends PulseTestCase
{
    public void testGetSummaryLinesEmpty()
    {
        assertEquals(asList(""), getSummaryLines(""));
    }

    public void testGetSummaryLinesSingle()
    {
        assertEquals(asList("one line"), getSummaryLines("one line"));
    }

    public void testGetSummaryLinesMultiple()
    {
        assertEquals(asList("line one", "line two", "line three"), getSummaryLines("line one\nline two\nline three"));
    }

    public void testGetSummaryLinesJustNewline()
    {
        assertEquals(asList("", ""), getSummaryLines("\n"));
    }

    public void testGetSummaryLinesEmptyFirstLine()
    {
        assertEquals(asList("", "line two", "line three"), getSummaryLines("\nline two\nline three"));
    }

    public void testGetSummaryLinesEmptyMiddleLine()
    {
        assertEquals(asList("line one", "", "line three"), getSummaryLines("line one\n\nline three"));
    }

    public void testGetSummaryLinesEmptyLastLine()
    {
        assertEquals(asList("line one", "line two", ""), getSummaryLines("line one\nline two\n"));
    }

    private List<String> getSummaryLines(String summary)
    {
        PersistentPlainFeature feature = new PersistentPlainFeature(Feature.Level.ERROR, summary, 1);
        return feature.getSummaryLines();
    }
}
