package com.zutubi.util;

import com.zutubi.util.junit.ZutubiTestCase;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 *
 *
 */
public class RandomUtilsTest extends ZutubiTestCase
{
    public void testRandomnessOfSecureRandom() throws NoSuchAlgorithmException
    {
        Random a = new Random(0);
        Random b = new Random(0);
        assertTrue(a != b);
        assertEquals(RandomUtils.randomString(a, 10), RandomUtils.randomString(b, 10));

        Random sa = SecureRandom.getInstance("SHA1PRNG");
        sa.setSeed(0);
        Random sb = SecureRandom.getInstance("SHA1PRNG");
        sb.setSeed(0);
        
        assertTrue(sa != sb);
        assertTrue(RandomUtils.randomString(sa, 10).compareTo(RandomUtils.randomString(sb, 10)) != 0);
    }
}
