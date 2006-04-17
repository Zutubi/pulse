package com.zutubi.pulse.license;

/**
 * <class-comment/>
 */
public class LicenseUtils
{
    private static final int WIDTH = 80;

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
}
