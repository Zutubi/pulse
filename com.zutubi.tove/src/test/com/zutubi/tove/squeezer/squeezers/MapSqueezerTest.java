package com.zutubi.tove.squeezer.squeezers;

import com.google.common.collect.ImmutableMap;
import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Collections;
import java.util.Map;

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
        roundTrip(Collections.emptyMap());
    }

    public void testSingleEntry() throws SqueezeException
    {
        roundTrip(ImmutableMap.of("k1", "v1"));
    }

    public void testMultipleEntries() throws SqueezeException
    {
        roundTrip(ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3"));
    }

    public void testKeysAndValuesOfDifferentTypes() throws SqueezeException
    {
        roundTrip(ImmutableMap.of("1", 1, "2", 2, "3", 3));
    }
    
    public void testSeparatorCharacters() throws SqueezeException
    {
        roundTrip(ImmutableMap.of("k:1", "v,1", "k,2", "v:2"));
    }

    public void testPercentCharacters() throws SqueezeException
    {
        roundTrip(ImmutableMap.of("k%1", "v%1"));
    }

    public void testSomeNullKeys() throws SqueezeException
    {
        roundTrip(ImmutableMap.of(null, "v1", 1, "v2"));
    }

    public void testAllNullKeys() throws SqueezeException
    {
        roundTrip(ImmutableMap.of(null, "v1", null, "v2"));
    }

    public void testSomeNullValues() throws SqueezeException
    {
        roundTrip(ImmutableMap.of("k1", null, "k2", 2));
    }

    public void testAllNullValues() throws SqueezeException
    {
        roundTrip(ImmutableMap.of("k1", null, "k2", null));
    }
    
    public void testUnsqueezableKey()
    {
        try
        {
            squeezer.squeeze(ImmutableMap.of(new Unsqueezable(), 1));
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
            squeezer.squeeze(ImmutableMap.of(1, new Unsqueezable()));
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
            squeezer.squeeze(ImmutableMap.of("1", 1, 2, 2));
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
            squeezer.squeeze(ImmutableMap.of(1, "1", 2, 2));
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
