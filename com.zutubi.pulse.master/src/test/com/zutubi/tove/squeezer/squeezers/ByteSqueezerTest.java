package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import junit.framework.TestCase;

/**
 */
public class ByteSqueezerTest extends TestCase
{
    private ByteSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new ByteSqueezer();
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

    public void testByteToString() throws SqueezeException
    {
        assertEquals("25", squeezer.squeeze(new Byte((byte) 25)));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("15", squeezer.squeeze((byte)15));
    }

    public void testStringToByte() throws SqueezeException
    {
        assertEquals((byte)15, squeezer.unsqueeze("15"));
    }

    public void testEmptyStringToByte() throws SqueezeException
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
            assertEquals("'a' is not a valid byte value", e.getMessage());
        }
    }
}
