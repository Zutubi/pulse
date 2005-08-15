package com.cinnamonbob.scm.cvs;

import org.netbeans.lib.cvsclient.connection.Connection;

import java.io.IOException;

/**
 * 
 *
 */
public class CvsHelper
{
    /**
     * Helper method to close the connection.
     * @param c
     */ 
    public static void close(Connection c)
    {
        try
        {
            if (c != null)
            {
                c.close();
            }
        }
        catch (IOException e)
        {
            //noop.
        }
    }
}
