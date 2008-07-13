package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import junit.framework.TestCase;

/**
 */
public class LongSqueezerTest extends TestCase
{
    private LongSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new LongSqueezer();
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

    public void testLongToString() throws SqueezeException
    {
        assertEquals("25", squeezer.squeeze(new Long(25L)));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("15", squeezer.squeeze(15L));
    }

    public void testStringToLong() throws SqueezeException
    {
        assertEquals(15L, squeezer.unsqueeze("15"));
    }

    public void testEmptyStringToLong() throws SqueezeException
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
            assertEquals("'a' is not a valid long", e.getMessage());
        }
    }
}
