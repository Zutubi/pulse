package com.zutubi.pulse.core.util;

import java.util.Random;

/**
 * <class-comment/>
 */
public class RandomUtils
{
    private static final Random RAND = new Random(System.currentTimeMillis());

    private static final char[] ALPHA_NUMERIC_CHARACTERS = new char[]{'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    public static String randomString(int length)
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++)
        {
            buffer.append(ALPHA_NUMERIC_CHARACTERS[RAND.nextInt(ALPHA_NUMERIC_CHARACTERS.length)]);
        }
        return buffer.toString();
    }
}
