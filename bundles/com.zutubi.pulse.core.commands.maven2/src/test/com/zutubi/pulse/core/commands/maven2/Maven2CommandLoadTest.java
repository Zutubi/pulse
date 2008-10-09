package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.ExpressionElement;
import com.zutubi.pulse.core.FileLoaderTestBase;
import com.zutubi.pulse.core.api.PulseException;

import java.util.List;

/**
 */
public class Maven2CommandLoadTest extends FileLoaderTestBase
{
    public void setUp() throws Exception
    {
        super.setUp();

        loader.register("maven2", Maven2Command.class);
    }

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
