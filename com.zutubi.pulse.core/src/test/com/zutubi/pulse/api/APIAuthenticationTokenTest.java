package com.zutubi.pulse.api;

import com.zutubi.pulse.test.PulseTestCase;

/**
 *
 *
 */
public class APIAuthenticationTokenTest extends PulseTestCase
{
    public void testEncodeDecode()
    {
        APIAuthenticationToken token = new APIAuthenticationToken("myName", "myPass", 1234567890);
        APIAuthenticationToken decoded = APIAuthenticationToken.decode(APIAuthenticationToken.encode(token));
        assertEquals(token.getUsername(), decoded.getUsername());
        assertEquals(token.getExpiryTime(), decoded.getExpiryTime());

        APIAuthenticationToken twiceDecoded = APIAuthenticationToken.decode(APIAuthenticationToken.encode(decoded));
        assertEquals(token.getUsername(), twiceDecoded.getUsername());
        assertEquals(token.getExpiryTime(), twiceDecoded.getExpiryTime());
    }
}
