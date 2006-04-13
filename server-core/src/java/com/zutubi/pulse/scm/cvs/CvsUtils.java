package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.util.logging.Logger;
import org.netbeans.lib.cvsclient.connection.Connection;

import java.io.IOException;

/**
 * Basic utilities class.
 *
 */
public class CvsUtils
{
    private static final Logger LOG = Logger.getLogger(CvsUtils.class);

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
            // Programmatically, we dont care about this exception. That doesnt mean we
            // dont want to know when there are problems.
            LOG.info(e);
        }
    }
}
