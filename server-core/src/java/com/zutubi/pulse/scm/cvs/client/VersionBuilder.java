package com.zutubi.pulse.scm.cvs.client;

import org.netbeans.lib.cvsclient.command.Builder;

/**
 * This builder implementation parses the output from the version command when executed on
 * the cvs server.
 *
 * Because the netbeans package implements the client, we do not get a client string.
 * Just the server string.
 *
 *
 * Example of the output from a cvs version call:
 *
 * $ cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs version
 * Client: Concurrent Versions System (CVS) 1.11.17 (client/server)
 * Server: Concurrent Versions System (scast-vc-1.5.2) (CVS) 1.11.1p1 (client/server)
 *
 * and
 *
 * $ cvs -d :pserver:anonymous@dev.w3.org:/sources/public version
 * Client: Concurrent Versions System (CVS) 1.11.17 (client/server)
 * Server: Concurrent Versions System (CVS) 1.12.9 (client/server)
 *
 * Alternatively, when looking at the local host directly, we get:
 *
 * $ cvs -d /c/cvsroot version
 * Concurrent Versions System (CVS) 1.11.17 (client/server)
 *
 */
public class VersionBuilder implements Builder
{
    private String serverVersion;

    public VersionBuilder()
    {
    }

    public void parseLine(String line, boolean isErrorMessage)
    {
        if (isErrorMessage)
        {
            return;
        }

        serverVersion = line;
    }

    public void parseEnhancedMessage(String key, Object value)
    {
    }

    public void outputDone()
    {
    }

    public String getServerVersion()
    {
        return serverVersion;
    }

    public void reset()
    {
        serverVersion = null;
    }
}
