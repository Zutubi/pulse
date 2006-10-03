package com.zutubi.pulse.form.squeezer.squeezers;

import junit.framework.TestCase;
import com.zutubi.pulse.form.squeezer.SqueezeException;

/**
 * <class-comment/>
 */
public class IntegerSqueezerTest extends TestCase
{
    private IntegerSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();

        squeezer = new IntegerSqueezer();
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

    public void testIntegerToString() throws SqueezeException
    {
        assertEquals("25", squeezer.squeeze(new Integer(25)));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("15", squeezer.squeeze(15));
    }

    public void testStringToInteger() throws SqueezeException
    {
        assertEquals(15, squeezer.unsqueeze("15"));
    }
}
