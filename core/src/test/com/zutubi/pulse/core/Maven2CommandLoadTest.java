package com.zutubi.pulse.core;

import java.util.List;

/**
 */
public class Maven2CommandLoadTest extends FileLoaderTestBase
{
    public void testEmpty() throws PulseException
    {
        Maven2Command command = commandHelper("empty");
        assertNull(command.getGoals());
    }

    public void testGoals() throws PulseException
    {
        Maven2Command command = commandHelper("goals");
        assertEquals("compile test", command.getGoals());
    }

    public void testSuppressWarnings() throws PulseException
    {
        Maven2Command command = commandHelper("suppress warnings");
        Maven2PostProcessor postProcessor = command.getPp();
        List<ExpressionElement> elements = postProcessor.getWarningPattern().getExclusions();
        assertEquals(1, elements.size());
        assertEquals("hello", elements.get(0).getExpression());
    }

    public void testSuppressErrors() throws PulseException
    {
        Maven2Command command = commandHelper("suppress errors");
        Maven2PostProcessor postProcessor = command.getPp();
        List<ExpressionElement> elements = postProcessor.getWarningPattern().getExclusions();
        assertEquals(1, elements.size());
        assertEquals("errorz", elements.get(0).getExpression());
    }
}
