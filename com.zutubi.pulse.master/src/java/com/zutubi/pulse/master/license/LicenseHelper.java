package com.zutubi.pulse.master.license;

import com.zutubi.util.StringUtils;

import java.util.Calendar;
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
                        "com.zutubi.pulse.master.license.LicenseEncoder").newInstance();
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
    public static String newLicenseKey(LicenseType type, String holder) throws LicenseException
    {
        return newLicenseKey(type, holder, null);
    }

    /**
     * Generate a license key for a license that has expired.
     */
    public static String newExpiredLicenseKey(LicenseType type, String holder) throws LicenseException
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        return newLicenseKey(type, holder, cal.getTime());
    }

    /**
     * Generate an invalid license key.
     */
    public static String newInvalidLicenseKey(LicenseType type, String holder) throws LicenseException
    {
        String validKey = newLicenseKey(type, holder, null);
        return validKey.substring(2, validKey.length() - 2);
    }

    /**
     * Generate a license key with the specified details.
     */
    public static String newLicenseKey(LicenseType type, String holder, Date expiry) throws LicenseException
    {
        return newLicenseKey(new License(type, holder, expiry));
    }

    public static String newLicenseKey(LicenseType type, String holder, Date expiry, int projectLimit, int agentLimit, int userLimit) throws LicenseException
    {
        License license = new License(type, holder, expiry);
        license.setSupported(agentLimit, projectLimit, userLimit);
        return newLicenseKey(license);
    }

    public static String newLicenseKey(License license) throws LicenseException
    {
        byte[] licenseKey = getKeyFactory().encode(license);

        // insert '\n' characters at regular intervals so that the resulting string is
        // the same in format as those pasted via the UI.
        return StringUtils.wrapString(new String(licenseKey), 80, null);
    }

}
