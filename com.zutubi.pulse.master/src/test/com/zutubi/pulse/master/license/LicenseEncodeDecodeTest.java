package com.zutubi.pulse.master.license;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.RandomUtils;

import java.util.Calendar;

/**
 * <class-comment/>
 */
public class LicenseEncodeDecodeTest extends PulseTestCase
{
    private LicenseEncoder encoder;
    private LicenseDecoder decoder;

    /**
     * Original license string used to ensure backward compatibility of licenses.
     */
    private static final String LICENSE_V1 = "AAAAOUVWQUxVQVRJT04KUy4gTy4gTWVCb2R5CjIwMDYt" +
            "MTAtMTkgMTE6MjY6MjEgRVNUCjEyCjM0CjU2CgLWL3MGBkF68yRbQ4BAjuU7XmO/uJp7jOGFTk5s0" +
            "y7aPiuzxTEuQ/1216+Y+M1n8kQEfZsSSB4dfB/qIynqHdl0EJsvozdORIALcRrAXByhcBKIQE3KfJ" +
            "g/fPhm4Nmfy6Hic9gMeioXjpf6meDSOfnP1F9sOnIh2E1B70Ou3zP0";

    protected void setUp() throws Exception
    {
        super.setUp();

        encoder = new LicenseEncoder();
        decoder = new LicenseDecoder();
    }

    public void testSimpleLicenseEncoding() throws Exception
    {
        License e = new License(LicenseType.EVALUATION, "S. O MeBody", null);
        assertEquals(e, decoder.decode(encoder.encode(e)));
        License c = new License(LicenseType.STANDARD, "Nob. Ody", null);
        assertEquals(c, decoder.decode(encoder.encode(c)));
        License n = new License(LicenseType.NON_PROFIT, "S. O MeBody", null);
        assertEquals(n, decoder.decode(encoder.encode(n)));
    }

    public void testLongLicenseEncoding() throws LicenseException
    {
        License l = new License(LicenseType.EVALUATION, RandomUtils.insecureRandomString(128), null);
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testEncodingOfExpiryDate() throws LicenseException
    {
        // zero out the milliseconds since they are not encoded.
        Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND, 0);

        License l = new License(LicenseType.EVALUATION, "S. O. MeBody", now.getTime());
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testDecodeInvalidLength() throws LicenseException
    {
        LicenseDecoder decoder = new LicenseDecoder();
        assertNull(decoder.decode("SADFA".getBytes()));
    }

    public void testEncodingOfSupportedEntities() throws LicenseException
    {
        License l = new License(LicenseType.EVALUATION, "S. O. MeBody", null);
        l.setSupported(12, 34, 56);
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testBackwardCompatibilityOfLicense() throws LicenseException
    {
/*
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        License l = new License(LicenseType.EVALUATION, "S. O. MeBody", yesterday.getTime());
        l.setSupported(12, 34, 56);
        System.out.println(new String(encoder.encode(l)));
*/

        License l = decoder.decode(LICENSE_V1.getBytes());
        assertEquals(12, l.getSupportedAgents());
        assertEquals(34, l.getSupportedProjects());
        assertEquals(56, l.getSupportedUsers());
        assertEquals(License.UNRESTRICTED, l.getSupportedContactPoints());
        assertEquals(0, l.getDaysRemaining());
        assertEquals("S. O. MeBody", l.getHolder());
        assertEquals(LicenseType.EVALUATION, l.getType());
        assertTrue(l.expires());
        assertTrue(l.isExpired());
    }
}
