package com.cinnamonbob.scm.cvs;

import org.netbeans.lib.cvsclient.connection.Connection;

import java.io.IOException;

/**
 * 
 *
 */
public class CVSHelper
{


    /**
     * Helper method to close the connection.
     * @param c
     */ 
    public static final void close(Connection c)
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
