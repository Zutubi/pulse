package com.zutubi.pulse.core.scm;

import java.io.File;

/**
 * Helpers shared amongst SCM implementations.
 */
public class ScmUtils
{
    public static File[] specToFiles(File base, String... spec) throws ScmException
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
                throw new ScmException("File '" + spec[i] + "' does not exist");
            }
        }

        return result;
    }
}
