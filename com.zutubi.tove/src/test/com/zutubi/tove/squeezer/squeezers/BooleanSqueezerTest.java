package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.util.junit.ZutubiTestCase;

public class BooleanSqueezerTest extends ZutubiTestCase
{
    private BooleanSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();

        squeezer = new BooleanSqueezer();
    }

    public void testNullToString() throws SqueezeException
    {
        assertEquals("", squeezer.squeeze(null));
    }

    public void testBooleanToString() throws SqueezeException
    {
        assertEquals("true", squeezer.squeeze(Boolean.TRUE));
        assertEquals("false", squeezer.squeeze(Boolean.FALSE));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("false", squeezer.squeeze(false));
        assertEquals("true", squeezer.squeeze(true));
    }

    public void testStringToBoolean() throws SqueezeException
    {
        assertEquals(Boolean.FALSE, squeezer.unsqueeze("false"));
        assertEquals(Boolean.FALSE, squeezer.unsqueeze("0"));
        assertEquals(Boolean.TRUE, squeezer.unsqueeze("true"));
    }

}
