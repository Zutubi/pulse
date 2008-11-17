package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.ExpressionElement;
import com.zutubi.pulse.core.FileLoaderTestBase;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.commands.core.RegexPostProcessor;

import java.util.List;

public class Maven2PostProcessorLoadTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("maven2.pp", Maven2PostProcessor.class);
    }

    public void testEmpty() throws PulseException
    {
        Maven2PostProcessor pp = referenceHelper("empty");
        assertEquals(3, pp.size());
    }

    public void testFailOnError() throws PulseException
    {
        Maven2PostProcessor pp = referenceHelper("fail");
        assertEquals(3, pp.size());
        assertTrue(((RegexPostProcessor)pp.get(1)).isFailOnError());
        assertTrue(((RegexPostProcessor)pp.get(2)).isFailOnError());
    }

    public void testSuppressError() throws PulseException
    {
        Maven2PostProcessor pp = referenceHelper("suppress error");
        List<ExpressionElement> exclusions = pp.getErrorPattern().getExclusions();

        // The error pattern has a built in suppression.
        assertEquals(2, exclusions.size());
        assertEquals("erex", exclusions.get(1).getExpression());
    }

    public void testSuppressWarning() throws PulseException
    {
        Maven2PostProcessor pp = referenceHelper("suppress warning");
        List<ExpressionElement> exclusions = pp.getWarningPattern().getExclusions();
        assertEquals(1, exclusions.size());
        assertEquals("waex", exclusions.get(0).getExpression());
    }
}
