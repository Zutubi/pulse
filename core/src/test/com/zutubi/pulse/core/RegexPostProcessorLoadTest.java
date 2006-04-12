package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Feature;

/**
 */
public class RegexPostProcessorLoadTest extends FileLoaderTestBase
{
    private RegexPostProcessor helper(String ppName) throws PulseException
    {
        PulseFile bf = new PulseFile();
        loader.load(getInput("basic"), bf);

        Scope globalScope = bf.getGlobalScope();
        assertTrue(globalScope.containsReference(ppName));
        assertTrue(globalScope.getReference(ppName) instanceof RegexPostProcessor);

        return (RegexPostProcessor) globalScope.getReference(ppName);
    }

    public void testEmpty() throws PulseException
    {
        RegexPostProcessor pp = helper("empty");
        assertTrue(pp.getFailOnError());
        assertFalse(pp.getFailOnWarning());
        assertEquals(0, pp.getPatterns().size());
    }

    public void testFailOnWarning() throws PulseException
    {
        RegexPostProcessor pp = helper("failOnWarning");
        assertTrue(pp.getFailOnError());
        assertTrue(pp.getFailOnWarning());
        assertEquals(0, pp.getPatterns().size());
    }

    public void testNoFailOnError() throws PulseException
    {
        RegexPostProcessor pp = helper("noFailOnError");
        assertFalse(pp.getFailOnError());
        assertFalse(pp.getFailOnWarning());
        assertEquals(0, pp.getPatterns().size());
    }

    private void levelPattern(String name, Feature.Level level) throws PulseException
    {
        RegexPostProcessor pp = helper(name);
        assertEquals(1, pp.getPatterns().size());
        RegexPattern pattern = pp.getPatterns().get(0);
        assertEquals(level, pattern.getCategory());
        assertEquals(".", pattern.getPattern().pattern());
    }

    public void testErrorPattern() throws PulseException
    {
        levelPattern("errorPattern", Feature.Level.ERROR);
    }

    public void testWarningPattern() throws PulseException
    {
        levelPattern("warningPattern", Feature.Level.WARNING);
    }

    public void testInfoPattern() throws PulseException
    {
        levelPattern("infoPattern", Feature.Level.INFO);
    }

    public void testContext() throws PulseException
    {
        RegexPostProcessor pp = helper("context");
        assertEquals(1, pp.getLeadingContext());
        assertEquals(2, pp.getTrailingContext());
    }

    public void testUnknownLevel() throws PulseException
    {
        errorHelper("unknownLevel", "Unrecognised regex category 'wtf?'");
    }

    public void testRegexPPInvalidRegex() throws PulseException
    {
        errorHelper("invalidRegex", "Unclosed group");
    }

    public void testNegativeLeadingContext() throws PulseException
    {
        errorHelper("negativeLeadingContext", "Leading context count must be non-negative (got -1)");
    }

    public void testNegativeTrailingContext() throws PulseException
    {
        errorHelper("negativeTrailingContext", "Trailing context count must be non-negative (got -2)");
    }
}
