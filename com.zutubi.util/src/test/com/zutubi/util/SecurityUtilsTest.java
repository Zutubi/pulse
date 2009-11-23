package com.zutubi.util;

import static com.zutubi.util.SecurityUtils.*;
import com.zutubi.util.junit.ZutubiTestCase;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class SecurityUtilsTest extends ZutubiTestCase
{
    private static final String MD5_EMPTY = "d41d8cd98f00b204e9800998ecf8427e";
    private static final String MD5_HELLO = "5d41402abc4b2a76b9719d911017c592";

    private static final String SHA1_EMPTY = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
    private static final String SHA1_HELLO = "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d";

    private static final String STRING_HELLO = "hello";
    private static final byte[] BYTES_HELLO;
    static
    {
        byte[] bytes;
        try
        {
            bytes = STRING_HELLO.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            bytes = new byte[0];
        }

        BYTES_HELLO = bytes;
    }

    public void testDigestBadAlgorithm()
    {
        try
        {
            digest("bad");
            fail();
        }
        catch (NoSuchAlgorithmException e)
        {
            // Yay
        }
    }

    public void testDigestEmpty() throws NoSuchAlgorithmException
    {
        assertEquals(MD5_EMPTY, digest(ALGORITHM_MD5));
    }

    public void testDigest() throws NoSuchAlgorithmException
    {
        assertEquals(SHA1_HELLO, digest(ALGORITHM_SHA1, BYTES_HELLO));
    }

    public void testDigestUnsafe()
    {
        assertEquals(SHA1_HELLO, digestUnsafe(ALGORITHM_SHA1, BYTES_HELLO));
    }

    public void testDigestStringBadAlgorithm() throws UnsupportedEncodingException
    {
        try
        {
            digest("bad", "");
            fail();
        }
        catch (NoSuchAlgorithmException e)
        {
            // Yay
        }
    }

    public void testDigestStringEmpty() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        assertEquals(SHA1_EMPTY, digest(ALGORITHM_SHA1, ""));
    }

    public void testDigestString() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        assertEquals(MD5_HELLO, digest(ALGORITHM_MD5, STRING_HELLO));
    }

    public void testDigestUnsafeString()
    {
        assertEquals(SHA1_HELLO, digestUnsafe(ALGORITHM_SHA1, STRING_HELLO));
    }

    public void testMD5DigestEmpty()
    {
        assertEquals(MD5_EMPTY, md5Digest());
    }

    public void testMD5Digest()
    {
        assertEquals(MD5_HELLO, md5Digest(BYTES_HELLO));
    }

    public void testMD5DigestString()
    {
        assertEquals(MD5_HELLO, md5Digest(STRING_HELLO));
    }

    public void testSHA1DigestEmpty()
    {
        assertEquals(SHA1_EMPTY, sha1Digest());
    }

    public void testSHA1Digest()
    {
        assertEquals(SHA1_HELLO, sha1Digest(BYTES_HELLO));
    }

    public void testSHA1DigestString()
    {
        assertEquals(SHA1_HELLO, sha1Digest(STRING_HELLO));
    }
}
