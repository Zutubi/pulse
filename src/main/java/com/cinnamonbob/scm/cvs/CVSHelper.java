package com.cinnamonbob.scm.cvs;

import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.CVSRoot;

import java.io.IOException;

import com.cinnamonbob.scm.cvs.client.ConnectionFactory;

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
