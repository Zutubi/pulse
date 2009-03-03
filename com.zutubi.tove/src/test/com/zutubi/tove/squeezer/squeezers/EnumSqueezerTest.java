package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.util.junit.ZutubiTestCase;

public class EnumSqueezerTest extends ZutubiTestCase
{
    private enum TestEnum
    {
        ONE,
        TWO_WORDS,
        THREE_WORDS_HERE
    }

    private EnumSqueezer squeezer = new EnumSqueezer(TestEnum.class);

    public void testSqueezeNull() throws SqueezeException
    {
        assertEquals("", squeezer.squeeze(null));
    }

    public void testSqueezeEnums() throws SqueezeException
    {
        assertEquals("one", squeezer.squeeze(TestEnum.ONE));
        assertEquals("two words", squeezer.squeeze(TestEnum.TWO_WORDS));
        assertEquals("three words here", squeezer.squeeze(TestEnum.THREE_WORDS_HERE));
    }

    public void testUnsqueezeEmpty() throws SqueezeException
    {
        assertNull(squeezer.unsqueeze(""));
    }
    
    public void testUnsqueezeRecognised() throws SqueezeException
    {
        assertEquals(TestEnum.ONE, squeezer.unsqueeze("one"));
        assertEquals(TestEnum.TWO_WORDS, squeezer.unsqueeze("two words"));
        assertEquals(TestEnum.THREE_WORDS_HERE, squeezer.unsqueeze("three words here"));
    }

    public void testUnsqueezeUnrecognised() throws SqueezeException
    {
        try
        {
            squeezer.unsqueeze("one word");
            fail("Can't unsqueeze something not in enum");
        }
        catch (SqueezeException e)
        {
            assertTrue(e.getMessage().contains("Invalid"));
        }
    }
}
