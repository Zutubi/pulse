package com.zutubi.pulse.license;

import java.util.Calendar;

/**
 * <class-comment/>
 */
public class LicenseUtils
{
    private static final int WIDTH = 60;

    public static String print(byte[] b)
    {
        StringBuffer buffer = new StringBuffer();

        int lines = (b.length / WIDTH);
        if (b.length % WIDTH > 0)
        {
            lines++;
        }
        for (int i = 0; i < lines; i++)
        {
            int start = i * WIDTH;
            int end = (i + 1) * WIDTH;
            if (end > b.length)
            {
                end = b.length;
            }
            for (int j = start; j < end; j++)
            {
                buffer.append((char)b[j]);
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }

    public static void printEncodedLicense(License l) throws LicenseException
    {
        LicenseEncoder encoder = new LicenseEncoder();
        System.out.println(print(encoder.encode(l)));
    }

    public static void main(String argv[]) throws LicenseException
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 45);
        License l = new License("beta", "Aslak Hellesoy", cal.getTime());
        printEncodedLicense(l);
    }
}
