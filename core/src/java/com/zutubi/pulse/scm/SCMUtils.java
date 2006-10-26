package com.zutubi.pulse.scm;

import java.io.File;

/**
 * Helpers shared amongst SCM implementations.
 */
public class SCMUtils
{
    public static File[] specToFiles(File base, String... spec) throws SCMException
    {
        if(spec.length == 0)
        {
            return null;
        }
        
        File[] result = new File[spec.length];
        for(int i = 0; i < spec.length; i++)
        {
            result[i] = new File(base, spec[i]);
            if(!result[i].exists())
            {
                throw new SCMException("File '" + spec[i] + "' does not exist");
            }
        }

        return result;
    }
}
