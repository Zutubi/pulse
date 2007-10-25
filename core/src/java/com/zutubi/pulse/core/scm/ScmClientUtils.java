package com.zutubi.pulse.core.scm;

/**
 */
public class ScmClientUtils
{
    public static void close(ScmClient client)
    {
        if (client != null)
        {
            client.close();
        }
    }
}
