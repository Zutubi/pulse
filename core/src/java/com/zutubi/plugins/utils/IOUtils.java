package com.zutubi.plugins.utils;

import java.io.InputStream;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class IOUtils
{
    public static void close(InputStream input)
    {
        if (input != null)
        {
            try
            {
                input.close();
            }
            catch (IOException e)
            {
                // noop.
            }
        }
    }
}
