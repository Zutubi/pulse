package com.zutubi.pulse.api;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.binary.Base64;

/**
 *
 *
 */
public class APIAuthenticationToken
{
    private long expiryTime;
    private String username;
    private String signature;

    public APIAuthenticationToken(String username, String password, long expiryTime)
    {
        this.username = username;
        this.expiryTime = expiryTime;
        this.signature = DigestUtils.md5Hex(username + ":" + expiryTime + ":" + password);
    }

    private APIAuthenticationToken(String username, long expiryTime, String signature)
    {
        this.username = username;
        this.expiryTime = expiryTime;
        this.signature = signature;
    }

    public static String encode(APIAuthenticationToken token)
    {
        String tokenValue = token.username + ":" + token.expiryTime + ":" + token.signature;
        return new String(Base64.encodeBase64(tokenValue.getBytes()));
    }

    public static APIAuthenticationToken decode(String token)
    {
        String decoded = new String(Base64.decodeBase64(token.getBytes()));
        String[] parts = decoded.split(":");
        if (parts.length != 3)
        {
            throw new IllegalArgumentException("Invalid token format.");
        }
        String username = parts[0];
        String expiryTime = parts[1];

        // verify the signature? Not needed since our handling of the tokens ensures that they are always valid.
        // We keep a copy of created tokens and require that the user provided token first matches one of these.

        return new APIAuthenticationToken(username, Long.valueOf(expiryTime), parts[2]);
    }

    public String getUsername()
    {
        return this.username;
    }

    public long getExpiryTime()
    {
        return expiryTime;
    }
}
