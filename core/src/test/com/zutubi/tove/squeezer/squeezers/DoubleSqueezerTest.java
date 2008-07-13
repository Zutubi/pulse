package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import junit.framework.TestCase;

/**
 */
public class DoubleSqueezerTest extends TestCase
{
    private DoubleSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new DoubleSqueezer();
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

    public void testDoubleToString() throws SqueezeException
    {
        assertEquals("1.0", squeezer.squeeze(new Double(1.0d)));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("0.015", squeezer.squeeze(0.015d));
    }

    public void testStringToDouble() throws SqueezeException
    {
        assertEquals(1.5d, squeezer.unsqueeze("1.5"));
    }

    public void testEmptyStringToDouble() throws SqueezeException
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
            assertEquals("'a' is not a valid double", e.getMessage());
        }
    }
}
