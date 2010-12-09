package com.zutubi.pulse.servercore.api;

import com.zutubi.pulse.core.test.api.PulseTestCase;

public class APIAuthenticationTokenTest extends PulseTestCase
{
    public void testEncodeDecode()
    {
        APIAuthenticationToken token = new APIAuthenticationToken("myName", "myPass", 1234567890);
        APIAuthenticationToken decoded = new APIAuthenticationToken(token.toString());
        assertEquals(token.getUsername(), decoded.getUsername());
        assertEquals(token.getExpiryTime(), decoded.getExpiryTime());

        APIAuthenticationToken twiceDecoded = new APIAuthenticationToken(decoded.toString());
        assertEquals(token.getUsername(), twiceDecoded.getUsername());
        assertEquals(token.getExpiryTime(), twiceDecoded.getExpiryTime());
    }
}
