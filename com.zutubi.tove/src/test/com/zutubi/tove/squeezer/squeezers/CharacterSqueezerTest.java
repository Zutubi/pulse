package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.util.junit.ZutubiTestCase;

/**
 */
public class CharacterSqueezerTest extends ZutubiTestCase
{
    private CharacterSqueezer squeezer;

    protected void setUp() throws Exception
    {
        super.setUp();
        squeezer = new CharacterSqueezer();
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

    public void testCharacterToString() throws SqueezeException
    {
        assertEquals("g", squeezer.squeeze(new Character('g')));
    }

    public void testPrimitiveToString() throws SqueezeException
    {
        assertEquals("a", squeezer.squeeze('a'));
    }

    public void testStringToCharacter() throws SqueezeException
    {
        assertEquals('t', squeezer.unsqueeze("t"));
    }

    public void testEmptyStringToCharacter() throws SqueezeException
    {
        assertNull(squeezer.unsqueeze(""));
    }
}
