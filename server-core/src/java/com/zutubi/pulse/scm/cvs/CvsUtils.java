package com.cinnamonbob.scm.cvs;

import org.netbeans.lib.cvsclient.connection.Connection;

import java.io.IOException;

/**
 * Basic utilities class.
 *
 */
public class CvsUtils
{
    /**
     * Helper method to close the connection. 
     *
     * @param c
     */
    public static void close(Connection c)
    {
        try
        {
            if (c != null && c.isOpen())
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
