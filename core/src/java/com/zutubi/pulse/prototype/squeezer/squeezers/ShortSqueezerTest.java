package com.zutubi.pulse.prototype.squeezer.squeezers;

import com.zutubi.pulse.prototype.squeezer.SqueezeException;
import junit.framework.TestCase;

/**
 */
public class ShortSqueezerTest extends TestCase
{
    private ShortSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new ShortSqueezer();
    }

    protected void tearDown() throws Exception
    {
        squeezer = null;
        super.tearDown();
    }

    public void testNullToString() throws SqueezeException
    {
        assertEquals("", squeezer.squeeze(null));
    }

    public void testShortToString() throws SqueezeException
    {
        assertEquals("25", squeezer.squeeze(new Short((short) 25)));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("15", squeezer.squeeze((short)15));
    }

    public void testStringToShort() throws SqueezeException
    {
        assertEquals((short)15, squeezer.unsqueeze("15"));
    }

    public void testEmptyStringToShort() throws SqueezeException
    {
        assertNull(squeezer.unsqueeze(""));
    }

    public void testInvalid() throws SqueezeException
    {
        try
        {
            squeezer.unsqueeze("a");
            fail();
        }
        catch (SqueezeException e)
        {
            assertEquals("'a' is not a valid short", e.getMessage());
        }
    }
}
