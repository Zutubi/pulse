package com.zutubi.pulse.test;

import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseKeyFactory;
import com.zutubi.pulse.license.LicenseException;

import java.util.Date;

/**
 * <class-comment/>
 */
public class LicenseHelper
{
    private static LicenseKeyFactory keyFactory;

    private static LicenseKeyFactory getKeyFactory()
    {
        if (keyFactory == null)
        {
            try
            {
                // remove the compile time dependency on the License Encoder.
                keyFactory = (LicenseKeyFactory) Class.forName(
                        "com.zutubi.pulse.license.LicenseEncoder").newInstance();
            }
            catch (Exception e)
            {
                keyFactory = new LicenseKeyFactory()
                {
                    public byte[] encode(License license)
                    {
                        return new byte[0];
                    }
                };
            }
        }
        return keyFactory;
    }


    public static String newLicenseKey(String name, String holder, Date expiry) throws LicenseException
    {
        return new String(getKeyFactory().encode(new License(name, holder, expiry)));
    }

}
