package com.zutubi.pulse.license;

import com.zutubi.pulse.test.PulseTestCase;

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
        License l = new License("dummy", null);
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testLongLicenseEncoding() throws LicenseException
    {
        License l = new License("this is a dummy holder that is more then 128 chars in length to ensure that " +
                "setting of the data length at the end of the data encoding is handled correctly.", null);
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testEncodingOfExpiryDate() throws LicenseException
    {
        // zero out the milliseconds since they are not encoded.
        Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND, 0);

        License l = new License("dummy license", now.getTime());
        assertEquals(l, decoder.decode(encoder.encode(l)));
    }

    public void testDecodeInvalidLength() throws LicenseException
    {
        LicenseDecoder decoder = new LicenseDecoder();
        assertNull(decoder.decode("SADFA".getBytes()));
    }
}
