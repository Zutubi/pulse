package com.zutubi.pulse.scm;

/**
 */
public class SCMServerUtils
{
    public static void close(SCMServer scm)
    {
        if(scm != null)
        {
            scm.close();
        }
    }
}
