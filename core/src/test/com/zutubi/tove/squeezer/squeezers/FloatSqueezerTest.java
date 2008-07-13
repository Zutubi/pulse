package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import junit.framework.TestCase;

/**
 */
public class FloatSqueezerTest extends TestCase
{
    private FloatSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new FloatSqueezer();
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

    public void testFloatToString() throws SqueezeException
    {
        assertEquals("1.0", squeezer.squeeze(new Float(1.0)));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("0.015", squeezer.squeeze(0.015f));
    }

    public void testStringToFloat() throws SqueezeException
    {
        assertEquals(1.5f, squeezer.unsqueeze("1.5"));
    }

    public void testEmptyStringToFloat() throws SqueezeException
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
            assertEquals("'a' is not a valid float", e.getMessage());
        }
    }
}
