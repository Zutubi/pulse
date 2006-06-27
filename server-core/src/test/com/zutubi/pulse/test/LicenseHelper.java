package com.zutubi.pulse.test;

import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseKeyFactory;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseUtils;

import java.util.Date;
import java.util.Calendar;

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

    /**
     * Generate a license key for a license that does not expire.
     */
    public static String newLicenseKey(String name, String holder) throws LicenseException
    {
        return newLicenseKey(name, holder, null);
    }

    /**
     * Generate a license key for a license that has expired.
     */
    public static String newExpiredLicenseKey(String name, String holder) throws LicenseException
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        return newLicenseKey(name, holder, cal.getTime());
    }

    /**
     * Generate a license key with the specified details.
     */
    public static String newLicenseKey(String name, String holder, Date expiry) throws LicenseException
    {
        byte[] licenseKey = getKeyFactory().encode(new License(name, holder, expiry));

        // insert '\n' characters at regular intervals so that the resulting string is
        // the same in format as those pasted via the UI.
        return LicenseUtils.print(licenseKey);
    }

}
