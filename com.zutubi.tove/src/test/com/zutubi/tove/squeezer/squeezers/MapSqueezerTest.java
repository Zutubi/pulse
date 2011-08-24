package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Map;

import static com.zutubi.util.CollectionUtils.asMap;
import static com.zutubi.util.CollectionUtils.asPair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class MapSqueezerTest extends ZutubiTestCase
{
    private MapSqueezer squeezer = new MapSqueezer();
    
    public void testNull() throws SqueezeException
    {
        assertEquals("", squeezer.squeeze(null));
        assertNull(squeezer.unsqueeze(""));
    }

    public void testEmpty() throws SqueezeException
    {
        roundTrip(asMap());
    }

    public void testSingleEntry() throws SqueezeException
    {
        roundTrip(asMap(asPair("k1", "v1")));
    }

    public void testMultipleEntries() throws SqueezeException
    {
        roundTrip(asMap(asPair("k1", "v1"), asPair("k2", "v2"), asPair("k3", "v3")));
    }

    public void testKeysAndValuesOfDifferentTypes() throws SqueezeException
    {
        roundTrip(asMap(asPair("1", 1), asPair("2", 2), asPair("3", 3)));
    }
    
    public void testSeparatorCharacters() throws SqueezeException
    {
        roundTrip(asMap(asPair("k:1", "v,1"), asPair("k,2", "v:2")));
    }

    public void testPercentCharacters() throws SqueezeException
    {
        roundTrip(asMap(asPair("k%1", "v%1")));
    }

    public void testSomeNullKeys() throws SqueezeException
    {
        roundTrip(asMap(asPair(null, "v1"), asPair(1, "v2")));
    }

    public void testAllNullKeys() throws SqueezeException
    {
        roundTrip(asMap(asPair(null, "v1"), asPair(null, "v2")));
    }

    public void testSomeNullValues() throws SqueezeException
    {
        roundTrip(asMap(asPair("k1", null), asPair("k2", 2)));
    }

    public void testAllNullValues() throws SqueezeException
    {
        roundTrip(asMap(asPair("k1", null), asPair("k2", null)));
    }
    
    public void testUnsqueezableKey()
    {
        try
        {
            squeezer.squeeze(asMap(asPair(new Unsqueezable(), 1)));
            fail("Should not be able to squeeze a map with an unsqueezable key");
        }
        catch (SqueezeException e)
        {
            assertThat(e.getMessage(), containsString("Cannot convert: no squeezer for class"));
        }
    }
    
    public void testUnsqueezableValue()
    {
        try
        {
            squeezer.squeeze(asMap(asPair(1, new Unsqueezable())));
            fail("Should not be able to squeeze a map with an unsqueezable value");
        }
        catch (SqueezeException e)
        {
            assertThat(e.getMessage(), containsString("Cannot convert: no squeezer for class"));
        }
    }

    public void testMismatchedKeyTypes()
    {
        try
        {
            squeezer.squeeze(asMap(asPair("1", 1), asPair(2, 2)));
            fail("Should not be able to squeeze a map with keys of different types");
        }
        catch (SqueezeException e)
        {
            assertThat(e.getMessage(), containsString("Unable to squeeze maps with different classes"));
        }
    }

    public void testMismatchedValueTypes()
    {
        try
        {
            squeezer.squeeze(asMap(asPair(1, "1"), asPair(2, 2)));
            fail("Should not be able to squeeze a map with values of different types");
        }
        catch (SqueezeException e)
        {
            assertThat(e.getMessage(), containsString("Unable to squeeze maps with different classes"));
        }
    }
    
    private void roundTrip(Map<?, ?> map) throws SqueezeException
    {
        assertEquals(map, squeezer.unsqueeze(squeezer.squeeze(map)));
    }

    public static class Unsqueezable
    {
    }
}
