package com.zutubi.pulse.core.ui.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class YesNoResponseTest extends PulseTestCase
{
    public void testFromInputEmpty()
    {
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("", YesNoResponse.ALWAYS, YesNoResponse.values()));
    }

    public void testFromInputWhitespace()
    {
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("  \t", YesNoResponse.ALWAYS, YesNoResponse.values()));
    }

    public void testFromInputKeyChars()
    {
        assertEquals(YesNoResponse.YES, YesNoResponse.fromInput("Y", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.NO, YesNoResponse.fromInput("N", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("A", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.NEVER, YesNoResponse.fromInput("E", null, YesNoResponse.values()));
    }

    public void testFromInputKeyCharLower()
    {
        assertEquals(YesNoResponse.YES, YesNoResponse.fromInput("y", null, YesNoResponse.values()));
    }

    public void testFromInputKeyCharNotInAllowed()
    {
        assertNull(YesNoResponse.fromInput("N", YesNoResponse.YES, YesNoResponse.ALWAYS, YesNoResponse.YES));
    }

    public void testFromInputKeyCharNotRecognised()
    {
        assertNull(YesNoResponse.fromInput("X", YesNoResponse.YES, YesNoResponse.values()));
    }

    public void testFromInputKeyCharWhitespace()
    {
        assertEquals(YesNoResponse.YES, YesNoResponse.fromInput(" Y ", null, YesNoResponse.values()));
    }

    public void testFromInputFull()
    {
        assertEquals(YesNoResponse.YES, YesNoResponse.fromInput("YES", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.NO, YesNoResponse.fromInput("NO", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("ALWAYS", null, YesNoResponse.values()));
        assertEquals(YesNoResponse.NEVER, YesNoResponse.fromInput("NEVER", null, YesNoResponse.values()));
    }

    public void testFromInputFullMixedCase()
    {
        assertEquals(YesNoResponse.NO, YesNoResponse.fromInput("No", null, YesNoResponse.values()));
    }

    public void testFromInputFullWhitespace()
    {
        assertEquals(YesNoResponse.ALWAYS, YesNoResponse.fromInput("  ALWAYS  ", null, YesNoResponse.values()));
    }

    public void testFromInputFullNotInAllowed()
    {
        assertNull(YesNoResponse.fromInput("NO", YesNoResponse.YES, YesNoResponse.YES, YesNoResponse.NEVER));
    }

    public void testFromInputFullNotRecognised()
    {
        assertNull(YesNoResponse.fromInput("YEZ", YesNoResponse.YES, YesNoResponse.values()));
    }

}
