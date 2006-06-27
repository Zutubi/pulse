package com.zutubi.pulse.license;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.RandomUtils;

import java.util.Calendar;

/**
 * <class-comment/>
 */
public class LicenseEncodeDecodeTest extends PulseTestCase
{
    private LicenseEncoder encoder;
    private LicenseDecoder decoder;

    public LicenseEncodeDecodeTest()
    {
    }

    public LicenseEncodeDecodeTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        encoder = new LicenseEncoder();
        decoder = new LicenseDecoder();
    }

    protected void tearDown() throws Exception
    {
        encoder = null;
        decoder = null;

        super.tearDown();
    }

    public void testSimpleLicenseEncoding() throws Exception
    {
        License l = new License("dummy", "S. O MeBody", null);
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testLongLicenseEncoding() throws LicenseException
    {
        License l = new License("dummy", RandomUtils.randomString(128), null);
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testEncodingOfExpiryDate() throws LicenseException
    {
        // zero out the milliseconds since they are not encoded.
        Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND, 0);

        License l = new License("dummy", "S. O. MeBody", now.getTime());
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testDecodeInvalidLength() throws LicenseException
    {
        LicenseDecoder decoder = new LicenseDecoder();
        assertNull(decoder.decode("SADFA".getBytes()));
    }
}
