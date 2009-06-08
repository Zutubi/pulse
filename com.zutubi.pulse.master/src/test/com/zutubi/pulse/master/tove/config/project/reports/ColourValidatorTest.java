package com.zutubi.pulse.master.tove.config.project.reports;

import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.awt.*;

public class ColourValidatorTest extends PulseTestCase
{
    public void testParseEmpty()
    {
        errorHelper("");
    }

    public void testParseUnknown()
    {
        errorHelper("no such colour");
    }

    public void testParseByName()
    {
        Color color = ColourValidator.parseColour("white");
        assertSame(Color.white, color);
    }

    public void testParseByValue()
    {
        Color color = ColourValidator.parseColour("20");
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(20, color.getBlue());
    }

    public void testParseByHexValue()
    {
        Color color = ColourValidator.parseColour("0xff2018");
        assertEquals(255, color.getRed());
        assertEquals(32, color.getGreen());
        assertEquals(24, color.getBlue());
    }

    private void errorHelper(String colour)
    {
        try
        {
            ColourValidator.parseColour(colour);
            fail("Colour '" + colour + "' should not be parseable");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Unrecognised colour '" + colour + "'", e.getMessage());
        }
    }
}
